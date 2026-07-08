package in.shivam.retaillite.auth.filter;

import in.shivam.retaillite.auth.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import  org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.security.DigestException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class JwtRequestFilterTest {
    @Mock private JwtService jwtService;
    @Mock private HandlerExceptionResolver handlerExceptionResolver;
    @Mock private UserDetailsService userDetailsService;
    @InjectMocks private JwtRequestFilter jwtRequestFilter;
    @Mock private FilterChain filterChain;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setup(){
        request=new MockHttpServletRequest();
        response =new MockHttpServletResponse();
//        request.setContextPath("/api/v1.0/");
        SecurityContextHolder.clearContext();
    }


    @Test
    void shouldContinueFilterChain_WhenAuthorizationHeaderIsMissing() throws ServletException, IOException {
        jwtRequestFilter.doFilter(request,response,filterChain);
        Mockito.verify(filterChain,Mockito.times(1)).doFilter(request,response);
        Mockito.verifyNoInteractions(jwtService);
        Assertions.assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldContinueFilterChain_WhenJwtTokenIsInvalid() throws ServletException, IOException {
        request.addHeader(HttpHeaders.AUTHORIZATION,"Bearer Invalid jwt token");
        Mockito.when(jwtService.extractUsername("Invalid jwt token")).thenThrow(JwtException.class);
        jwtRequestFilter.doFilterInternal(request,response,filterChain);
//        Mockito.verifyNoInteractions(jwtService);
        Mockito.verify(handlerExceptionResolver)
                .resolveException(
                        eq(request),
                        eq(response),
                        Mockito.isNull(),
                        Mockito.any(JwtException.class)
                );
        verify(filterChain, never())
                .doFilter(any(), any());
        Assertions.assertNull(
                SecurityContextHolder.getContext()
                        .getAuthentication()
        );

    }


    @Test
    void shouldAuthenticateUser_WhenTokenIsValid() throws ServletException, IOException {
        String token="valid jwt token";
        String username="shivam";
        request.addHeader("Authorization","Bearer "+token);

        Mockito.when(jwtService.extractUsername(token)).thenReturn(username);
        Mockito.when(jwtService.isTokenValid(token)).thenReturn(true);
        UserDetails userDetails=Mockito.mock(UserDetails.class);
        Mockito.when(userDetails.isEnabled()).thenReturn(true);
        Mockito.when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        jwtRequestFilter.doFilterInternal(request,response,filterChain);
        Mockito.verify(filterChain).doFilter(request,response);

        assertNotNull(
                SecurityContextHolder.getContext().getAuthentication()
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("shivam",null)
        );
        assertEquals(
                username,
                SecurityContextHolder.getContext().getAuthentication().getName()
        );
    }



    @Test
    void shouldClearContextAndResolveException_ExpiredJwtException() throws ServletException, IOException {
        String authToken="expired_token";
        request.addHeader("Authorization","Bearer "+authToken);
        Mockito.when(jwtService.extractUsername(authToken)).thenThrow(ExpiredJwtException.class);
        jwtRequestFilter.doFilterInternal(request,response,filterChain);
        Mockito.verify(handlerExceptionResolver)
                .resolveException(
                        eq(request),
                        eq(response),
                        isNull(),
                        any(ExpiredJwtException.class)
                );
        Assertions.assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldClearContextAndResolveException_DisabledException() throws ServletException, IOException {
        request.addHeader(HttpHeaders.AUTHORIZATION,"Bearer valid token...");
        when(jwtService.extractUsername("valid token...")).thenReturn("shivam");
        when(jwtService.isTokenValid("valid token...")).thenReturn(true);
        UserDetails userDetails=mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername("shivam")).thenReturn(userDetails);
        when(userDetails.isEnabled()).thenReturn(false);
        jwtRequestFilter.doFilterInternal(request,response,filterChain);
        handlerExceptionResolver.resolveException(
                eq(request),
                eq(response),
                isNull(),
                any(DigestException.class)
        );
        assertNull(SecurityContextHolder.getContext().getAuthentication());

    }
    @Test
    void shouldContinueFilter_WhenTokenIsNotValid() throws ServletException, IOException {
        request.addHeader(HttpHeaders.AUTHORIZATION,"Bearer invalid token....");
        when(jwtService.extractUsername("invalid token....")).thenReturn("shivam");
        when(jwtService.isTokenValid("invalid token....")).thenReturn(false);
        jwtRequestFilter.doFilterInternal(request,response,filterChain);
        verify(filterChain).doFilter(
                request,response
        );
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldContinueFilter_WhenAuthenticationExistsInSecurityContext() throws ServletException, IOException {
        Authentication existingAuth=new UsernamePasswordAuthenticationToken("existing",null);
        SecurityContextHolder.getContext().setAuthentication(existingAuth);
        request.addHeader(HttpHeaders.AUTHORIZATION,"Bearer valid token....");
        when(jwtService.extractUsername("valid token....")).thenReturn("shivam");
        jwtRequestFilter.doFilterInternal(request,response,filterChain);
        verify(filterChain).doFilter(
                request,response
        );
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());

    }

    @BeforeEach
    void cleanUp(){
        SecurityContextHolder.clearContext();
    }
}
