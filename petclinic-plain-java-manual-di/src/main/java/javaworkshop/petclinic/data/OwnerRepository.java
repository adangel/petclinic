package javaworkshop.petclinic.data;

import java.util.List;

public class OwnerRepository {
    public Owner findById(Integer id) {
        return Database.getInstance().getOwnerById(id);
    }

    public List<Owner> findAll() {
        return Database.getInstance().getAllOwners();
    }

    public void create(Owner newOwner) {
        Database.getInstance().createNewOwner(newOwner);
    }
}
