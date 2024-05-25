package com.example.mahi.models;
public class TempUser {

    private String id;
    private String email;
    private String username;
    private String phone;
    private String passphrase;

    public TempUser() {
        // Default constructor
    }

    public TempUser(String email, String username, String phone, String passphrase) {
        this.email = email;
        this.username = username;
        this.phone = phone;
        this.passphrase = passphrase;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassphrase() {
        return passphrase;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    @Override
    public String toString() {
        return "TempUser{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", phone='" + phone + '\'' +
                ", passphrase='" + passphrase + '\'' +
                '}';
    }
}
