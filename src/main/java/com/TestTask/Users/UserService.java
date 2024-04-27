package com.TestTask.Users;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;

import java.sql.Date;
import java.util.List;

public interface UserService {

    List<UserEntity> getAll(Date start, Date end);

    UserEntity getOneById(Long id);

    UserEntity create(UserDTO userDTO);

    UserEntity update(Long id, UserDTO userDTO);

    UserEntity partialUpdate(Long id, JsonPatch jsonPatch) throws JsonPatchException, JsonProcessingException;

    boolean delete(Long id);
}
