package it.uniroma3.siw.torneocalcio.config;

import it.uniroma3.siw.torneocalcio.model.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    protected SecurityFilterChain configure(final HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeHttpRequests(authorize -> {
            // Risorse pubbliche
            authorize.requestMatchers(HttpMethod.GET,
                "/", "/index", "/register", "/login",
                "/tornei", "/tornei/**",
                "/squadre", "/squadre/**",
                "/analisi",
                "/css/**", "/images/**", "/react/**", "/favicon.ico"
            ).permitAll();
            authorize.requestMatchers(HttpMethod.POST, "/register", "/login").permitAll();

            // Solo ADMIN
            authorize.requestMatchers(HttpMethod.GET, "/admin/**")
                .hasAnyAuthority(User.ADMIN_ROLE);
            authorize.requestMatchers(HttpMethod.POST, "/admin/**")
                .hasAnyAuthority(User.ADMIN_ROLE);

            // Qualsiasi altro: autenticato (per commenti)
            authorize.anyRequest().authenticated();
        });

        httpSecurity.formLogin(form -> {
            form.loginPage("/login").permitAll();
            form.defaultSuccessUrl("/success", true);
            form.failureUrl("/login?error=true");
        });

        httpSecurity.logout(logout -> {
            logout.logoutUrl("/logout");
            logout.logoutSuccessUrl("/");
            logout.invalidateHttpSession(true);
            logout.deleteCookies("JSESSIONID");
            logout.clearAuthentication(true);
            logout.permitAll();
        });

        return httpSecurity.build();
    }
}