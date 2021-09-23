package com.cloud.vaccinewallet.controller;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.cloud.vaccinewallet.beans.User;
import com.cloud.vaccinewallet.repositories.UserRepository;
import com.cloud.vaccinewallet.repositories.RoleRepository;
import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
public class HomeController {

    private UserRepository userRepository;
    private RoleRepository roleRepository;

    private String encodePassword(String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(password);
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/user")
    public String userIndex(Model model, Authentication authentication) {

        String name = authentication.getName();

        List<String> roles = new ArrayList<String>();

        for (GrantedAuthority ga: authentication.getAuthorities()) {
            roles.add(ga.getAuthority());
        }
        model.addAttribute("name", name);
        model.addAttribute("roles", roles);

        return "user/index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "/error/access-denied";
    }

    @GetMapping("/register")
    public String goRegistration() {
        return "register";
    }

    @PostMapping("/register")
    public String doRegistration(Model model,@RequestParam String username, @RequestParam String password) {


        User user= new User(username, encodePassword(password), Byte.valueOf("1"));
        user.getRoles().add(roleRepository.findByRolename("ROLE_USER"));

        userRepository.save(user);
        model.addAttribute("userList",userRepository.findAll());
        return "redirect:/";
    }
}
