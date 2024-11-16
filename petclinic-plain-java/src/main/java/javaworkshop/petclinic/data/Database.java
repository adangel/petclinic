package javaworkshop.petclinic.data;

import static javaworkshop.petclinic.data.Owner.createOwner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Database {
    private final static Database INSTANCE = new Database();
    public static Database getInstance() {
        return INSTANCE;
    }

    private Map<Integer, Owner> owners = new HashMap<>();
    private Integer nextOwnerId;

    private Map<Integer, PetType> petTypes = new HashMap<>();

    private Database() {
        init();
    }

    private void init() {
        petTypes.put(1, PetType.createPetType(1, "cat"));
        petTypes.put(6, PetType.createPetType(6, "hamster"));

        // INSERT INTO owners VALUES (default, 'George', 'Franklin', '110 W. Liberty St.', 'Madison', '6085551023');
        owners.put(1, createOwner(1, "George", "Franklin", "110 W. Liberty St.", "Madison", "6085551023"));
        // INSERT INTO owners VALUES (default, 'Betty', 'Davis', '638 Cardinal Ave.', 'Sun Prairie', '6085551749');
        owners.put(2, createOwner(2, "Betty", "Davis", "638 Cardinal Ave.", "Sun Prairie", "6085551749"));

        //INSERT INTO pets VALUES (default, 'Leo', '2010-09-07', 1, 1);
        owners.get(1).addPet(Pet.createPet(1, "Leo", LocalDate.parse("2010-09-07"), petTypes.get(1)));
        // INSERT INTO pets VALUES (default, 'Basil', '2012-08-06', 6, 2);
        owners.get(2).addPet(Pet.createPet(2, "Basil", LocalDate.parse("2012-08-06"), petTypes.get(6)));

        nextOwnerId = 3;
    }

    public Owner getOwnerById(int ownerId) {
        return owners.get(ownerId);
    }

    public List<Owner> getAllOwners() {
        return new ArrayList<>(owners.values());
    }

    public void createNewOwner(Owner newOwner) {
        newOwner.setId(nextOwnerId);
        nextOwnerId++;
        owners.put(newOwner.getId(), newOwner);
    }
}
