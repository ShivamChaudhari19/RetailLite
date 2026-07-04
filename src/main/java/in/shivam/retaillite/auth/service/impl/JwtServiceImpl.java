package in.shivam.retaillite.auth.service.impl;

import in.shivam.retaillite.auth.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JwtServiceImpl implements JwtService {
    private final SecretKey key;
    private static final long EXPIRATION=1000*60*60;
    public JwtServiceImpl(
            @Value("${app.security.jwt-secret-key}") String SECRET_KEY
    ){
        String SECRET_KEY="lkskdksifkdioalsiseewqesdegsdgss";
        this.key=Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
        this.EXPIRATION=EXPIRATION*60*1000L;
    }
    @Override
    public String generateToken(UserDetails userDetails) {
        return createToken(userDetails);
    }
    private String createToken(UserDetails userDetails){
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("role",
                        userDetails
                                .getAuthorities()
                                .stream()
                                .map(GrantedAuthority::getAuthority)
                                .toList())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis()+EXPIRATION))
                .signWith(key)
                .compact();
    }
    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public boolean isTokenValid(String jwt) {
        return !isExpired(jwt);
    }
    @Override
    public List<String> extractRoles(String token) {
        List<?> o = extractClaim(token, claims -> claims.get("role", List.class));
        if (o != null) {
            return o.stream()
                    .map(Object::toString)
                    .toList();
        }
        return Collections.emptyList();
    }
    public <T>T extractClaim(String jwt, Function<Claims,T>extractClaim){
        Claims allClaims=extractAllClaims(jwt);
        return extractClaim.apply(allClaims);
    }
    // TODO: Optimize JWT processing by extracting Claims once per request
    // instead of parsing the token multiple times in extractUsername()
    // and isTokenValid() to reduce redundant JWT parsing overhead.
    private Claims extractAllClaims(String token){
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

    }
    private boolean isExpired(String token){
        return extractAllClaims(token)
                .getExpiration()
                .before(new Date());
    }
}
