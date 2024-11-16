package javaworkshop.petclinic;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;

import io.quarkus.arc.processor.BeanDeploymentValidator;
import io.quarkus.arc.processor.BeanProcessor;
import io.quarkus.arc.processor.BeanRegistrar;
import io.quarkus.arc.processor.ReflectionRegistration;
import io.quarkus.arc.processor.ResourceOutput;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

class SetupArc {
    private static final Logger LOGGER = Logger.getLogger(SetupArc.class.getName());

    static void init(boolean devMode, Path devModeBasePath) {
        if (!devMode) {
            throw new IllegalStateException("This example only works with devMode (e.g. start from IDE)");
        }

        try {
            Indexer indexer = new Indexer();
            index(indexer, Inject.class);
            index(indexer, ApplicationScoped.class);
            index(indexer, App.class);
            Index index = indexer.complete();

            BeanProcessor beanProcessor = BeanProcessor.builder()
                    .setApplicationIndex(index)
                    .setComputingBeanArchiveIndex(index)
                    .setImmutableBeanArchiveIndex(index)
                    .setGenerateSources(true)
                    .setTransformUnproxyableClasses(true)
                    .setOutput(new ResourceOutput() {
                        @Override
                        public void writeResource(Resource resource) throws IOException {
                            switch (resource.getType()) {
                                case SERVICE_PROVIDER -> {
                                    Path target = devModeBasePath.resolve("..")
                                            .resolve("..")
                                            .resolve("..")
                                            .resolve("target")
                                            .resolve("classes")
                                            .resolve("META-INF")
                                            .resolve("services")
                                            .resolve(resource.getName())
                                            .normalize();
                                    Files.createDirectories(target.getParent());
                                    Files.write(target, resource.getData());
                                    LOGGER.info("Created " + target);
                                }
                                case JAVA_CLASS -> {
                                    Path target = devModeBasePath.resolve("..")
                                            .resolve("..")
                                            .resolve("..")
                                            .resolve("target")
                                            .resolve("classes")
                                            .resolve(resource.getName() + ".class")
                                            .normalize();
                                    Files.createDirectories(target.getParent());
                                    Files.write(target, resource.getData());
                                    LOGGER.info("Created " + target);
                                    //LOGGER.info(" source: " + resource.getSource());
                                }
                            }
                        }
                    })
                    .build();
            beanProcessor.registerCustomContexts();
            beanProcessor.registerScopes();
            BeanRegistrar.RegistrationContext registrationContext = beanProcessor.registerBeans();
            beanProcessor.registerSyntheticInjectionPoints(registrationContext);
            beanProcessor.getBeanDeployment().initBeanByTypeMap();
            beanProcessor.registerSyntheticObservers();
            beanProcessor.initialize((bytecodeTransformer) -> {
            }, Collections.emptyList());
            BeanDeploymentValidator.ValidationContext validationContext = beanProcessor.validate(bytecodeTransformer -> {
            });
            beanProcessor.processValidationErrors(validationContext);
            beanProcessor.generateResources(ReflectionRegistration.NOOP, new HashSet<>(), bytecodeTransformer -> {
                    },
                    false, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void index(Indexer indexer, Class<?> startClass) throws IOException, URISyntaxException {
        URL location = startClass.getProtectionDomain().getCodeSource().getLocation();
        final Path path = Paths.get(location.toURI());

        if (!Files.isDirectory(path)) {
            indexJar(indexer, location);
        } else {
            indexDir(indexer, path);
        }
    }

    private static void indexDir(Indexer indexer, Path path) throws IOException {
        try (Stream<Path> s = Files.walk(path)) {
            Set<Path> collect = s.filter(path1 -> !Files.isDirectory(path1))
                    .filter(path1 -> path1.toString().endsWith(".class"))
                    .collect(Collectors.toSet());
            for (Path path1 : collect) {
                LOGGER.fine("Indexing %s".formatted(path1));
                try (InputStream inStream = Files.newInputStream(path1)) {
                    indexer.index(inStream);
                }
            }
        }
    }

    private static void indexJar(Indexer indexer, URL location) throws IOException {
        try (JarInputStream jarInputStream = new JarInputStream(
                location.openStream())) {
            JarEntry entry;
            while ((entry = jarInputStream.getNextJarEntry()) != null) {
                if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                    LOGGER.fine("Indexing %s".formatted(entry.getName()));
                    indexer.index(jarInputStream);
                }
            }
        }
    }
}
