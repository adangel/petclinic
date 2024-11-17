package javaworkshop.petclinic.web;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import javaworkshop.petclinic.data.Owner;
import javaworkshop.petclinic.service.OwnerService;

class OwnerControllerTest {

    @Test
    void getAllOwners() {
        OwnerController controller = new OwnerController(
                // use a mocked owner service
                new OwnerService(null) {
                    @Override
                    public List<Owner> searchOwners(String lastName) {
                        return List.of(new Owner(), new Owner());
                    }
                }
        );
        List<Owner> owners = controller.findOwners("");
        assertEquals(2, owners.size());
    }
}
