package javaworkshop.petclinic.data;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OwnerRepository {
    private final Database database;

    @Autowired
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
