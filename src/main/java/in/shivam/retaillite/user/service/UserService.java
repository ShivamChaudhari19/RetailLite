package in.shivam.retaillite.user.service;

import in.shivam.retaillite.common.exception.ResourceNotFoundException;
import in.shivam.retaillite.user.dto.UserRequest;
import in.shivam.retaillite.user.dto.UserResponse;
import in.shivam.retaillite.user.entity.User;
import org.springframework.data.domain.Page;

import java.util.List;

public interface UserService {
    UserResponse create(UserRequest user);
    User findEntityByUsername(String username)throws ResourceNotFoundException;
    Page<UserResponse> fetch(int page, int size, String sortBy, String direction);
    void delete(String userId)throws ResourceNotFoundException;
}
