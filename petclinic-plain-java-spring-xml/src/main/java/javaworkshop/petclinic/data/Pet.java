package javaworkshop.petclinic.data;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

public class Pet extends BaseEntity {
    private String name;
    private LocalDate birthDate;
    private PetType petType;
    private Set<Visit> visits = new LinkedHashSet<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public PetType getPetType() {
        return petType;
    }

    public void setPetType(PetType petType) {
        this.petType = petType;
    }

    public Set<Visit> getVisits() {
        return visits;
    }

    public void setVisits(Set<Visit> visits) {
        this.visits = visits;
    }

    @Override
    public String toString() {
        return "Pet{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", birthDate=" + birthDate +
                ", petType=" + petType +
                ", visits=" + visits +
                '}';
    }

    static Pet createPet(Integer id, String name, LocalDate birthDate, PetType petType) {
        Pet pet = new Pet();
        pet.setId(id);
        pet.setName(name);
        pet.setBirthDate(birthDate);
        pet.setPetType(petType);
        return pet;
    }
}
