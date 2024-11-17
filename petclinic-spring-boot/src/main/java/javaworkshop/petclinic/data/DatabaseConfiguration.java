package javaworkshop.petclinic.data;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseConfiguration {
    @Bean
    public Database database() {
        return Database.getInstance();
    }
}
