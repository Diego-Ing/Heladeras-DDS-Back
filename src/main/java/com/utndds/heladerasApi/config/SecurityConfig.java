package com.utndds.heladerasApi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.http.HttpMethod;

import java.io.IOException;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${okta.oauth2.issuer}")
    private String issuer;
    @Value("${okta.oauth2.client-id}")
    private String clientId;

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
                .csrf((csrf) -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Usamos la configuración CORS
                                                                                   // personalizada
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/notificacion/**").permitAll()
                        .requestMatchers("/api/storage/**").permitAll()
                        .requestMatchers("/roles/**").permitAll()
                        .requestMatchers("/callback").permitAll()
                        .requestMatchers("/validar-contraseña").permitAll()
                        .requestMatchers("/tecnicos/**").hasAnyAuthority("SCOPE_ROLE_ADMIN")
                        .requestMatchers("/reportes/**").hasAnyAuthority("SCOPE_ROLE_ADMIN")
                        .requestMatchers("/suscripciones/**").hasAnyAuthority("SCOPE_ROLE_COLLABORATOR")
                        .requestMatchers(HttpMethod.GET, "/ubicaciones-googlemaps").permitAll()
                        .requestMatchers(HttpMethod.POST, "/heladeras/recomendarPuntos")
                        .hasAuthority("SCOPE_ROLE_COLLABORATOR")
                        .requestMatchers(HttpMethod.GET, "/mockAPI/recomendarPuntos").permitAll()
                        .requestMatchers(HttpMethod.GET, "/colaboraciones/recomendaciones-colaboradores").permitAll()
                        .requestMatchers("/aperturas/**").hasAnyAuthority("SCOPE_ROLE_COLLABORATOR", "SCOPE_ROLE_ADMIN")
                        .requestMatchers("/canjes/**").hasAnyAuthority("SCOPE_ROLE_COLLABORATOR", "SCOPE_ROLE_ADMIN")
                        .requestMatchers("/cargaCSV/**").hasAnyAuthority("SCOPE_ROLE_ADMIN")
                        .requestMatchers("/colaboraciones/**").hasAnyAuthority("SCOPE_ROLE_COLLABORATOR")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder())))
                .logout(logout -> logout
                        .addLogoutHandler(logoutHandler()));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("https://heladeras-dds.vercel.app/"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public NimbusJwtDecoder jwtDecoder() {
        return JwtDecoders.fromOidcIssuerLocation(issuer);
    }

    private LogoutHandler logoutHandler() {
        return (request, response, authentication) -> {
            try {
                String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
                response.sendRedirect(issuer + "v2/logout?client_id=" + clientId + "&returnTo=" + baseUrl);
            } catch (IOException e) {
                // Muestra detalles del error para depuración
                e.printStackTrace();
                throw new RuntimeException("Error during logout redirect", e);
            }
        };
    }
}
