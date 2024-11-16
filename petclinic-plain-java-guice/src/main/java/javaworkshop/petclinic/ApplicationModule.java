package javaworkshop.petclinic;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import javaworkshop.petclinic.data.Database;

public class ApplicationModule extends AbstractModule {

    @Provides
    @Singleton
    static Database provideDatabase() {
        return Database.getInstance();
    }

    @Provides
    @Named("defaultFirstName")
    String defaultFirstName() {
        return "Jane";
    }
}
