package javaworkshop.petclinic;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.json.JSONObject;
import org.json.JSONTokener;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import javaworkshop.petclinic.data.Owner;
import javaworkshop.petclinic.web.OwnerController;

public class App {
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    public static void main(String[] args) throws IOException {
        App app = new App(8081, true);
        app.start();
    }


    private final boolean devMode;
    private Path devModeBasePath;
    private final HttpServer httpServer;
    public App(int port, boolean developmentMode) throws IOException {
        initLogging();
        devMode = initializeDevMode(developmentMode);
        httpServer = HttpServer.create();
        httpServer.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), port), 0);

        Injector injector = Guice.createInjector(new ApplicationModule());

        HttpContext context = httpServer.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                boolean post = "POST".equals(exchange.getRequestMethod());
                boolean get = "GET".equals(exchange.getRequestMethod());
                String requestPath = exchange.getRequestURI().getPath();
                String acceptHeader = Optional.ofNullable(exchange.getRequestHeaders().getFirst("Accept")).orElse("");

                // REST-API Requests for JSON data
                if (acceptHeader.contains("application/json")) {
                    if (get && requestPath.matches("^/owners/\\d+$")) {
                        Matcher matcher = Pattern.compile("^/owners/(\\d+)$").matcher(requestPath);
                        if (!matcher.matches()) {
                            throw new IllegalStateException("invalid request path");
                        }
                        handleController(exchange, () -> injector.getInstance(OwnerController.class).getOwnerById(Integer.parseInt(matcher.group(1))));
                        return;
                    }
                    if (get && requestPath.matches("^/owners$")) {
                        handleController(exchange, () -> injector.getInstance(OwnerController.class).findOwners(exchange.getRequestURI().getQuery()));
                        return;
                    }
                    if (post && requestPath.matches("^/owners/\\d+/edit$")) {
                        Matcher matcher = Pattern.compile("^/owners/(\\d+)/edit$").matcher(requestPath);
                        if (!matcher.matches()) {
                            throw new IllegalStateException("invalid request path");
                        }
                        int ownerId = Integer.parseInt(matcher.group(1));
                        JSONObject data = new JSONObject(new JSONTokener(exchange.getRequestBody()));
                        handleController(exchange, () -> injector.getInstance(OwnerController.class).saveOwner(ownerId, data), "/owners/%d".formatted(ownerId));
                        return;
                    }
                    if (post && requestPath.matches("^/owners/new$")) {
                        JSONObject data = new JSONObject(new JSONTokener(exchange.getRequestBody()));
                        Owner newOwner = injector.getInstance(OwnerController.class).newOwner(data);
                        handleController(exchange, () -> injector.getInstance(OwnerController.class).getOwnerById(newOwner.getId()), "/owners/%d".formatted(newOwner.getId()));
                        return;
                    }
                }

                // Normal Resource Requests for Single Page App
                if (get) {
                    if (requestPath.matches("^/js/.*")) {
                        sendResource(exchange, "application/javascript;charset=UTF-8", findResource(requestPath));
                        return;
                    }
                    if (requestPath.matches("^/css/[^/]+")) {
                        sendResource(exchange, "text/css", findResource(requestPath));
                        return;
                    }

                    // single page app - always return index.html
                    if (acceptHeader.contains("text/html")) {
                        handleResource(exchange, "index.html");
                        return;
                    }
                }

                // anything else: 404 not found
                sendNotFound(exchange);
            }
        });
        context.getFilters().add(Filter.afterHandler("Log", exchange -> {
            if (LOGGER.isLoggable(Level.INFO)) {
                // 127.0.0.1 - frank [10/Oct/2000:13:55:36 -0700] "GET /apache_pb.gif HTTP/1.0" 200 2326
                String logline = "%s - - [%s] \"%s %s %s\" %d -";
                LOGGER.info(logline.formatted(
                        exchange.getRemoteAddress().getHostString(),
                        OffsetDateTime.now().toString(),
                        exchange.getRequestMethod(),
                        exchange.getRequestURI().toString(),
                        exchange.getProtocol(),
                        exchange.getResponseCode()));
            }
        }));
    }

    private void initLogging() {
        try {
            LogManager.getLogManager().readConfiguration(App.class.getResourceAsStream("/logging.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static class LogFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            String logLevel = record.getLevel().getName();
            if (record.getLevel().equals(Level.WARNING)) {
                logLevel = "WARN";
            }
            String loggerName = record.getLoggerName();
            if (loggerName.length() > 10) {
                String[] names = loggerName.split("\\.");
                for (int i = 0; i < names.length - 1; i++) {
                    names[i] = names[i].substring(0, 1);
                }
                loggerName = String.join(".", names);
            }
            String thrown = "";
            if (record.getThrown() != null) {
                StringWriter s = new StringWriter();
                record.getThrown().printStackTrace(new PrintWriter(s));
                thrown = s.toString();
            }

            String message = record.getMessage();
            if (record.getParameters() != null) {
                message = MessageFormat.format(record.getMessage(), record.getParameters());
            }

            return String.format("%s - %s: %s%s%n",
                    logLevel,
                    loggerName,
                    message,
                    thrown);
        }
    }

    private boolean initializeDevMode(boolean developmentMode) {
        final boolean devMode;
        if (developmentMode) {
            Path pathToIndexHtml = Paths.get("src", "main", "resources", "html", "index.html");
            devModeBasePath = Stream.of(Paths.get("petclinic-plain-java-guice").resolve(pathToIndexHtml), pathToIndexHtml)
                    .filter(Files::exists)
                    .findFirst()
                    .map(p -> p.getParent().getParent()) // select ...src/main/resources
                    .orElse(null);

            if (devModeBasePath != null) {
                devMode = true;
            } else {
                LOGGER.warning("Couldn't find resources for devMode. DevMode is disabled!");
                devMode = false;
            }
        } else {
            devMode = false;
        }
        return devMode;
    }

    private void start() {
        httpServer.start();
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("Started webserver: http://%s:%s".formatted(httpServer.getAddress().getHostString(), httpServer.getAddress().getPort()));
        }
        if (devMode) {
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.info("-> DevMode is active: serving static resources from %s".formatted(devModeBasePath));
            }
        }
    }

    private void handleResource(HttpExchange exchange, String name) throws IOException {
        exchange.sendResponseHeaders(200, 0);
        try (OutputStream out = exchange.getResponseBody();
             InputStream resource = getHtmlResource(name)) {
            resource.transferTo(out);
        }
    }

    private static void handleController(HttpExchange exchange, Supplier<String> jsonProducer) throws IOException {
        handleController(exchange, jsonProducer, null);
    }

    private static void handleController(HttpExchange exchange, Supplier<String> jsonProducer, String redirectHeader) throws IOException {
        String result = jsonProducer.get();
        if (result == null) {
            sendNotFound(exchange);
            return;
        }

        byte[] bytes = result.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        if (redirectHeader != null) {
            exchange.getResponseHeaders().set("X-Redirect-Path", redirectHeader);
        }
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    private InputStream getHtmlResource(String name) throws IOException {
        InputStream resource = null;

        if (devMode) {
            Path p = devModeBasePath.resolve(Paths.get("html", name));
            if (Files.exists(p)) {
                resource = Files.newInputStream(p);
            } else {
                if (LOGGER.isLoggable(Level.WARNING)) {
                    LOGGER.warning("DevMode: HTML resource %s not found!".formatted(p));
                }
            }
        }

        if (resource == null) {
            resource = App.class.getResourceAsStream("/html/" + name);
        }

        if (resource == null) {
            throw new IllegalArgumentException("HTML Resource " + name + " not found!");
        }
        return resource;
    }

    private Optional<InputStream> findResource(String path) throws IOException {
        if (devMode) {
            Path p = devModeBasePath.resolve(path.substring(1)); // skip leading "/"
            if (Files.exists(p)) {
                return Optional.of(Files.newInputStream(p));
            } else {
                if (LOGGER.isLoggable(Level.WARNING)) {
                    LOGGER.warning("DevMode: Static Resource %s not found!".formatted(p));
                }
            }
        }

        return Optional.ofNullable(App.class.getResourceAsStream(path));
    }
    private static void sendResource(HttpExchange exchange, final String contentType, final Optional<InputStream> stream) throws IOException {
        try (exchange) {
            if (stream.isEmpty()) {
                sendNotFound(exchange);
                return;
            }

            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, 0);
            try (InputStream in = stream.get(); OutputStream out = exchange.getResponseBody()) {
                in.transferTo(out);
            }
        }
    }

    private static void sendNotFound(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(404, -1);
    }
}
