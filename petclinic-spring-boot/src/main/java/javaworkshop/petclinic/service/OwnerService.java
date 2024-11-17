package javaworkshop.petclinic.service;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import javaworkshop.petclinic.data.Owner;
import javaworkshop.petclinic.data.OwnerRepository;

@Service
public class OwnerService {
    private final OwnerRepository repository;

    public OwnerService(OwnerRepository repository) {
        this.repository = repository;
    }

    public List<Owner> searchOwners(String lastName) {
        List<Owner> all = repository.findAll();
        all = all.stream().filter(owner -> owner.getLastName().toLowerCase(Locale.ROOT).contains(lastName.toLowerCase(Locale.ROOT))).toList();
        return all;
    }

    public Owner getOwnerById(int ownerId) {
        return repository.findById(ownerId);
    }

    public void saveOwner(Owner owner) {
        if (owner.getId() == null) {
            throw new IllegalArgumentException("owner without id cannot be saved");
        }
        if (repository.findById(owner.getId()) == null) {
            throw new IllegalArgumentException("owner does not exist");
        }
    }

    public void create(Owner newOwner) {
        if (newOwner.getId() != null) {
            throw new IllegalArgumentException("new owner must not have an id");
        }
        if (!newOwner.getPets().isEmpty()) {
            throw new IllegalArgumentException("new owner must not have pets yet");
        }
        repository.create(newOwner);
    }
}
