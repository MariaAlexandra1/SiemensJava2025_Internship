package com.siemens.internship;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
    The Item entity represents a domain object that can be stored in database or memory.
    It uses JPA annotations to define the entity and its properties and Jakarta Validation for input constraints.
*/

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    // Ensure name is not blank with @NotBlank annotation
    @NotBlank(message = "Name is mandatory")
    private String name;
    // The description and status fields can have annotations for validation as well, but it was not specified in project's requirements
    private String description;
    private String status;

    // Add email regex validation
    // Even if we have an ItemValidator that use regex to validate the email
    // We can do almost the same thing with @Email annotation and specific dependency in pom.xml
    @Email(message = "Email should be valid")
    private String email;
}
