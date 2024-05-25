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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
        String email = formData.getEmail();
        String username = formData.getUsername();
        String phone = formData.getPhone();

        String usernameRegex = "^[a-zA-Z0-9._]{1,30}$";
        if (!username.matches(usernameRegex)) {
            model.addAttribute("error", "Username must be 1-30 characters long and can only contain letters, numbers, periods, and underscores.");
            model.addAttribute("formData", formData);
            return "register";
        }

        if (userService.findByUsername(username).isPresent() || tempUserService.findByUsername(username).isPresent()) {
            model.addAttribute("error", "Username taken");
            model.addAttribute("formData", formData);
            return "register";
        }

        if (userService.findByEmail(email).isPresent() || tempUserService.findByEmail(email).isPresent()) {
            model.addAttribute("error", "User already exists with this email.");
            model.addAttribute("formData", formData);
            return "register";
        }

        if (userService.findByPhone(phone).isPresent() || tempUserService.findByPhone(phone).isPresent()) {
            model.addAttribute("error", "User already exists with this phone number.");
            model.addAttribute("formData", formData);
            return "register";
        }

        String passphrase = generatePassphrase();
        System.out.println("Generated passphrase for " + email + ": " + passphrase);

        formData.setPassphrase(passphrase);
        tempUserService.save(formData);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("your-email@example.com");
        message.setTo(email);
        message.setSubject("Your Passphrase for Registration");
        message.setText("Your passphrase for registration is: " + passphrase);
        mailSender.send(message);

        return "redirect:/verify?email=" + email;
    }

    @GetMapping("/verify")
    public String showVerifyForm(@RequestParam String email, Model model) {
        model.addAttribute("email", email);
        return "verify";
    }
    @PostMapping("/verify")
    public String handleVerify(@RequestParam String email, @RequestParam String passphrase, HttpServletRequest request, Model model) {
        System.out.println("Handling verify for email: " + email + " with passphrase: " + passphrase);

        HttpSession session = request.getSession();
        Optional<TempUser> tempUserOpt = tempUserService.findByEmail(email);

        if (tempUserOpt.isPresent() && tempUserOpt.get().getPassphrase().equals(passphrase)) {
            session.setAttribute("email", email);
            session.setAttribute("username", tempUserOpt.get().getUsername());
            return "redirect:/setpassword";
        } else {
            model.addAttribute("email", email);
            model.addAttribute("error", "Invalid passphrase");
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
        String passwordRegex = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*]).{8,12}$";
        if (!password.matches(passwordRegex)) {
            model.addAttribute("email", email);
            model.addAttribute("error", "Password must be 8-12 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character.");
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
            model.addAttribute("email", email);
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

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            if (encoder.matches(password, user.getPassword())) {
                session.setAttribute("user", user);
                return "redirect:/dashboard";
            } else {
                model.addAttribute("error", "Invalid password");
                model.addAttribute("username", username);
                return "login";
            }
        } else {
            model.addAttribute("error", "User not registered");
            model.addAttribute("username", username);
            return "login";
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

    private String generatePassphrase() {
        SecureRandom random = new SecureRandom();
        return Long.toString(Math.abs(random.nextLong()), 36).substring(0, 8);
    }
}