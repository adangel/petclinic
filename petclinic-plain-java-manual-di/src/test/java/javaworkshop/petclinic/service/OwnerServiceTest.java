package javaworkshop.petclinic.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import javaworkshop.petclinic.data.Owner;

//@TestMethodOrder(MethodOrderer.MethodName.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OwnerServiceTest {

    @Test
    @Order(2)
    void canCreateOwner() {
        OwnerService service = new OwnerService();
        Owner newOwner = new Owner();
        newOwner.setLastName("Test");
        assertNull(newOwner.getId());

        service.create(newOwner);

        assertNotNull(newOwner.getId());
        Owner loaded = service.getOwnerById(newOwner.getId());
        assertEquals(newOwner, loaded);
    }

    @Test
    @Order(1)
    void searchByLastName() {
        OwnerService service = new OwnerService();
        List<Owner> list = service.searchOwners(Optional.of("Test"));
        assertTrue(list.isEmpty());

        Owner newOwner = new Owner();
        newOwner.setLastName("Test");
        service.create(newOwner);

        list = service.searchOwners(Optional.of("Test"));
        assertEquals(1, list.size());
    }
}
