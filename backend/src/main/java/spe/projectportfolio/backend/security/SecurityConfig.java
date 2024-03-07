package spe.projectportfolio.backend.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                //.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(requests -> requests

                        // allow all react files to be accessed without logging in
                        .requestMatchers("/index.html", "/static/**", "/*.png", "/*.ico", ".json", "/*.txt").permitAll()

                        // allow all swagger files to be accessed without logging in
                        .requestMatchers(HttpMethod.GET, "/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // allow all pages to be accessed without logging in
                        // (restricting access to pages is done on the frontend)
                        .requestMatchers(HttpMethod.GET, "/", "/register", "/home", "/project", "/editor",
                                "/project-editor", "/admin","/emailVerification","/change-password").permitAll()

                        // allow anyone to send email
                        .requestMatchers(HttpMethod.POST, "/verify/send-email").permitAll()

                        // allow anyone to verify code
                        .requestMatchers(HttpMethod.POST, "/verify/verify-code").permitAll()

                        // allow anyone to reset password
                        .requestMatchers(HttpMethod.POST, "/auth/reset-password").permitAll()

                        // allow anyone to create an account
                        .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()

                        // allow anyone to request their user details (it will return null for users who are not logged in)
                        .requestMatchers(HttpMethod.GET, "/api/users/current").permitAll()

                        // allow anyone to check if an email exists
                        .requestMatchers(HttpMethod.GET, "/api/users/exists").permitAll()

                        // restrict certain user API endpoints to logged-in users with ADMIN role
                        .requestMatchers(HttpMethod.GET,    "/api/users", "/api/users/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST,   "/api/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/*").hasRole("ADMIN")

                        // restrict certain case study API endpoints to logged-in users with EDITOR role
                        .requestMatchers(HttpMethod.POST,   "/api/case-studies", "/api/images").hasRole("EDITOR")
                        .requestMatchers(HttpMethod.PUT,    "/api/case-studies", "/api/images/*").hasRole("EDITOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/case-studies/*", "/api/images/*").hasRole("EDITOR")

                        // restrict all other pages and API endpoints to any logged-in user
                        .anyRequest().authenticated()
                )

                // return 401 if user not logged in instead of redirecting to login page
                .exceptionHandling(customizer -> {
                    customizer.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));
                })

                .formLogin(form -> form
                        .loginPage("/")
                        .loginProcessingUrl("/auth/login")
                        .successHandler(((request, response, authentication) -> {
                            response.setStatus(HttpServletResponse.SC_OK);
                        }))
                        .failureHandler(((request, response, exception) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        })))

                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessHandler(((request, response, authentication) -> {
                            response.setStatus(HttpServletResponse.SC_OK);
                        }))
                        .deleteCookies("JSESSIONID"));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
        hierarchy.setHierarchy(
                "ROLE_ADMIN > ROLE_EDITOR\n" +
                "ROLE_EDITOR > ROLE_USER"
        );
        return hierarchy;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public FilterRegistrationBean corsFilter() {
        // create config
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:3000");
        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(List.of("*"));

        // create source
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        // create bean
        FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
        bean.setOrder(-102);

        return bean;
    }

//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        // create config
//        CorsConfiguration config = new CorsConfiguration();
//        config.setAllowCredentials(true);
//        config.addAllowedOrigin("http://localhost:3000");
//        config.setAllowedHeaders(List.of("*"));
//        config.setAllowedMethods(List.of("*"));
//
//        // create source
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", config);
//
//        return source;
//    }
}
