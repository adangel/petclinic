package javaworkshop.petclinic.web;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.json.JSONWriter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import javaworkshop.petclinic.Config;
import javaworkshop.petclinic.data.Owner;
import javaworkshop.petclinic.data.Pet;
import javaworkshop.petclinic.service.OwnerService;

@ApplicationScoped
public class OwnerController {
    private final OwnerService ownerService;
    private String defaultFirstName = "Jon";
    private int counter; // instance field is shared for singletons...

    OwnerController() {
        this.ownerService = null;
    }

    @Inject
    public OwnerController(OwnerService ownerService) {
        this.ownerService = ownerService;
    }

    @Inject
    public void setDefaultFirstName(@Named("defaultFirstName") Config.StringSupplier defaultFirstName) {
        this.defaultFirstName = defaultFirstName.get();
    }

    public String findOwners(String query) {
        Optional<String> lastName = Optional.empty();
        Pattern pattern = Pattern.compile("lastName=(.+)");
        Matcher matcher = pattern.matcher(query);
        if (matcher.find()) {
            lastName = Optional.of(matcher.group(1));
        }

        List<Owner> all = ownerService.searchOwners(lastName);
        String result = all.stream().map(OwnerController::convertOwnerToJson).collect(Collectors.joining(","));
        return "[" + result + "]";
    }

    private static String convertOwnerToJson(Owner owner) {
        StringBuilder result = new StringBuilder();
        JSONWriter jsonWriter = new JSONWriter(result);
        jsonWriter.object();
        jsonWriter.key("id").value(owner.getId());
        jsonWriter.key("firstName").value(owner.getFirstName());
        jsonWriter.key("lastName").value(owner.getLastName());
        jsonWriter.key("address").value(owner.getAddress());
        jsonWriter.key("city").value(owner.getCity());
        jsonWriter.key("telephone").value(owner.getTelephone());
        jsonWriter.key("pets").array();
        for (Pet pet : owner.getPets()) {
            jsonWriter.object()
                    .key("name").value(pet.getName())
                    .key("birthDate").value(pet.getBirthDate())
                    .key("type").value(pet.getPetType().getName())
                    .endObject();
        }
        jsonWriter.endArray();
        jsonWriter.endObject();
        return result.toString();
    }

    public String getOwnerById(Integer id) {
        Owner owner = ownerService.getOwnerById(id);
        if (owner == null) {
            return null;
        }

        counter++;
        owner.setCity("Request Count: " + counter);
        return convertOwnerToJson(owner);
    }

    public String saveOwner(int ownerId, JSONObject data) {
        Owner owner = ownerService.getOwnerById(ownerId);
        if (owner == null) {
            throw new RuntimeException("Owner 1 not found");
        }

        owner.setFirstName(data.getString("firstName"));
        owner.setLastName(data.getString("lastName"));
        owner.setAddress(data.getString("address"));
        owner.setCity(data.getString("city"));
        owner.setTelephone(data.getString("telephone"));
        ownerService.saveOwner(owner);

        return convertOwnerToJson(owner);
    }

    public Owner newOwner(JSONObject data) {
        String firstName = data.getString("firstName");
        if (firstName == null || firstName.isBlank()) {
            firstName = defaultFirstName;
        }

        Owner newOwner = new Owner();
        newOwner.setFirstName(firstName);
        newOwner.setLastName(data.getString("lastName"));
        newOwner.setAddress(data.getString("address"));
        newOwner.setCity(data.getString("city"));
        newOwner.setTelephone(data.getString("telephone"));
        ownerService.create(newOwner);
        return newOwner;
    }
}
