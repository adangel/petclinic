package javaworkshop.petclinic.web;

import io.vertx.ext.web.Router;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Singleton;
import jakarta.ws.rs.core.MediaType;

@Singleton
public class RouterConfiguration {
    // https://stackoverflow.com/questions/61197844/how-to-re-direct-all-not-found-routes-to-index-html-using-the-vertx-router-with
    // https://quarkus.io/guides/reactive-routes#using-the-vert-x-web-router
    public void init(@Observes Router router) {
        router.routeWithRegex("\\/(?!js\\/|css\\/).+")
                .produces(MediaType.TEXT_HTML)
                .handler(routingContext -> routingContext.reroute("/"));
    }
}
