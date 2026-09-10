package com.bezkoder.spring_boot_jpa_postgresql.model;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tutorials")
public class Tutorial {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    @JsonBackReference("author-tutorials")
    private Author author;

    @ManyToMany
    @JoinTable(name = "tutorial_courses", joinColumns = @JoinColumn(name = "tutorial_id"), inverseJoinColumns = @JoinColumn(name = "course_id"))
    private Set<Course> courses = new HashSet<>();

    @OneToOne(mappedBy = "tutorial", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("tutorial-detail")
    private TutorialDetail detail;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "published")
    private boolean published;

    public Tutorial() {

    }

    public Tutorial(String title, String description, boolean published) {
        this.title = title;
        this.description = description;
        this.published = published;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        if (this.author == author) {
            return;
        }
        if (this.author != null) {
            this.author.getTutorials().remove(this);
        }
        this.author = author;
        if (author != null) {
            author.getTutorials().add(this);
        }
    }

    public Set<Course> getCourses() {
        return courses;
    }

    public void addCourse(Course course) {
        if (courses.add(course)) {
            course.getTutorials().add(this);
        }
    }

    public void removeCourse(Course course) {
        if (courses.remove(course)) {
            course.getTutorials().remove(this);
        }
    }

    public TutorialDetail getDetail() {
        return detail;
    }

    public void setDetail(TutorialDetail detail) {
        if (this.detail == detail) {
            return;
        }
        if (this.detail != null) {
            this.detail.setTutorial(null);
        }
        this.detail = detail;
        if (detail != null && detail.getTutorial() != this) {
            detail.setTutorial(this);
        }
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean isPublished) {
        this.published = isPublished;
    }

    @Override
    public String toString() {
        return "Tutorial [id=" + id + ", title=" + title + ", desc=" + description + ", published=" + published + "]";
    }
}
