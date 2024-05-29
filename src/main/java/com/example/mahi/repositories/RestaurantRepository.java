package com.example.mahi.repositories;

import com.example.mahi.models.Restaurant;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface RestaurantRepository extends MongoRepository<Restaurant, String> {
    List<Restaurant> findByName(String name);
    List<Restaurant> findByLocationLatitudeAndLocationLongitude(double latitude, double longitude);
    List<Restaurant> findByPincode(String pincode);

    // Add case-insensitive search for 'place'
    @Query("{ 'place': { $regex: ?0, $options: 'i' } }")
    List<Restaurant> findByPlace(String place);

    List<Restaurant> findByNameAndPlace(String name,String place);

    List<Restaurant> findByRating(double rating);
}
