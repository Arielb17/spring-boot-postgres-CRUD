package com.bezkoder.spring_boot_jpa_postgresql.model;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "authors")
public class Author {

    @OneToMany(mappedBy = "author")
    @JsonManagedReference("author-tutorials")
    private Set<Tutorial> tutorials = new HashSet<>();

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    public Author() {
    }

    public Author(String name) {
        this.name = name;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Set<Tutorial> getTutorials() { return tutorials; }

    public void addTutorial(Tutorial tutorial) {
        if (!tutorials.contains(tutorial)) {
            tutorials.add(tutorial);
            tutorial.setAuthor(this);
        }
    }
}
