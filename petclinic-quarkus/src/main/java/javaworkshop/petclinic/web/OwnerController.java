package javaworkshop.petclinic.web;

import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;
import org.jboss.resteasy.reactive.RestResponse;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import javaworkshop.petclinic.data.Owner;
import javaworkshop.petclinic.service.OwnerService;

@Path("/owners")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
//@jakarta.enterprise.context.RequestScoped
public class OwnerController {
    private final OwnerService ownerService;
    @ConfigProperty(name = "defaultFirstName") // see application.properties
    private String defaultFirstName = "Jon";
    private int counter; // instance field is shared for singletons (application scoped)...

    public OwnerController(OwnerService ownerService) {
        this.ownerService = ownerService;
    }

    @GET
    public List<Owner> findOwners(@RestQuery String lastName) {
        List<Owner> all = ownerService.searchOwners(lastName);
        return all;
    }

    @GET
    @Path("/{id}")
    public Owner getOwnerById(@RestPath Integer id) {
        Owner owner = ownerService.getOwnerById(id);
        if (owner == null) {
            return null;
        }

        counter++;
        owner.setCity("Request Count: " + counter);
        return owner;
    }

    @POST
    @Path("/{ownerId}/edit")
    public RestResponse<Owner> saveOwner(@RestPath int ownerId, Owner modifiedOwner) {
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

        return RestResponse.ResponseBuilder.ok(owner)
                .header("X-Redirect-Path", "/owners/%d".formatted(ownerId))
                .build();
    }

    @POST
    @Path("/new")
    public RestResponse<Owner> newOwner(Owner newOwner) {
        String firstName = newOwner.getFirstName();
        if (firstName == null || firstName.isBlank()) {
            firstName = defaultFirstName;
            newOwner.setFirstName(defaultFirstName);
        }

        ownerService.create(newOwner);
        return RestResponse.ResponseBuilder.ok(newOwner)
                .header("X-Redirect-Path", "/owners/%d".formatted(newOwner.getId()))
                .build();
    }
}
