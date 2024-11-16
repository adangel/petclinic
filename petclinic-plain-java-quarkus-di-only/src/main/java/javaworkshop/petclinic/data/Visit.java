package javaworkshop.petclinic.data;

import java.time.LocalDate;

public class Visit extends BaseEntity {
    private LocalDate date;
    private String description;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Visit{" +
                "id=" + getId() +
                ", date=" + date +
                ", description='" + description + '\'' +
                '}';
    }
}
