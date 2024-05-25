package com.example.mahi.Services;



import com.example.mahi.models.TempUser;
import com.example.mahi.repositories.TempUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TempUserService {

    @Autowired
    private TempUserRepository tempUserRepository;

    public Optional<TempUser> findByUsername(String username) {
        return tempUserRepository.findByUsername(username);
    }

    public Optional<TempUser> findByEmail(String email) {
        return tempUserRepository.findByEmail(email);
    }

    public Optional<TempUser> findByPhone(String phone) {
        return tempUserRepository.findByPhone(phone);
    }

    public TempUser save(TempUser tempUser) {
        return tempUserRepository.save(tempUser);
    }

    public void deleteByEmail(String email) {
        tempUserRepository.deleteByEmail(email);
    }
}
