package com.example.mahi.Controllers;

import com.example.mahi.models.Restaurant;
import com.example.mahi.Services.RestaurantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/restaurants")
public class RestaurantController {

    @Autowired
    private RestaurantService restaurantService;

    @GetMapping
    public ResponseEntity<List<Restaurant>> getRestaurants(
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) String pincode,
            @RequestParam(required = false) String place,
            @RequestParam(required = false) Double rating) {

        try {
            List<Restaurant> restaurants = restaurantService.searchRestaurants(id, name, latitude, longitude, pincode, place, rating);
            return ResponseEntity.ok(restaurants);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping
    public ResponseEntity<String> addRestaurant(@RequestBody Restaurant restaurant) {
        Optional<Restaurant> addedRestaurant = restaurantService.addRestaurant(restaurant);
        if (addedRestaurant.isPresent()) {
            return ResponseEntity.ok("Restaurant added successfully.");
        } else {
            return ResponseEntity.status(409).body("Restaurant with the same name already exists in this place.");
        }
    }

    @PatchMapping
    public ResponseEntity<String> updateRestaurant(@RequestBody Restaurant updatedRestaurant,
                                                   @RequestParam(required = false) String id,
                                                   @RequestParam(required = false) String name,
                                                   @RequestParam(required = false) String place,
                                                   @RequestParam(required = false) String pincode) {
        if ((id == null && (name == null || place == null)) || (id != null && (name != null || place != null))) {
            return ResponseEntity.status(400).body("Provide either ID or both name and place for updating.");
        }

        Optional<String> restaurantId = id != null ? Optional.of(id) :
                restaurantService.findIdByDetails(name, place, pincode);

        if (restaurantId.isPresent()) {
            updatedRestaurant.setId(restaurantId.get());
            Restaurant updated = restaurantService.updateRestaurant(updatedRestaurant);
            return ResponseEntity.ok("Restaurant updated successfully.");
        } else {
            return ResponseEntity.status(404).body("Restaurant not found for the provided details.");
        }
    }



    @DeleteMapping
    public ResponseEntity<String> deleteRestaurant(@RequestParam(required = false) String id,
                                                   @RequestParam(required = false) String name,
                                                   @RequestParam(required = false) String place,
                                                   @RequestParam(required = false) String pincode) {
        try {
            restaurantService.deleteRestaurant(id, name, place);
            return ResponseEntity.ok("Restaurant deleted successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

}
