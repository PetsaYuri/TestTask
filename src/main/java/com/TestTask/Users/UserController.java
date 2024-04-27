package com.TestTask.Users;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    public static final String URI_USERS_ID = "/{id}";

    private final UserService userService;

    private final UserMapper userMapper;

    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers(@RequestParam(required = false) Date start, @RequestParam(required = false) Date end) {
        Map<String, List<UserDTO>> response = new HashMap<>();
        response.put("data", userService.getAll(start, end)
                .stream()
                .map(userMapper)
                .toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping(URI_USERS_ID)
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        Map<String, UserDTO> response = new HashMap<>();
        UserEntity user = userService.getOneById(id);
        response.put("data", userMapper.apply(user));
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody @Valid UserDTO userDTO) {
        Map<String, UserDTO> response = new HashMap<>();
        UserEntity createdUser = userService.create(userDTO);
        response.put("data", userMapper.apply(createdUser));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping(URI_USERS_ID)
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody @Valid UserDTO userDTO) {
        Map<String, UserDTO> response = new HashMap<>();
        UserEntity updatedUser = userService.update(id, userDTO);
        response.put("data", userMapper.apply(updatedUser));
        return ResponseEntity.ok(response);
    }

    @PatchMapping(value = URI_USERS_ID, consumes = "application/json-patch+json")
    public ResponseEntity<?> partialUpdateUser(@PathVariable Long id, @RequestBody JsonPatch jsonPatch) throws JsonPatchException, JsonProcessingException {
        Map<String, UserDTO> response = new HashMap<>();
        UserEntity updatedUser = userService.partialUpdate(id, jsonPatch);
        response.put("data", userMapper.apply(updatedUser));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(URI_USERS_ID)
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        Map<String, Map<String, String>> response = new HashMap<>();
        Map<String, String> message = new HashMap<>();
        if (userService.delete(id)) {
            message.put("message", "Successfully deleted");
            response.put("data", message);
            return ResponseEntity.ok(response);
        }
        return null;
    }
}
