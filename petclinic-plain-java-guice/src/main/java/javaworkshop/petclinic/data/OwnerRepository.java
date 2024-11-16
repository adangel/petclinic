package javaworkshop.petclinic.data;

import java.util.List;

import jakarta.inject.Inject;

public class OwnerRepository {
    private final Database database;

    @Inject
    public OwnerRepository(Database database) {
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
