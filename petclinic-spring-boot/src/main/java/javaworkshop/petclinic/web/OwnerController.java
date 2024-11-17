package javaworkshop.petclinic.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import javaworkshop.petclinic.data.Owner;
import javaworkshop.petclinic.service.OwnerService;

@RestController
@RequestMapping(value = "/owners", produces = "application/json")
//@org.springframework.web.context.annotation.RequestScope
public class OwnerController {
    private final OwnerService ownerService;
    @Value("${defaultFirstName}") // see application.properties
    private String defaultFirstName = "Jon";
    private int counter; // instance field is shared for singletons...

    public OwnerController(OwnerService ownerService) {
        this.ownerService = ownerService;
    }

    @GetMapping
    public List<Owner> findOwners(@RequestParam("lastName") String lastName) {
        return ownerService.searchOwners(lastName);
    }

    @GetMapping("/{id}")
    public Owner getOwnerById(@PathVariable Integer id) {
        Owner owner = ownerService.getOwnerById(id);

        counter++;
        owner.setCity("Request Count: " + counter);
        return owner;
    }

    @PostMapping(value = "/{ownerId}/edit", consumes = "application/json")
    public Owner saveOwner(@PathVariable int ownerId, @RequestBody Owner modifiedOwner, HttpServletResponse response) {
        Owner owner = ownerService.getOwnerById(ownerId);
        if (owner == null) {
            throw new RuntimeException("Owner 1 not found");
        }
        owner.setFirstName(modifiedOwner.getFirstName());
        owner.setLastName(modifiedOwner.getLastName());
        owner.setAddress(modifiedOwner.getAddress());
        owner.setCity(modifiedOwner.getCity());
        owner.setTelephone(modifiedOwner.getTelephone());
        ownerService.saveOwner(owner);

        response.setHeader("X-Redirect-Path", "/owners/%d".formatted(ownerId));
        return owner;
    }

    @PostMapping("/new")
    public Owner newOwner(@RequestBody Owner newOwner, HttpServletResponse response) {
        String firstName = newOwner.getFirstName();
        if (firstName == null || firstName.isBlank()) {
            newOwner.setFirstName(defaultFirstName);
        }

        ownerService.create(newOwner);
        response.setHeader("X-Redirect-Path", "/owners/%d".formatted(newOwner.getId()));
        return newOwner;
    }
}
