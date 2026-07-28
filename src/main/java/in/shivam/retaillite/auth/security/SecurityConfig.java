package in.shivam.retaillite.auth.security;

import in.shivam.retaillite.auth.filter.JwtRequestFilter;
import in.shivam.retaillite.auth.service.JwtService;
import in.shivam.retaillite.auth.service.impl.AppUserDetailsService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final AppUserDetailsService appUserDetailsService;

    private final JwtService jwtService;
    @Qualifier("handlerExceptionResolver")
    private final HandlerExceptionResolver handlerExceptionResolver;

    public SecurityConfig(AppUserDetailsService appUserDetailsService, JwtService jwtService, HandlerExceptionResolver handlerExceptionResolver) {
        this.appUserDetailsService = appUserDetailsService;
        this.jwtService = jwtService;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(CsrfConfigurer::disable)
                .authorizeHttpRequests(authorize->authorize
                        .requestMatchers(AuthConstants.PUBLIC_URL).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtRequestFilter(), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(
                        (ex)->ex.accessDeniedHandler(
                                (
                                        (request,
                                         response,
                                         accessDeniedException
                                        ) ->{
                                            response.setStatus(HttpStatus.FORBIDDEN.value());
                                            response.getWriter().write("Access denied provide valid roles");
                                        }
                                )
                        )
                ).build();
    }

   @Bean
   public CorsFilter corsFilter(){
        return new CorsFilter(urlBasedCorsConfigurationSource());
   }
   private UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource(){
       CorsConfiguration configuration=new CorsConfiguration();
       configuration.setAllowedOrigins(List.of("http://localhost:5173"));
       configuration.setAllowedMethods(List.of("POST","GET","DELETE","PUT","PATCH"));
       configuration.setAllowCredentials(true);
       configuration.setAllowedHeaders(List.of("Authorization","Content-Type"));

       UrlBasedCorsConfigurationSource source=new UrlBasedCorsConfigurationSource();
       source.registerCorsConfiguration("/**",configuration);
       return source;
   }

   @Bean
   public PasswordEncoder passwordEncoder(){
        return  new BCryptPasswordEncoder();
   }
   @Bean
    public AuthenticationManager authenticationManager(){
       DaoAuthenticationProvider daoAuthenticationProvider=new DaoAuthenticationProvider(appUserDetailsService);
       daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(daoAuthenticationProvider);
   }

    public JwtRequestFilter jwtRequestFilter(){
        return new JwtRequestFilter(jwtService,appUserDetailsService,handlerExceptionResolver);
   }

}
