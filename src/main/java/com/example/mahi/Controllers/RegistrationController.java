package com.example.mahi.Controllers;

import com.example.mahi.models.TempUser;
import com.example.mahi.models.User;
import com.example.mahi.Services.TempUserService;
import com.example.mahi.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.Optional;

@Controller
public class RegistrationController {

    @Autowired
    private UserService userService;

    @Autowired
    private TempUserService tempUserService;

    @Autowired
    private JavaMailSender mailSender;

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("error", null);
        model.addAttribute("formData", new TempUser());
        return "register";
    }
    @PostMapping("/register")
    public String handleRegister(@ModelAttribute TempUser formData, Model model) {
        if (!validateUserData(formData, model)) {
            model.addAttribute("formData", formData);
            return "register";
        }

        try {
            sendVerificationEmail(formData);
            return "redirect:/verify?email=" + formData.getEmail();
        } catch (Exception e) {
            // If an error occurs, delete the temporary user data
            tempUserService.deleteByEmail(formData.getEmail());
            model.addAttribute("error", "An error occurred. Please try again later.");
            return "register";
        }
    }
    @GetMapping("/verify")
    public String showVerifyForm(@RequestParam String email, Model model) {
        model.addAttribute("email", email);
        return "verify";
    }

    @PostMapping("/verify")
    public String handleVerify(@RequestParam String email, @RequestParam String passphrase, HttpServletRequest request, Model model) {
        if (verifyPassphrase(email, passphrase, model)) {
            HttpSession session = request.getSession();
            TempUser tempUser = tempUserService.findByEmail(email).get();
            session.setAttribute("email", email);
            session.setAttribute("username", tempUser.getUsername());
            return "redirect:/setpassword";
        } else {
            model.addAttribute("email", email);
            return "verify";
        }
    }

    @GetMapping("/setpassword")
    public String showSetPasswordForm(HttpSession session, Model model) {
        String email = (String) session.getAttribute("email");
        String username = (String) session.getAttribute("username");

        if (email != null) {
            model.addAttribute("email", email);
            model.addAttribute("username", username);
            model.addAttribute("error", null);
            return "setpassword";
        } else {
            return "redirect:/register";
        }
    }

    @PostMapping("/setpassword")
    public String handleSetPassword(@RequestParam String email, @RequestParam String password, HttpSession session, Model model) {
        if (!isValidPassword(password)) {
            model.addAttribute("error", "Password must be 8-12 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character.");
            model.addAttribute("email", email);
            model.addAttribute("username", session.getAttribute("username"));
            return "setpassword";
        }

        Optional<TempUser> tempUserOpt = tempUserService.findByEmail(email);
        if (tempUserOpt.isPresent()) {
            TempUser tempUser = tempUserOpt.get();
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setUsername(tempUser.getUsername());
            newUser.setPhone(tempUser.getPhone());
            newUser.setPassword(password); // The password will be hashed in the UserService

            userService.save(newUser);
            tempUserService.deleteByEmail(email);

            return "redirect:/login";
        } else {
            model.addAttribute("error", "Internal Server Error");
            return "setpassword";
        }
    }

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("error", null);
        model.addAttribute("username", "");
        return "login";
    }

    @PostMapping("/login")
    public String handleLogin(@RequestParam String username, @RequestParam String password, HttpSession session, Model model) {
        Optional<User> userOpt = userService.findByUsername(username);
        Integer loginAttempts = (Integer) session.getAttribute("loginAttempts");
        if (loginAttempts == null) {
            loginAttempts = 0;
        }

        if (userOpt.isPresent() && new BCryptPasswordEncoder().matches(password, userOpt.get().getPassword())) {
            session.setAttribute("user", userOpt.get());
            session.setAttribute("loginAttempts", 0); // Reset login attempts on successful login
            return "redirect:/dashboard";
        } else {
            loginAttempts++;
            session.setAttribute("loginAttempts", loginAttempts);
            model.addAttribute("error", userOpt.isPresent() ? "Invalid password" : "User not registered");
        }

        if (loginAttempts >= 3) {
            model.addAttribute("showForgotLink", true);
        }

        model.addAttribute("username", username);
        return "login";
    }

    @GetMapping("/forgot")
    public String showForgotForm(Model model) {
        model.addAttribute("error", null);
        return "forgot";
    }

    @PostMapping("/forgot")
    public String handleForgot(@RequestParam String email, Model model) {
        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            sendVerificationEmail(user);
            return "redirect:/reset?email=" + email + "&username=" + user.getUsername();
        } else {
            model.addAttribute("error", "No user found with this email.");
            return "forgot";
        }
    }

    @GetMapping("/reset")
    public String showResetForm(@RequestParam String email, @RequestParam String username, Model model) {
        model.addAttribute("email", email);
        model.addAttribute("username", username);
        return "reset";
    }


    @PostMapping("/reset")
    public String handleReset(@RequestParam String email, @RequestParam String passphrase, @RequestParam String newPassword, Model model) {
        if (!isValidPassword(newPassword)) {
            model.addAttribute("error", "Password must be 8-12 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character.");
            model.addAttribute("email", email);
            model.addAttribute("username", userService.findByEmail(email).get().getUsername());
            return "reset";
        }

        try {
            if (verifyPassphrase(email, passphrase, model)) {
                User user = userService.findByEmail(email).get();
                user.setPassword(newPassword); // The password will be hashed in the UserService
                userService.save(user);
                tempUserService.deleteByEmail(email); // Clean up the temporary passphrase
                return "redirect:/login";
            } else {
                model.addAttribute("email", email);
                return "reset";
            }
        } catch (Exception e) {
            // If an error occurs, delete the temporary user data
            tempUserService.deleteByEmail(email);
            model.addAttribute("error", "An error occurred. Please try again later.");
            return "reset";
        }
    }

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            model.addAttribute("user", user);
            return "dashboard";
        } else {
            return "redirect:/login";
        }
    }

    @GetMapping("/logout")
    public String handleLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/contact")
    public String showContactPage() {
        return "contact";
    }

    @GetMapping("/")
    public String showHomePage() {
        return "home";
    }

    private boolean validateUserData(TempUser formData, Model model) {
        String email = formData.getEmail();
        String username = formData.getUsername();
        String phone = formData.getPhone();

        String usernameRegex = "^[a-zA-Z0-9._]{1,30}$";
        if (!username.matches(usernameRegex)) {
            model.addAttribute("error", "Username must be 1-30 characters long and can only contain letters, numbers, periods, and underscores.");
            return false;
        }

        if (userService.findByUsername(username).isPresent() || tempUserService.findByUsername(username).isPresent()) {
            model.addAttribute("error", "Username taken");
            return false;
        }

        if (userService.findByEmail(email).isPresent() || tempUserService.findByEmail(email).isPresent()) {
            model.addAttribute("error", "User already exists with this email.");
            return false;
        }

        if (userService.findByPhone(phone).isPresent() || tempUserService.findByPhone(phone).isPresent()) {
            model.addAttribute("error", "User already exists with this phone number.");
            return false;
        }

        return true;
    }

    private void sendVerificationEmail(TempUser formData) {
        String passphrase = generatePassphrase();
        formData.setPassphrase(passphrase);
        tempUserService.save(formData);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("your-email@example.com");
        message.setTo(formData.getEmail());
        message.setSubject("Your Passphrase for Registration");
        message.setText("Your passphrase for registration is: " + passphrase);
        mailSender.send(message);
    }

    private void sendVerificationEmail(User user) {
        String passphrase = generatePassphrase();
        TempUser tempUser = new TempUser();
        tempUser.setEmail(user.getEmail());
        tempUser.setUsername(user.getUsername());
        tempUser.setPhone(user.getPhone());
        tempUser.setPassphrase(passphrase);
        tempUserService.save(tempUser);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("your-email@example.com");
        message.setTo(user.getEmail());
        message.setSubject("Reset Your Password");
        message.setText("Your passphrase for resetting your password is: " + passphrase);
        mailSender.send(message);
    }

    private boolean verifyPassphrase(String email, String passphrase, Model model) {
        Optional<TempUser> tempUserOpt = tempUserService.findByEmail(email);
        if (tempUserOpt.isPresent() && tempUserOpt.get().getPassphrase().equals(passphrase)) {
            return true;
        } else {
            model.addAttribute("error", "Invalid passphrase");
            return false;
        }
    }

    private boolean isValidPassword(String password) {
        String passwordRegex = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*]).{8,12}$";
        return password.matches(passwordRegex);
    }

    private String generatePassphrase() {
        SecureRandom random = new SecureRandom();
        return Long.toString(Math.abs(random.nextLong()), 36).substring(0, 8);
    }
}
