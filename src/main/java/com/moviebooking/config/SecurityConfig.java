package com.moviebooking.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter authFilter;

    @Autowired
    private UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOriginPatterns(List.of("*"));
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(List.of("*"));
                    config.setAllowCredentials(false);
                    return config;
                }))
                .authorizeHttpRequests(auth -> auth

                        // ── ƯU TIÊN CAO NHẤT: Webhook PayOS ──────────────────────────────
                        .requestMatchers(HttpMethod.POST, "/api/payments/payos/webhook").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/payments/webhook").permitAll()

                        // ── 1. Public APIs ────────────────────────────────────────────────
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/movies/**", "/api/genres/**", "/api/cinemas/**",
                                "/api/rooms/**", "/api/showtimes/**").permitAll()

                        // ── 2. Comment & Rating ───────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET,  "/api/movies/*/comments").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/api/movies/*/rating").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/movies/*/comments").authenticated()

                        // ── 3. Sơ đồ ghế & Bảng giá ──────────────────────────────────────
                        // Public: xem giá theo rạp (dùng ở trang đặt vé)
                        .requestMatchers(HttpMethod.GET, "/api/seat-map/pricing/*").permitAll()
                        // Admin: toàn bộ CRUD pricing + xem sơ đồ ghế
                        .requestMatchers(HttpMethod.GET,    "/api/seat-map/pricing").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET,    "/api/seat-map/pricing/detail/*").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST,   "/api/seat-map/pricing").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/seat-map/pricing/*").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/seat-map/pricing/*").hasAuthority("ADMIN")
                        .requestMatchers("/api/seat-map/room/*").hasAuthority("ADMIN")

                        // ── 4. Voucher ────────────────────────────────────────────────────
                        // User: kiểm tra voucher (preview trước khi đặt)
                        .requestMatchers(HttpMethod.POST, "/api/vouchers/apply").authenticated()
                        // User: xem danh sách voucher còn hiệu lực
                        .requestMatchers(HttpMethod.GET, "/api/vouchers/active").authenticated()
                        // Admin: toàn bộ CRUD voucher
                        .requestMatchers("/api/vouchers/**").hasAuthority("ADMIN")

                        // ── 5. QR Vé ─────────────────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET,  "/api/tickets/qr/booking/*").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/tickets/qr/verify").hasAnyAuthority("ADMIN", "STAFF")

                        // ── 6. Hoàn vé ────────────────────────────────────────────────────
                        // User: gửi yêu cầu, xem lịch sử, xem chi tiết
                        .requestMatchers(HttpMethod.POST, "/api/ticket-transfers").authenticated()
                        .requestMatchers(HttpMethod.GET,  "/api/ticket-transfers/my").authenticated()
                        .requestMatchers(HttpMethod.GET,  "/api/ticket-transfers/*").authenticated()
                        // Admin: xem tất cả, duyệt, từ chối
                        .requestMatchers("/api/ticket-transfers/**").hasAuthority("ADMIN")

                        // ── 7. User Profile ───────────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET,  "/api/users/profile").authenticated()
                        .requestMatchers(HttpMethod.PUT,  "/api/users/profile").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/users/profile/change-password").authenticated()
                        // Admin: quản lý user
                        .requestMatchers("/api/users/**").hasAuthority("ADMIN")

                        // ── 8. Admin APIs ─────────────────────────────────────────────────
                        .requestMatchers(HttpMethod.POST,
                                "/api/movies/**", "/api/genres/**", "/api/cinemas/**",
                                "/api/rooms/**", "/api/seats/**", "/api/showtimes/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT,
                                "/api/movies/**", "/api/genres/**", "/api/cinemas/**",
                                "/api/rooms/**").hasAuthority("ADMIN")          // ← thêm /api/rooms/**
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/movies/**", "/api/genres/**", "/api/cinemas/**",
                                "/api/rooms/**",                                // ← thêm /api/rooms/**
                                "/api/users/**").hasAuthority("ADMIN")

                        // ── 9. Các API còn lại cần đăng nhập ─────────────────────────────
                        .requestMatchers("/api/bookings/**", "/api/payments/**", "/api/seat-locks/**").authenticated()

                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}