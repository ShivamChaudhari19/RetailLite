package in.shivam.retaillite.user.service.impl;

import in.shivam.retaillite.common.exception.ResourceNotFoundException;
import in.shivam.retaillite.user.dto.UserRequest;
import in.shivam.retaillite.user.dto.UserResponse;
import in.shivam.retaillite.user.entity.User;
import in.shivam.retaillite.user.exception.UserAlreadyExists;
import in.shivam.retaillite.user.UserRepository;
import in.shivam.retaillite.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Override
    public UserResponse create(UserRequest user) {
        if(userRepository.existsByUsername(user.getUsername()))
        {
            throw new UserAlreadyExists("Username is taken");
        }else {
            var userEntity=toUserEntity(user);
            User savedUser = userRepository.save(userEntity);
            return toUserResponse(savedUser);
        }

    }

    @Override
    public User findEntityByUsername(String username) throws ResourceNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(()->new ResourceNotFoundException("User does not exist"));
    }

    @Override
    public Page<UserResponse> fetch(
            int page,
            int size,
            String sortBy,
            String direction
    ){
        if (sortBy==null) sortBy="userId";
        sortBy= switch (sortBy.toLowerCase()){
            case "name"->"name";
            case "role"->"role";
            case "username"->"username";
            case "createdat"->"createdAt";
            case "updatedat"->"updatedAt";
            default-> "userId";
        };

        Sort sort=direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                :Sort.by(sortBy).ascending();
        Pageable pageable=PageRequest.of(page,size,sort);
        return userRepository.findAll(pageable)
                .map(this::toUserResponse);
    }

    @Override
    @Transactional
    public void delete(String userId) throws ResourceNotFoundException {
        User user=userRepository.findByUsername(userId).orElseThrow(()->new ResourceNotFoundException("user does not exist."));
        user.setEnable(false);

//        userRepository.deleteByUserId(userId).orElseThrow( ()->new ResourceNotFoundException("user does not exist."));
    }
    private User toUserEntity(UserRequest userRequest){
        return User.builder()
                .userId(UUID.randomUUID().toString())
                .name(userRequest.getName())
                .username(userRequest.getUsername())
                .password(passwordEncoder.encode(userRequest.getPassword()))
                .role(userRequest.getRole())
                .isEnable(true)
                .build();
    }
    private UserResponse toUserResponse(User user){
        return UserResponse.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .username(user.getUsername())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
