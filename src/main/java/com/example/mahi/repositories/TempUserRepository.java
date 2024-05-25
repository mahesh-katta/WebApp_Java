package com.example.mahi.repositories;


import com.example.mahi.models.TempUser;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface TempUserRepository extends MongoRepository<TempUser, Long> {
    Optional<TempUser> findByUsername(String username);
    Optional<TempUser> findByEmail(String email);
    Optional<TempUser> findByPhone(String phone);
    void deleteByEmail(String email);
}