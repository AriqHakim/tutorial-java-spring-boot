package com.restful.core.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.restful.core.entity.User;
import com.restful.core.model.WebResponse;
import com.restful.core.model.User.RegisterUserRequest;
import com.restful.core.model.User.UpdateUserRequest;
import com.restful.core.model.User.UserResponse;
import com.restful.core.repository.UserRepository;
import com.restful.core.security.BCrypt;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void testRegisterSuccess() {
        try {
            RegisterUserRequest request = new RegisterUserRequest();
            request.setUsername("test");
            request.setPassword("rahasia");
            request.setName("Test User");

            mockMvc.perform(
                    post("/api/users/register")
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpectAll(status().isOk())
                    .andDo(result -> {
                        WebResponse<String> response = objectMapper
                                .readValue(result.getResponse().getContentAsString(),
                                        new TypeReference<WebResponse<String>>() {
                                        });

                        assertEquals("OK", response.getData());
                    });
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void testRegisterEmpty() {
        try {
            RegisterUserRequest request = new RegisterUserRequest();
            request.setUsername("");
            request.setPassword("");
            request.setName("");

            mockMvc.perform(
                    post("/api/users/register")
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpectAll(status().isBadRequest())
                    .andDo(result -> {
                        WebResponse<String> response = objectMapper
                                .readValue(result.getResponse().getContentAsString(),
                                        new TypeReference<WebResponse<String>>() {
                                        });

                        assertNotNull(response.getErrors());
                    });
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void getUserUnauthorized() throws Exception {
        mockMvc.perform(
                get("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", "not found"))
                .andExpectAll(status().isUnauthorized())
                .andDo(result -> {
                    WebResponse<String> response = objectMapper
                            .readValue(result.getResponse().getContentAsString(),
                                    new TypeReference<WebResponse<String>>() {
                                    });

                    assertNotNull(response.getErrors());
                });

    }

    @Test
    void getUserNullToken() throws Exception {
        mockMvc.perform(
                get("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isUnauthorized())
                .andDo(result -> {
                    WebResponse<String> response = objectMapper
                            .readValue(result.getResponse().getContentAsString(),
                                    new TypeReference<WebResponse<String>>() {
                                    });

                    assertNotNull(response.getErrors());
                });

    }

    @Test
    void getUserSuccess() throws Exception {
        User user = new User();
        user.setUsername("test");
        user.setName("Test");
        user.setToken("test");
        user.setPassword(BCrypt.hashpw("test", BCrypt.gensalt()));
        user.setTokenExpiredAt(System.currentTimeMillis() + 100000000000L);
        userRepository.save(user);

        mockMvc.perform(
                get("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", "test"))
                .andExpectAll(status().isOk())
                .andDo(result -> {
                    WebResponse<UserResponse> response = objectMapper
                            .readValue(result.getResponse().getContentAsString(),
                                    new TypeReference<WebResponse<UserResponse>>() {
                                    });

                    assertNull(response.getErrors());
                    assertEquals("test", response.getData().getUsername());
                    assertEquals("Test", response.getData().getName());
                });

    }

    @Test
    void getUserExpired() throws Exception {
        User user = new User();
        user.setUsername("test");
        user.setName("Test");
        user.setToken("test");
        user.setPassword(BCrypt.hashpw("test", BCrypt.gensalt()));
        user.setTokenExpiredAt(System.currentTimeMillis() - 100000000L);
        userRepository.save(user);

        mockMvc.perform(
                get("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", "test"))
                .andExpectAll(status().isUnauthorized())
                .andDo(result -> {
                    WebResponse<UserResponse> response = objectMapper
                            .readValue(result.getResponse().getContentAsString(),
                                    new TypeReference<WebResponse<UserResponse>>() {
                                    });

                    assertNotNull(response.getErrors());
                });

    }

    @Test
    void updateUserUnauthorized() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();

        mockMvc.perform(
                patch("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpectAll(status().isUnauthorized())
                .andDo(result -> {
                    WebResponse<String> response = objectMapper
                            .readValue(result.getResponse().getContentAsString(),
                                    new TypeReference<WebResponse<String>>() {
                                    });

                    assertNotNull(response.getErrors());
                });

    }

    @Test
    void updateUserSuccess() throws Exception {
        User user = new User();
        user.setUsername("test");
        user.setName("Test");
        user.setToken("test");
        user.setPassword(BCrypt.hashpw("test", BCrypt.gensalt()));
        user.setTokenExpiredAt(System.currentTimeMillis() + 100000000L);
        userRepository.save(user);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Updated Name");
        request.setPassword("newpassword");

        mockMvc.perform(
                patch("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", "test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpectAll(status().isOk())
                .andDo(result -> {
                    WebResponse<UserResponse> response = objectMapper
                            .readValue(result.getResponse().getContentAsString(),
                                    new TypeReference<WebResponse<UserResponse>>() {
                                    });

                    assertNull(response.getErrors());
                    assertEquals("test", response.getData().getUsername());
                    assertEquals("Updated Name", response.getData().getName());

                    User userInDb = userRepository.findById("test").orElseThrow();
                    assertTrue(BCrypt.checkpw("newpassword", userInDb.getPassword()));
                });

    }
}
