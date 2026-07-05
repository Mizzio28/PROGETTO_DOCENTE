package it.uniroma3.siw.torneocalcio.controller;

import it.uniroma3.siw.torneocalcio.model.User;
import it.uniroma3.siw.torneocalcio.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") User user,
                           BindingResult result,
                           Model model) {
        if (userService.usernameExists(user.getUsername())) {
            model.addAttribute("usernameError", "Username già in uso");
            return "auth/register";
        }
        if (result.hasErrors()) return "auth/register";

        userService.registerUser(user);
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "auth/login";
    }

    @GetMapping("/success")
    public String loginSuccess(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByUsername(username);
        if (user != null) model.addAttribute("user", user);
        model.addAttribute("username", username);
        return "auth/success";
    }
}