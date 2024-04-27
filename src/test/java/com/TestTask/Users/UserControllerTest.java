package com.TestTask.Users;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Date;
import java.util.Calendar;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(UserController.class)
@ComponentScan("com.TestTask.Users")
class UserControllerTest {

    public static final String URI_USERS = "/api/users";
    public static final Long ID = 1L;
    public static final String URI_USERS_ID = URI_USERS + "/" + ID;
    public static final String EMAIL = "test@gmail.com";
    public static final String FIRST_NAME = "test";
    public static final String LAST_NAME = "user";
    public static final Date BIRTH_DATE = Date.valueOf("2000-09-05");
    public static final String ADDRESS = "NY";
    public static final String PHONE_NUMBER = "380123456789";
    private UserEntity user;

    public static final String MESSAGE_ENTITY_NOT_FOUND = "Unable to find UserEntity with id %s";
    public static final String MESSAGE_BAD_REQUEST_WITH_MISSING_REQUIRED_FIELD = "The %s field must not be null";
    public static final String MESSAGE_BAD_REQUEST_WITH_EMPTY_REQUIRED_FIELD = "The %s field must not be blank";
    public static final String MESSAGE_BAD_REQUEST_WITH_INVALID_EMAIL_FIELD = "The email field must be a well-formed email address";
    public static final String MESSAGE_BAD_REQUEST_WITH_BELOW_ALLOWED_AGE = "You cannot use this service, because you're under %s";
    public static final String MESSAGE_BAD_REQUEST_WITH_INVALID_FORMAT = "Failed to read request";
    public static final String MESSAGE_BAD_REQUEST_WITH_FUTURE_DATE_IN_DATE_FIELD = "The %s field must be a past date";

    @Value("${user.permittedAge}")
    private int permittedAge;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserRepository userRepository;

    @BeforeEach
    void init() {
        user = new UserEntity(EMAIL, FIRST_NAME, LAST_NAME, BIRTH_DATE);
    }

