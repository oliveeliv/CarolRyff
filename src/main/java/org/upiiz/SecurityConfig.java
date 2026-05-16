package org.upiiz;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // 1. LO PÚBLICO (Solo lo mínimo indispensable para que el alumno conteste)
                        .requestMatchers("/", "/evaluacion/**", "/css/**", "/js/**", "/images/**", "/error").permitAll()
                        .requestMatchers("/login").permitAll()

                        // 2. LO PRIVADO (Cualquier ruta dentro de /resultados requiere LOGIN)
                        // Esto incluye la lista, los detalles y la gráfica grupal.
                        .requestMatchers("/resultados/**").authenticated()
                        .requestMatchers("/resultados").authenticated()

                        // 3. BLOQUEO RESIDUAL
                        // Cualquier otra ruta que se te haya olvidado o intentes crear después
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/resultados", true) // Al loguearte, vas directo a la gestión
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/") // Al salir, regresas al inicio público
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .csrf(csrf -> csrf.disable()); // Necesario para que el POST de la encuesta funcione desde Aiven

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        UserDetails admin = User.builder()
                .username("admin")
                .password(encoder.encode("ryff2024"))
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}