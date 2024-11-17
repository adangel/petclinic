package javaworkshop.petclinic.data;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@ApplicationScoped
public class OwnerRepository {
    private final Database database;

    public OwnerRepository(@Named("instance") Database database) {
        this.database = database;
    }

    public Owner findById(Integer id) {
        return database.getOwnerById(id);
    }

    public List<Owner> findAll() {
        return database.getAllOwners();
    }

    public void create(Owner newOwner) {
        database.createNewOwner(newOwner);
    }
}
