package in.shivam.retaillite.auth.filter;

import in.shivam.retaillite.auth.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

//@RequiredArgsConstructor
@Component

public class JwtRequestFilter extends OncePerRequestFilter {
    private static final String BEARER_PREFIX="Bearer ";
    private static final Logger log= LoggerFactory.getLogger(JwtRequestFilter.class);

    @Qualifier("handlerExceptionResolver")
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;


    public JwtRequestFilter(
            JwtService jwtService,
            UserDetailsService userDetailsService,
            HandlerExceptionResolver handlerExceptionResolver
    ) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
            try {
                final String authToken = extractToken(request);
                if(authToken == null){
                    filterChain.doFilter(request,response);
                    return;
                }
                String username = jwtService.extractUsername(authToken);

                if (username != null
                        && SecurityContextHolder.getContext().getAuthentication() == null
                ) {

                    if (jwtService.isTokenValid(authToken)) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        if (!userDetails.isEnabled()){

                            log.info("disabled user is trying to authenticate");
                            throw new DisabledException(
                                    "Account has been disabled. Contact admin for help"
                            );
                        }
                        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    }
                }
                log.debug("authentication completed.");
                filterChain.doFilter(request, response);
                return;

            }catch (ExpiredJwtException exception){
                SecurityContextHolder.clearContext();
                log.warn("Jwt token expired {}",exception.getMessage());
//                response.sendError(HttpServletResponse.SC_UNAUTHORIZED,"JWT Expired");
                handlerExceptionResolver.resolveException(request,response,null,exception);
            }catch (JwtException exception){
                SecurityContextHolder.clearContext();
                log.warn("Jwt processing failed {}",exception.getMessage());
//                response.sendError(HttpServletResponse.SC_UNAUTHORIZED,"Invalid Jwt");
                handlerExceptionResolver.resolveException(request,response,null,exception);
            }catch (DisabledException exception){
                handlerExceptionResolver.resolveException(request,response,null,exception);
            }
    }

    private String extractToken(HttpServletRequest request){
        String authToken =request.getHeader(HttpHeaders.AUTHORIZATION);
        if(authToken ==null||
                !authToken.startsWith(BEARER_PREFIX)){

            return null;
        }
        return authToken.substring(BEARER_PREFIX.length());
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path=request.getServletPath();
        return path.startsWith("/auth/");
    }
}
