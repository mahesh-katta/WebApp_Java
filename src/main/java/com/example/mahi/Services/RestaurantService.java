package com.example.mahi.Services;

import com.example.mahi.models.Restaurant;
import com.example.mahi.repositories.RestaurantRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class RestaurantService {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @PostConstruct
    public void init() {
        List<Restaurant> restaurants = Arrays.asList(
                new Restaurant("Gismath Jail Mandi", "Dilsuknagar", new Restaurant.Location(17.36829441406888, 78.53090196297957)));

        restaurantRepository.saveAll(restaurants);
    }
    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll();
    }
}
