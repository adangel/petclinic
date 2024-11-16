package javaworkshop.petclinic.web;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.junit.jupiter.api.Test;

import javaworkshop.petclinic.data.Database;
import javaworkshop.petclinic.data.Owner;
import javaworkshop.petclinic.data.OwnerRepository;
import javaworkshop.petclinic.service.OwnerService;

class OwnerControllerTest {

    @Test
    void getAllOwners() {
        OwnerController controller = new OwnerController(
                // use a mocked owner service
                new OwnerService(null) {
                    @Override
                    public List<Owner> searchOwners(Optional<String> lastName) {
                        return List.of(new Owner(), new Owner());
                    }
                }
        );
        String owners = controller.findOwners("");
        JSONArray list = new JSONArray(owners);
        assertEquals(2, list.length());
    }
}
