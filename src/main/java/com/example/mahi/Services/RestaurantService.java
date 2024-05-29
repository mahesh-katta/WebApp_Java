package com.example.mahi.Services;

import com.example.mahi.models.Restaurant;
import com.example.mahi.repositories.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class RestaurantService {

    @Autowired
    private RestaurantRepository restaurantRepository;

    private static final Logger LOGGER = Logger.getLogger(RestaurantService.class.getName());

    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll();
    }

    public Optional<Restaurant> getRestaurantById(String id) {
        return restaurantRepository.findById(id);
    }

    public List<Restaurant> getRestaurantsByName(String name) {
        return restaurantRepository.findByName(name);
    }

    public List<Restaurant> getRestaurantsByLocation(double latitude, double longitude) {
        return restaurantRepository.findByLocationLatitudeAndLocationLongitude(latitude, longitude);
    }

    public List<Restaurant> getRestaurantsByPincode(String pincode) {
        return restaurantRepository.findByPincode(pincode);
    }

    public List<Restaurant> getRestaurantsByPlace(String place) {
        List<Restaurant> restaurants = restaurantRepository.findByPlace(place);
        LOGGER.info("Fetched " + restaurants.size() + " restaurants by place: " + place);
        return restaurants;
    }

    public List<Restaurant> getRestaurantsByRating(double rating) {
        return restaurantRepository.findByRating(rating);
    }

    public Optional<Restaurant> addRestaurant(Restaurant restaurant) {
        List<Restaurant> existingRestaurants = restaurantRepository.findByName(restaurant.getName());
        for (Restaurant existingRestaurant : existingRestaurants) {
            if (existingRestaurant.getPlace().equalsIgnoreCase(restaurant.getPlace())) {
                return Optional.empty();  // Conflict: same name and place
            }
        }
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        return Optional.of(savedRestaurant);
    }
    public Restaurant updateRestaurant(Restaurant updatedRestaurant) {
        if (updatedRestaurant.getId() != null) {
            Optional<Restaurant> existingRestaurantOptional = restaurantRepository.findById(updatedRestaurant.getId());
            if (existingRestaurantOptional.isPresent()) {
                Restaurant existingRestaurant = existingRestaurantOptional.get();
                // Update fields if provided
                if (updatedRestaurant.getName() != null) {
                    existingRestaurant.setName(updatedRestaurant.getName());
                }
                if (updatedRestaurant.getAddress() != null) {
                    existingRestaurant.setAddress(updatedRestaurant.getAddress());
                }
                if (updatedRestaurant.getLocation() != null) {
                    existingRestaurant.setLocation(updatedRestaurant.getLocation());
                }
                if (updatedRestaurant.getPincode() != null) {
                    existingRestaurant.setPincode(updatedRestaurant.getPincode());
                }
                if (updatedRestaurant.getPlace() != null) {
                    existingRestaurant.setPlace(updatedRestaurant.getPlace());
                }
                if (updatedRestaurant.getRating() != 0) {
                    existingRestaurant.setRating(updatedRestaurant.getRating());
                }
                return restaurantRepository.save(existingRestaurant);
            } else {
                throw new IllegalArgumentException("Restaurant not found for the provided ID.");
            }
        } else if (updatedRestaurant.getName() != null && updatedRestaurant.getPlace() != null) {
            List<Restaurant> restaurants = restaurantRepository.findByNameAndPlace(updatedRestaurant.getName(), updatedRestaurant.getPlace());
            if (!restaurants.isEmpty()) {
                Restaurant existingRestaurant = restaurants.get(0);
                // Update fields if provided
                if (updatedRestaurant.getAddress() != null) {
                    existingRestaurant.setAddress(updatedRestaurant.getAddress());
                }
                if (updatedRestaurant.getLocation() != null) {
                    existingRestaurant.setLocation(updatedRestaurant.getLocation());
                }
                if (updatedRestaurant.getPincode() != null) {
                    existingRestaurant.setPincode(updatedRestaurant.getPincode());
                }
                if (updatedRestaurant.getRating() != 0) {
                    existingRestaurant.setRating(updatedRestaurant.getRating());
                }
                return restaurantRepository.save(existingRestaurant);
            } else {
                throw new IllegalArgumentException("Restaurant not found for the provided name and place.");
            }
        } else {
            throw new IllegalArgumentException("Either ID or name and place must be provided for updating.");
        }
    }

    public void deleteRestaurant(String id, String name, String place) {
        if (id != null) {
            restaurantRepository.deleteById(id);
        } else if (name != null && place != null) {
            Optional<String> restaurantId = findIdByDetails(name, place, null);
            if (restaurantId.isPresent()) {
                restaurantRepository.deleteById(restaurantId.get());
            } else {
                throw new IllegalArgumentException("Restaurant not found for the provided name and place.");
            }
        } else {
            throw new IllegalArgumentException("Provide either ID or both name and place for deletion.");
        }
    }




    public List<Restaurant> searchRestaurants(String id, String name, Double latitude, Double longitude, String pincode, String place, Double rating) {
        List<Restaurant> results = new ArrayList<>();

        if (id != null) {
            Optional<Restaurant> restaurant = getRestaurantById(id);
            restaurant.ifPresent(results::add);
        }
        if (name != null) {
            results.addAll(getRestaurantsByName(name));
        }
        if (latitude != null && longitude != null) {
            results.addAll(getRestaurantsByLocation(latitude, longitude));
        }
        if (pincode != null) {
            results.addAll(getRestaurantsByPincode(pincode));
        }
        if (place != null) {
            LOGGER.info("Searching restaurants by place: " + place);
            List<Restaurant> placeResults = getRestaurantsByPlace(place);
            LOGGER.info("Found " + placeResults.size() + " restaurants by place.");
            results.addAll(placeResults);
        }
        if (rating != null) {
            results.addAll(getRestaurantsByRating(rating));
        }

        return results;
    }

    public Optional<String> findIdByDetails(String name, String place, String pincode) {
        List<Restaurant> restaurants = new ArrayList<>();
        if (name != null) {
            restaurants = restaurantRepository.findByName(name);
        } else if (place != null) {
            restaurants = restaurantRepository.findByPlace(place);
        } else if (pincode != null) {
            restaurants = restaurantRepository.findByPincode(pincode);
        }

        if (!restaurants.isEmpty()) {
            return Optional.of(restaurants.get(0).getId());
        } else {
            return Optional.empty();
        }
    }
}
