package it.uniroma3.siw.torneocalcio.controller;

import it.uniroma3.siw.torneocalcio.model.Credentials;
import it.uniroma3.siw.torneocalcio.model.User;
import it.uniroma3.siw.torneocalcio.service.CredentialsService;
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
    private CredentialsService credentialsService;

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("credentials", new Credentials());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") User user,
                           BindingResult userResult,
                           @Valid @ModelAttribute("credentials") Credentials credentials,
                           BindingResult credResult,
                           Model model) {
        if (credentialsService.usernameExists(credentials.getUsername())) {
            model.addAttribute("usernameError", "Username già in uso");
            return "auth/register";
        }
        if (userResult.hasErrors() || credResult.hasErrors()) return "auth/register";

        credentials.setUser(user);
        credentialsService.saveCredentials(credentials);
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "auth/login";
    }

    @GetMapping("/success")
    public String loginSuccess(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Credentials creds = credentialsService.getCredentials(username);
        if (creds != null && creds.getUser() != null)
            model.addAttribute("user", creds.getUser());
        model.addAttribute("username", username);
        return "auth/success";
    }
}
