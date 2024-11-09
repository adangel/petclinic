package javaworkshop.petclinic.web;

import static org.junit.jupiter.api.Assertions.*;

import org.json.JSONArray;
import org.junit.jupiter.api.Test;

class OwnerControllerTest {

    @Test
    void getAllOwners() {
        OwnerController controller = new OwnerController();
        String owners = controller.findOwners("");
        JSONArray list = new JSONArray(owners);
        assertEquals(2, list.length());
    }
}
