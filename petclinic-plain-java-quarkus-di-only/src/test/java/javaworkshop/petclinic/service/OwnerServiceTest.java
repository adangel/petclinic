package javaworkshop.petclinic.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import javaworkshop.petclinic.data.Database;
import javaworkshop.petclinic.data.Owner;
import javaworkshop.petclinic.data.OwnerRepository;

class OwnerServiceTest {

    @Test
    void canCreateOwner() {
        OwnerService service = new OwnerService(
                // use a mocked owner repository, that knows only about id 1.
                new OwnerRepository(null) {
                    @Override
                    public Owner findById(Integer id) {
                        return id == 1 ? new Owner() : null;
                    }
                }
        );

        assertNotNull(service.getOwnerById(1));
        assertNull(service.getOwnerById(42));
    }

    @Test
    void searchByLastName() {
        OwnerService service = new OwnerService(
                // create a mocked owner repository that returns two owners, one should be found by the service.
                new OwnerRepository(null) {

                    @Override
                    public List<Owner> findAll() {
                        Owner testOwner = new Owner();
                        testOwner.setLastName("Test");
                        Owner otherOwner = new Owner();
                        otherOwner.setLastName("Other");
                        return List.of(testOwner, otherOwner);
                    }
                }
        );

        List<Owner> list = service.searchOwners(Optional.of("Test"));
        assertEquals(1, list.size());
    }
}