    @Test
    void givenUsers_whenGetAllUsers_thenReturnJsonArray() throws Exception {
        List<UserEntity> allUsers = List.of(user);
        given(userRepository.findAll()).willReturn(allUsers);

        mvc.perform(get(URI_USERS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].email").value(EMAIL))
                .andExpect(jsonPath("$.data[0].firstName").value(FIRST_NAME))
                .andExpect(jsonPath("$.data[0].lastName").value(LAST_NAME))
                .andExpect(jsonPath("$.data[0].birthDate").value(BIRTH_DATE.toString()));
    }

    @Test
    void whenGetAllUsersWithStartDateGreaterThanAndEndDate_thenReturnBadRequestError() throws Exception {
        Date start = Date.valueOf("2009-02-06");
        Date end = Date.valueOf("2008-06-02");

        mvc.perform(get(URI_USERS + "?start=" + start + "&end=" + end))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.name()))
                .andExpect(jsonPath("$.message").value("Start date greater than end date"))
                .andExpect(jsonPath("$.path").value(URI_USERS));
    }

    @Test
    void givenUser_whenGetUserById_thenReturnUser() throws Exception {
        UserEntity user = new UserEntity(EMAIL, FIRST_NAME, LAST_NAME, BIRTH_DATE, ADDRESS, PHONE_NUMBER);
        given(userRepository.existsById(ID)).willReturn(true);
        given(userRepository.getReferenceById(ID)).willReturn(user);

        mvc.perform(get(URI_USERS_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(EMAIL))
                .andExpect(jsonPath("$.data.firstName").value(FIRST_NAME))
                .andExpect(jsonPath("$.data.lastName").value(LAST_NAME))
                .andExpect(jsonPath("$.data.birthDate").value(BIRTH_DATE.toString()))
                .andExpect(jsonPath("$.data.address").value(ADDRESS))
                .andExpect(jsonPath("$.data.phoneNumber").value(PHONE_NUMBER));
    }

    @Test
    void whenGetUserByIdWithNonExistentId_thenReturnEntityNotFoundError() throws Exception {
        mvc.perform(get(URI_USERS_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(HttpStatus.NOT_FOUND.name()))
                .andExpect(jsonPath("$.message").value(String.format(MESSAGE_ENTITY_NOT_FOUND, ID)))
                .andExpect(jsonPath("$.path").value(URI_USERS_ID));
    }

    @Test
    void givenUser_whenCreateUser_thenReturnUser() throws Exception {
        given(userRepository.save(any(UserEntity.class))).willReturn(user);

        String body = "{" +
                "\"email\": \"" + EMAIL + "\", " +
                "\"firstName\": \"" + FIRST_NAME + "\", " +
                "\"lastName\": \"" + LAST_NAME + "\", " +
                "\"birthDate\": \"" + BIRTH_DATE + "\" " +
                "}";

        mvc.perform(post(URI_USERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value(EMAIL))
                .andExpect(jsonPath("$.data.firstName").value(FIRST_NAME))
                .andExpect(jsonPath("$.data.lastName").value(LAST_NAME))
                .andExpect(jsonPath("$.data.birthDate").value(BIRTH_DATE.toString()));
    }

    @Test
    void whenCreateUserWithMissingRequiredField_thenReturnBadRequestError() throws Exception {
        String body = "{" +
                "\"firstName\": \"" + FIRST_NAME + "\", " +
                "\"lastName\": \"" + LAST_NAME + "\", " +
                "\"birthDate\": \"" + BIRTH_DATE + "\" " +
                "}";

        mvc.perform(post(URI_USERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.name()))
                .andExpect(jsonPath("$.message").value(String.format(MESSAGE_BAD_REQUEST_WITH_MISSING_REQUIRED_FIELD, "email")))
                .andExpect(jsonPath("$.path").value(URI_USERS));
    }

    @Test
    void whenCreateUserWithEmptyRequiredField_thenReturnBadRequestError() throws Exception {
        String body = "{" +
                "\"email\": \"\", " +
                "\"firstName\": \"" + FIRST_NAME + "\", " +
                "\"lastName\": \"" + LAST_NAME + "\", " +
                "\"birthDate\": \"" + BIRTH_DATE + "\" " +
                "}";

        mvc.perform(post(URI_USERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.name()))
                .andExpect(jsonPath("$.message").value(String.format(MESSAGE_BAD_REQUEST_WITH_EMPTY_REQUIRED_FIELD, "email")))
                .andExpect(jsonPath("$.path").value(URI_USERS));
    }

    @Test
    void whenCreateUserWithInvalidEmail_thenReturnBadRequestError() throws Exception {
        String body = "{" +
                "\"email\": \"test\", " +
                "\"firstName\": \"" + FIRST_NAME + "\", " +
                "\"lastName\": \"" + LAST_NAME + "\", " +
                "\"birthDate\": \"" + BIRTH_DATE + "\" " +
                "}";

        mvc.perform(post(URI_USERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.name()))
                .andExpect(jsonPath("$.message").value(MESSAGE_BAD_REQUEST_WITH_INVALID_EMAIL_FIELD))
                .andExpect(jsonPath("$.path").value(URI_USERS));
    }

    @Test
    void whenCreateUserWithInvalidFormatOfBirthDate_thenReturnBadRequestError() throws Exception {
        String birthDate = "2015.09.09";
        String body = "{" +
                "\"email\": \"" + EMAIL + "\", " +
                "\"firstName\": \"" + FIRST_NAME + "\", " +
                "\"lastName\": \"" + LAST_NAME + "\", " +
                "\"birthDate\": \"" + birthDate + "\" " +
                "}";

        mvc.perform(post(URI_USERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").value(MESSAGE_BAD_REQUEST_WITH_INVALID_FORMAT))
                .andExpect(jsonPath("$.instance").value(URI_USERS));
    }

    @Test
    void whenCreateUserWithBelowAllowedAge_thenReturnBadRequestError() throws Exception {
        Date currentDate = new Date(new java.util.Date().getTime());
        String body = "{" +
                "\"email\": \"" + EMAIL + "\", " +
                "\"firstName\": \"" + FIRST_NAME + "\", " +
                "\"lastName\": \"" + LAST_NAME + "\", " +
                "\"birthDate\": \"" + currentDate + "\" " +
                "}";

        mvc.perform(post(URI_USERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.name()))
                .andExpect(jsonPath("$.message").value(String.format(MESSAGE_BAD_REQUEST_WITH_BELOW_ALLOWED_AGE, permittedAge)))
                .andExpect(jsonPath("$.path").value(URI_USERS));
    }

    @Test
    void whenCreateUserWithFutureDateInBirthDateField_thenReturnBadRequestError() throws Exception {
        java.util.Date currentDate = new java.util.Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.YEAR, 5);
        Date futureDate = new Date(calendar.getTime().getTime());

        String body = "{" +
                "\"email\": \"" + EMAIL + "\", " +
                "\"firstName\": \"" + FIRST_NAME + "\", " +
                "\"lastName\": \"" + LAST_NAME + "\", " +
                "\"birthDate\": \"" + futureDate + "\" " +
                "}";

        mvc.perform(post(URI_USERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.name()))
                .andExpect(jsonPath("$.message").value(String.format(MESSAGE_BAD_REQUEST_WITH_FUTURE_DATE_IN_DATE_FIELD, "birthDate")))
                .andExpect(jsonPath("$.path").value(URI_USERS));
    }

    @Test
    void givenUser_whenUpdateUser_thenReturnUser() throws Exception {
        given(userRepository.existsById(ID)).willReturn(true);
        given(userRepository.getReferenceById(ID)).willReturn(user);

        String updatedEmail = "updatedEmail@gmail.com";
        String updatedFirstName = "updated first name";
        String updatedLastName = "updated last name";
        Date updatedBirthDate = Date.valueOf("1995-05-07");

        String body = "{" +
                "\"email\": \"" + updatedEmail + "\", " +
                "\"firstName\": \"" + updatedFirstName + "\", " +
                "\"lastName\": \"" + updatedLastName + "\", " +
                "\"birthDate\": \"" + updatedBirthDate + "\" " +
                "}";

        mvc.perform(put(URI_USERS_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(updatedEmail))
                .andExpect(jsonPath("$.data.firstName").value(updatedFirstName))
                .andExpect(jsonPath("$.data.lastName").value(updatedLastName))
                .andExpect(jsonPath("$.data.birthDate").value(updatedBirthDate.toString()));
    }

    @Test
    void whenUpdateUserWithNonExistentId_thenReturnEntityNotFoundError() throws Exception {
        String updatedEmail = "updatedEmail@gmail.com";
        String updatedFirstName = "updated first name";
        String updatedLastName = "updated last name";
        Date updatedBirthDate = Date.valueOf("2015-05-07");

        String body = "{" +
                "\"email\": \"" + updatedEmail + "\", " +
                "\"firstName\": \"" + updatedFirstName + "\", " +
                "\"lastName\": \"" + updatedLastName + "\", " +
                "\"birthDate\": \"" + updatedBirthDate + "\" " +
                "}";

        mvc.perform(put(URI_USERS_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(HttpStatus.NOT_FOUND.name()))
                .andExpect(jsonPath("$.message").value(String.format(MESSAGE_ENTITY_NOT_FOUND, ID)))
                .andExpect(jsonPath("$.path").value(URI_USERS_ID));
    }

    @Test
    void givenUser_whenUpdateUserWithMissingRequiredField_thenReturnBadRequestError() throws Exception {
        given(userRepository.existsById(ID)).willReturn(true);
        given(userRepository.getReferenceById(ID)).willReturn(user);

        String updatedEmail = "updatedEmail@gmail.com";
        String updatedLastName = "updated last name";
        Date updatedBirthDate = Date.valueOf("2005-05-07");

        String body = "{" +
                "\"email\": \"" + updatedEmail + "\", " +
                "\"lastName\": \"" + updatedLastName + "\", " +
                "\"birthDate\": \"" + updatedBirthDate + "\" " +
                "}";

        mvc.perform(put(URI_USERS_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.name()))
                .andExpect(jsonPath("$.message").value(String.format(MESSAGE_BAD_REQUEST_WITH_MISSING_REQUIRED_FIELD, "firstName")))
                .andExpect(jsonPath("$.path").value(URI_USERS_ID));
    }

    @Test
    void givenUser_whenUpdateUserWithEmptyRequiredField_thenReturnBadRequestError() throws Exception {
        given(userRepository.existsById(ID)).willReturn(true);
        given(userRepository.getReferenceById(ID)).willReturn(user);

        String updatedEmail = "updatedEmail@gmail.com";
        String updatedLastName = "updated last name";
        Date updatedBirthDate = Date.valueOf("2015-05-07");

        String body = "{" +
                "\"email\": \"" + updatedEmail + "\", " +
                "\"firstName\": \"\", " +
                "\"lastName\": \"" + updatedLastName + "\", " +
                "\"birthDate\": \"" + updatedBirthDate + "\" " +
                "}";

        mvc.perform(put(URI_USERS_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.name()))
                .andExpect(jsonPath("$.message").value(String.format(MESSAGE_BAD_REQUEST_WITH_EMPTY_REQUIRED_FIELD, "firstName")))
                .andExpect(jsonPath("$.path").value(URI_USERS_ID));
    }

    @Test
    void givenUser_whenUpdateUserWithInvalidEmail_thenReturnBadRequestError() throws Exception {
        given(userRepository.existsById(ID)).willReturn(true);
        given(userRepository.getReferenceById(ID)).willReturn(user);

        String updatedEmail = "updatedEmail";
        String updatedFirstName = "updated first name";
        String updatedLastName = "updated last name";
        Date updatedBirthDate = Date.valueOf("2015-05-07");

        String body = "{" +
                "\"email\": \"" + updatedEmail + "\", " +
                "\"firstName\": \"" + updatedFirstName + "\", " +
                "\"lastName\": \"" + updatedLastName + "\", " +
                "\"birthDate\": \"" + updatedBirthDate + "\" " +
                "}";

        mvc.perform(put(URI_USERS_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.name()))
                .andExpect(jsonPath("$.message").value(MESSAGE_BAD_REQUEST_WITH_INVALID_EMAIL_FIELD))
                .andExpect(jsonPath("$.path").value(URI_USERS_ID));
    }

    @Test
    void givenUser_whenUpdateUserWithInvalidFormatOfBirthDate_thenReturnBadRequestError() throws Exception {
        given(userRepository.existsById(ID)).willReturn(true);
        given(userRepository.getReferenceById(ID)).willReturn(user);

        String birthDate = "2015.09.09";

        String body = "{" +
                "\"email\": \"" + EMAIL + "\", " +
                "\"firstName\": \"" + FIRST_NAME + "\", " +
                "\"lastName\": \"" + LAST_NAME + "\", " +
                "\"birthDate\": \"" + birthDate + "\" " +
                "}";

        mvc.perform(put(URI_USERS_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").value(MESSAGE_BAD_REQUEST_WITH_INVALID_FORMAT))
                .andExpect(jsonPath("$.instance").value(URI_USERS_ID));
    }

    @Test
    void givenUser_whenUpdateUserWithBelowAllowedAge_thenReturnBadRequestError() throws Exception {
        given(userRepository.existsById(ID)).willReturn(true);
        given(userRepository.getReferenceById(ID)).willReturn(user);

        Date currentDate = new Date(new java.util.Date().getTime());

        String body = "{" +
                "\"email\": \"" + EMAIL + "\", " +
                "\"firstName\": \"" + FIRST_NAME + "\", " +
                "\"lastName\": \"" + LAST_NAME + "\", " +
                "\"birthDate\": \"" + currentDate + "\" " +
                "}";

        mvc.perform(put(URI_USERS_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.name()))
                .andExpect(jsonPath("$.message").value(String.format(MESSAGE_BAD_REQUEST_WITH_BELOW_ALLOWED_AGE, permittedAge)))
                .andExpect(jsonPath("$.path").value(URI_USERS_ID));
    }

    @Test
    void givenUser_whenUpdateUserWithFutureDateInBirthDateField_thenReturnBadRequestError() throws Exception {
        given(userRepository.existsById(ID)).willReturn(true);
        given(userRepository.getReferenceById(ID)).willReturn(user);

        Date currentDate = new Date(new java.util.Date().getTime());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.YEAR, 5);
        Date futureDate = new Date(calendar.getTime().getTime());

        String body = "{" +
                "\"email\": \"" + EMAIL + "\", " +
                "\"firstName\": \"" + FIRST_NAME + "\", " +
                "\"lastName\": \"" + LAST_NAME + "\", " +
                "\"birthDate\": \"" + futureDate + "\" " +
                "}";

        mvc.perform(put(URI_USERS_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.name()))
                .andExpect(jsonPath("$.message").value(String.format(MESSAGE_BAD_REQUEST_WITH_FUTURE_DATE_IN_DATE_FIELD, "birthDate")))
                .andExpect(jsonPath("$.path").value(URI_USERS_ID));
    }

    @Test
    void givenUser_whenPartialUpdateUser_thenReturnUser() throws Exception {
        given(userRepository.existsById(ID)).willReturn(true);
        given(userRepository.getReferenceById(ID)).willReturn(user);

        String updatedEmail = "updatedEmail@gmail.com";
        String body = "[{" +
                "\"op\": \"replace\"," +
                "\"path\": \"/email\"," +
                "\"value\": \"" + updatedEmail + "\"" +
                "}]";

        mvc.perform(patch(URI_USERS_ID)
                        .contentType(MediaType.APPLICATION_JSON_VALUE + "-patch+json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(updatedEmail))
                .andExpect(jsonPath("$.data.firstName").value(FIRST_NAME))
                .andExpect(jsonPath("$.data.lastName").value(LAST_NAME))
                .andExpect(jsonPath("$.data.birthDate").value(BIRTH_DATE.toString()));
    }

    @Test
    void whenPartialUpdateUserWithNonExistentId_thenReturnEntityNotFoundError() throws Exception {
        String updatedEmail = "updatedEmail@gmail.com";
        String body = "[{" +
                "\"op\": \"replace\"," +
                "\"path\": \"/email\"," +
                "\"value\": \"" + updatedEmail + "\"" +
                "}]";

        mvc.perform(patch(URI_USERS_ID)
                        .contentType(MediaType.APPLICATION_JSON_VALUE + "-patch+json")
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(HttpStatus.NOT_FOUND.name()))
                .andExpect(jsonPath("$.message").value(String.format(MESSAGE_ENTITY_NOT_FOUND, ID)))
                .andExpect(jsonPath("$.path").value(URI_USERS_ID));
    }

    @Test
    void givenUser_whenPartialUpdateUserWithInvalidFormatOfBirthDate_thenReturnBadRequestError() throws Exception {
        given(userRepository.existsById(ID)).willReturn(true);
        given(userRepository.getReferenceById(ID)).willReturn(user);

        String birthDate = "2015.09.09";
        String body = "[{" +
                "\"op\": \"replace\"," +
                "\"path\": \"/birthDate\"," +
                "\"value\": \"" + birthDate + "\"" +
                "}]";

        mvc.perform(patch(URI_USERS_ID)
                        .contentType(MediaType.APPLICATION_JSON_VALUE + "-patch+json")
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.name()))
                .andExpect(jsonPath("$.message")
                        .value(String.format("Failed to parse Date value '%s': Cannot parse date \"%s\": not compatible with " +
                                "any of standard forms (\"yyyy-MM-dd'T'HH:mm:ss.SSSX\", \"yyyy-MM-dd'T'HH:mm:ss.SSS\", \"EEE, " +
                                "dd MMM yyyy HH:mm:ss zzz\", \"yyyy-MM-dd\"))\n", birthDate, birthDate)))
                .andExpect(jsonPath("$.path").value(URI_USERS_ID));
    }

    @Test
    void givenUser_whenPartialUpdateUserWithBellowAllowedAge_thenReturnBadRequestError() throws Exception {
        given(userRepository.existsById(ID)).willReturn(true);
        given(userRepository.getReferenceById(ID)).willReturn(user);

        Date currentDate = new Date(new java.util.Date().getTime());
        String body = "[{" +
                "\"op\": \"replace\"," +
                "\"path\": \"/birthDate\"," +
                "\"value\": \"" + currentDate + "\"" +
                "}]";

        mvc.perform(patch(URI_USERS_ID)
                        .contentType(MediaType.APPLICATION_JSON_VALUE + "-patch+json")
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.name()))
                .andExpect(jsonPath("$.message").value(String.format(MESSAGE_BAD_REQUEST_WITH_BELOW_ALLOWED_AGE, permittedAge)))
                .andExpect(jsonPath("$.path").value(URI_USERS_ID));
    }

    @Test
    void givenTrue_whenDeleteUser_thenReturnJson() throws Exception {
        given(userRepository.existsById(ID)).willReturn(true);

        mvc.perform(delete(URI_USERS_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("Successfully deleted"));
    }

    @Test
    void whenDeleteUserWithNonExistentId_thenReturnEntityNotFoundError() throws Exception {
        mvc.perform(delete(URI_USERS_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(HttpStatus.NOT_FOUND.name()))
                .andExpect(jsonPath("$.message").value(String.format(MESSAGE_ENTITY_NOT_FOUND, ID)))
                .andExpect(jsonPath("$.path").value(URI_USERS_ID));
    }
}