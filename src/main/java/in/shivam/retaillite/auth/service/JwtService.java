package in.shivam.retaillite.auth.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public interface JwtService {
    String generateToken(UserDetails userDetails);
    String extractUsername(String token);
    //todo: if the jwt is stateless then why we need to look for db for every request
    //todo: instead of this look for signature is not tempered and check for the expiration time
    boolean isTokenValid(String jwt);

    List<String> extractRoles(String token);
//    todo:if signature is not tempered and is not expired return true else return false
//    boolean isTokenValid(String jwt);
//    Collection<GrantedAuthority> extractRole(String jwt);
}
