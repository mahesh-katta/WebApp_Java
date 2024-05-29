package com.example.mahi.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "restaurants")
public class Restaurant {

    @Id
    private String id;
    private String name;
    private String address;
    private Location location;
    private String pincode;
    private String place;
    private Double rating; // Changed from double to Double

    // Constructors
    public Restaurant() {}

    public Restaurant(String name, String address, Location location, String pincode, String place, Double rating) {
        this.name = name;
        this.address = address;
        this.location = location;
        this.pincode = pincode;
        this.place = place;
        this.rating = rating;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public Double getRating() { // Changed from double to Double
        return rating;
    }

    public void setRating(Double rating) { // Changed from double to Double
        this.rating = rating;
    }

    // Nested Location Class
    public static class Location {
        private Double latitude; // Changed from double to Double
        private Double longitude; // Changed from double to Double

        // Constructors
        public Location() {}

        public Location(Double latitude, Double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        // Getters and Setters
        public Double getLatitude() {
            return latitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }
    }
}
