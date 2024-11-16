package javaworkshop.petclinic;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import javaworkshop.petclinic.data.Database;
import javaworkshop.petclinic.data.OwnerRepository;
import javaworkshop.petclinic.service.OwnerService;
import javaworkshop.petclinic.web.OwnerController;

public class ApplicationContext {
    private final Logger LOGGER = Logger.getLogger(ApplicationContext.class.getName());
    private final Map<Class<?>, Object> beans = new HashMap<>();

    public ApplicationContext() {
        Database database = Database.getInstance();
        OwnerRepository ownerRepository = new OwnerRepository(database);
        OwnerService ownerService = new OwnerService(ownerRepository);
        OwnerController ownerController = new OwnerController(ownerService);
        ownerController.setDefaultFirstName("Jane");

        registerBean(Database.class, database);
        registerBean(OwnerRepository.class, ownerRepository);
        registerBean(OwnerService.class, ownerService);
        registerBean(OwnerController.class, ownerController);

        // registerPrototypeBean(OwnerController.class, () -> new OwnerController(getBean(OwnerService.class)));

        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("Initialized application context: %d beans registered.".formatted(beans.size()));
        }
    }

    private <T> void registerBean(Class<T> type, T instance) {
        beans.put(type, instance);
    }

    private <T> void registerPrototypeBean(Class<T> type, Supplier<T> beanSupplier) {
        beans.put(type, beanSupplier);
    }


    // provides singleton beans
    public <T> T getBean(Class<T> type) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Getting bean of type %s".formatted(type));
        }
        Object o = beans.get(type);
        if (o == null) {
            throw new IllegalStateException("No Bean with type " + type + " found.");
        }
        // is it a prototype bean?
        if (o instanceof Supplier<?>) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Creating new bean of type %s".formatted(type));
            }
            @SuppressWarnings("unchecked")
            // a Supplier is only added by registerPrototypeBean and there it is made sure,
            // that T matches the correct type.
            Supplier<T> beanSupplier = (Supplier<T>) o;
            o = beanSupplier.get();
        }

        return type.cast(o);
    }
}
