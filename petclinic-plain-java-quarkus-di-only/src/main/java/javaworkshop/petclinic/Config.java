package javaworkshop.petclinic;

import java.util.function.Supplier;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;

public class Config {
    @Produces
    @Named("defaultFirstName")
    StringSupplier defaultFirstName() {
        return new StringSupplier("Jane");
    }

    public static class StringSupplier implements Supplier<String> {
        private final String value;

        public StringSupplier(String value) {
            this.value = value;
        }

        @Override
        public String get() {
            return value;
        }
    }
}
