package in.shivam.retaillite.auth.service.impl;

import in.shivam.retaillite.user.entity.User;
import in.shivam.retaillite.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserSecurityService {
    private final UserRepository userRepository;
    public User fetchUserByUsername(String username){

        return userRepository.findByUsername(username)
                .orElseThrow(()->new UsernameNotFoundException("User not found for the username: "+username));
    }
}
