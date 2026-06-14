package in.shivam.retaillite.auth.service.impl;

import in.shivam.retaillite.user.entity.User;


import lombok.RequiredArgsConstructor;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
@RequiredArgsConstructor
@Service
public class AppUserDetailsService implements UserDetailsService {


    private final UserSecurityService userSecurityService;
    @Override
    public UserDetails loadUserByUsername( String username) throws UsernameNotFoundException {
        User user=userSecurityService.fetchUserByUsername(username);
        return  org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .disabled(!user.isEnable())
                .authorities(Collections.singleton(new SimpleGrantedAuthority(user.getRole().name())))
                .build();
//        return new org.springframework.security.core.userdetails.User(user.getUsername(),user.getPassword(), Collections.singleton(new SimpleGrantedAuthority(user.getRole().name())));
    }
}
