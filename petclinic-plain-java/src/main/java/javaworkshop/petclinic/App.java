package javaworkshop.petclinic;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.json.JSONTokener;

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


    static {
        try {
            LogManager.getLogManager().readConfiguration(App.class.getResourceAsStream("/logging.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private final boolean devMode;
    private final HttpServer httpServer;
    public App(int port, boolean developmentMode) throws IOException {
        this.devMode = developmentMode;
        httpServer = HttpServer.create();
        httpServer.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), port), 0);
        HttpContext context = httpServer.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                boolean post = "POST".equals(exchange.getRequestMethod());
                boolean get = "GET".equals(exchange.getRequestMethod());
                String requestPath = exchange.getRequestURI().getPath();
                String acceptHeader = Optional.ofNullable(exchange.getRequestHeaders().getFirst("Accept")).orElse("");

                if (acceptHeader.contains("application/json")) {
                    if (get && requestPath.matches("^/owners/\\d+$")) {
                        Matcher matcher = Pattern.compile("^/owners/(\\d+)$").matcher(requestPath);
                        if (!matcher.matches()) {
                            throw new IllegalStateException("invalid request path");
                        }
                        handleController(exchange, () -> new OwnerController().getOwnerById(Integer.parseInt(matcher.group(1))));
                        return;
                    }
                    if (get && requestPath.matches("^/owners.*$")) {
                        handleController(exchange, () -> new OwnerController().findOwners(exchange.getRequestURI().getQuery()));
                        return;
                    }
                    if (post && requestPath.matches("^/owners/\\d+/edit$")) {
                        Matcher matcher = Pattern.compile("^/owners/(\\d+)/edit$").matcher(requestPath);
                        if (!matcher.matches()) {
                            throw new IllegalStateException("invalid request path");
                        }
                        int ownerId = Integer.parseInt(matcher.group(1));
                        JSONObject data = new JSONObject(new JSONTokener(exchange.getRequestBody()));
                        handleController(exchange, () -> new OwnerController().saveOwner(ownerId, data), "/owners/%d".formatted(ownerId));
                        return;
                    }
                    if (post && requestPath.matches("^/owners/new$")) {
                        JSONObject data = new JSONObject(new JSONTokener(exchange.getRequestBody()));
                        Owner newOwner = new OwnerController().newOwner(data);
                        handleController(exchange, () -> new OwnerController().getOwnerById(newOwner.getId()), "/owners/%d".formatted(newOwner.getId()));
                        return;
                    }
                }

                if (requestPath.matches("^/js/.*")) {
                    sendResource(exchange, "application/javascript;charset=UTF-8", findResource(requestPath));
                    return;
                }
                if (requestPath.matches("^/css/[^/]+")) {
                    sendResource(exchange, "text/css", findResource(requestPath));
                    return;
                }

                // single page app - always return index.html
                handleResource(exchange, "index.html");
            }
        });
        context.getFilters().add(Filter.afterHandler("Log", exchange -> {
            // 127.0.0.1 - frank [10/Oct/2000:13:55:36 -0700] "GET /apache_pb.gif HTTP/1.0" 200 2326
            String logline = "%s - - [%s] \"%s %s %s\" %d -";
            LOGGER.info(logline.formatted(
                    exchange.getRemoteAddress().getHostString(),
                    OffsetDateTime.now().toString(),
                    exchange.getRequestMethod(),
                    exchange.getRequestURI().toString(),
                    exchange.getProtocol(),
                    exchange.getResponseCode()));
        }));
    }

    private void start() {
        httpServer.start();
        LOGGER.info("Started webserver: http://%s:%s".formatted(httpServer.getAddress().getAddress(), httpServer.getAddress().getPort()));
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
            exchange.sendResponseHeaders(404, -1);
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
            Path p = Path.of("petclinic-plain-java/src/main/resources/html", name);
            if (Files.exists(p)) {
                resource = Files.newInputStream(p);
            }
        } else {
            resource = App.class.getResourceAsStream("/html/" + name);
        }

        if (resource == null) {
            throw new IllegalArgumentException("HTML Resource " + name + " not found!");
        }
        return resource;
    }

    private Optional<InputStream> findResource(String path) throws IOException {
        if (devMode) {
            Path p = Path.of("petclinic-plain-java/src/main/resources", path);
            if (Files.exists(p)) {
                return Optional.of(Files.newInputStream(p));
            }
            return Optional.empty();
        }

        return Optional.ofNullable(App.class.getResourceAsStream(path));
    }
    private static void sendResource(HttpExchange exchange, final String contentType, final Optional<InputStream> stream) throws IOException {
        try (exchange) {
            if (stream.isEmpty()) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }

            exchange.getResponseHeaders().set("Content-type", contentType);
            exchange.sendResponseHeaders(200, 0);
            try (InputStream in = stream.get(); OutputStream out = exchange.getResponseBody()) {
                in.transferTo(out);
            }
        }
    }
}
