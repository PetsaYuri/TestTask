package com.TestTask.Users;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Date;
import java.util.Calendar;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Value("${user.permittedAge}")
    private int permittedAge;

    private final UserRepository userRepository;

    private final ObjectMapper objectMapper;

    public UserServiceImpl(UserRepository userRepository, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<UserEntity> getAll(Date start, Date end) {
        if (start != null && end != null) {
            if (start.before(end)) {
                return userRepository.findALlByBirthDateBetween(start, end);
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date greater than end date");
        }
        return userRepository.findAll();
    }

    @Override
    public UserEntity getOneById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("Unable to find UserEntity with id " + id);
        }
        return userRepository.getReferenceById(id);
    }

    @Override
    public UserEntity create(UserDTO userDTO) {
        ageVerification(userDTO.birthDate());
        UserEntity user = new UserEntity(userDTO.email(), userDTO.firstName(), userDTO.lastName(), userDTO.birthDate(),
                userDTO.address(), userDTO.phoneNumber());
        return userRepository.save(user);
    }

    public void ageVerification(Date userBirthDate) {
        java.util.Date currentDate = new java.util.Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.YEAR, -permittedAge);
        if (userBirthDate.after(calendar.getTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot use this service, because you're under " + permittedAge);
        }
    }

    @Override
    public UserEntity update(Long id, UserDTO userDTO) {
        UserEntity existingUser = getOneById(id);
        UserEntity updatedUser = new UserEntity(userDTO.email(), userDTO.firstName(), userDTO.lastName(), userDTO.birthDate(),
                userDTO.address(), userDTO.phoneNumber());
        updatedUser.setId(existingUser.getId());
        ageVerification(updatedUser.getBirthDate());
        userRepository.save(updatedUser);
        return updatedUser;
    }

    @Override
    public UserEntity partialUpdate(Long id, JsonPatch jsonPatch) throws JsonPatchException, JsonProcessingException {
        UserEntity existingUser = getOneById(id);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        JsonNode patched = jsonPatch.apply(objectMapper.convertValue(existingUser, JsonNode.class));
        UserEntity updatedUser = objectMapper.treeToValue(patched, UserEntity.class);
        ageVerification(updatedUser.getBirthDate());
        userRepository.save(updatedUser);
        return updatedUser;
    }

    @Override
    public boolean delete(Long id) {
        UserEntity user = getOneById(id);
        userRepository.delete(user);
        return true;
    }
}
