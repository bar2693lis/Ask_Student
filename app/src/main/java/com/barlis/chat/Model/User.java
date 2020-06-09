package com.barlis.chat.Model;

public class User {

    private String id;
    private String username;
    private String imageURL;
    private String email;

    private String status;
    private String search;

    private String facebook;
    private String instagram;
    private String github;
    private String linkedin;

    public User(String id, String username, String imageURL, String email, String status, String search, String facebook, String instagram, String github, String linkedin) {
        this.id = id;
        this.username = username;
        this.imageURL = imageURL;
        this.email = email;
        this.status = status;
        this.search = search;
        this.facebook = facebook;
        this.instagram = instagram;
        this.github = search;
        this.linkedin = linkedin;

    }

    public User() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getFacebook() {
        return facebook;
    }

    public void setFacebook(String facebook) {
        this.facebook = facebook;
    }

    public String getInstagram() {
        return instagram;
    }

    public void setInstagram(String instagram) {
        this.instagram = instagram;
    }

    public String getGithub() {
        return github;
    }

    public void setGithub(String github) {
        this.github = github;
    }

    public String getLinkedin() {
        return linkedin;
    }

    public void setLinkedin(String linkedin) {
        this.linkedin = linkedin;
    }
}
