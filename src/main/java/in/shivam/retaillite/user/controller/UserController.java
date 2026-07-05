package in.shivam.retaillite.user.controller;

import in.shivam.retaillite.user.dto.UserRequest;
import in.shivam.retaillite.user.dto.UserResponse;
import in.shivam.retaillite.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/register")
    public ResponseEntity<UserResponse>create(@Valid @RequestBody UserRequest user){
        log.debug("creating user:{}",user);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.create(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{userId}")
    public void delete(@Email @PathVariable String userId){
        userService.delete(userId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> fetch(
            @RequestParam(defaultValue = "0")
            @Min(0) int page,
            @RequestParam(defaultValue = "10")
            @Min(0) @Max(50)int size,
            @RequestParam(defaultValue = "id")
            String  sortBy,
            @RequestParam(defaultValue = "asc")
            String orderedBy

    ){
        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.fetch(page,size,sortBy, orderedBy));
    }
}
