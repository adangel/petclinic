package javaworkshop.petclinic;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javaworkshop.petclinic.data.Database;

@Configuration
@ComponentScan(basePackages = "javaworkshop.petclinic")
public class ApplicationConfiguration {

    @Bean
    Database database() {
        return Database.getInstance();
    }

    @Bean("defaultFirstName")
    String defaultFirstName() {
        return "Jane";
    }
}
