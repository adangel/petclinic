package javaworkshop.petclinic.web;

import static org.springframework.web.servlet.function.RequestPredicates.accept;
import static org.springframework.web.servlet.function.RequestPredicates.path;
import static org.springframework.web.servlet.function.RouterFunctions.route;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class WebConfiguration {

    // https://stackoverflow.com/questions/27381781/java-spring-boot-how-to-map-my-app-root-to-index-html
    @Bean
    RouterFunction<ServerResponse> spaRouter() {
        ClassPathResource index = new ClassPathResource("static/index.html");
        return route().resource(
                accept(MediaType.TEXT_HTML)
                        .and(path("/js/*").or(path("/css/*")).negate())
                , index).build();
    }
}
