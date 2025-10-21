package com.restful.core.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.restful.core.model.TokenResponse;
import com.restful.core.model.WebResponse;
import com.restful.core.model.User.LoginUserRequest;
import com.restful.core.repository.UserRepository;
import org.springframework.http.MediaType;

import com.restful.core.entity.User;
import com.restful.core.security.BCrypt;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {
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
        void loginFailedUserNotFound() throws Exception {
                LoginUserRequest request = new LoginUserRequest();
                request.setUsername("nonexistent");
                request.setPassword("wrongpassword");

                mockMvc.perform(
                                post("/api/auth/login")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpectAll(
                                                status().isUnauthorized())
                                .andDo(
                                                result -> {
                                                        WebResponse<String> response = objectMapper.readValue(
                                                                        result.getResponse().getContentAsString(),
                                                                        new TypeReference<>() {
                                                                        });
                                                        assertNotNull(response.getErrors());
                                                });
        }

        @Test
        void loginFailedWrongPassword() throws Exception {
                User user = new User();
                user.setUsername("test");
                user.setName("Test");
                user.setPassword(BCrypt.hashpw("test", BCrypt.gensalt()));
                userRepository.save(user);

                LoginUserRequest request = new LoginUserRequest();
                request.setUsername("test");
                request.setPassword("wrongpassword");

                mockMvc.perform(
                                post("/api/auth/login")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpectAll(
                                                status().isUnauthorized())
                                .andDo(
                                                result -> {
                                                        WebResponse<String> response = objectMapper.readValue(
                                                                        result.getResponse().getContentAsString(),
                                                                        new TypeReference<>() {
                                                                        });
                                                        assertNotNull(response.getErrors());
                                                });
        }

        @Test
        void loginSuccess() throws Exception {
                User user = new User();
                user.setUsername("test");
                user.setName("Test");
                user.setPassword(BCrypt.hashpw("test", BCrypt.gensalt()));
                userRepository.save(user);

                LoginUserRequest request = new LoginUserRequest();
                request.setUsername("test");
                request.setPassword("test");

                mockMvc.perform(
                                post("/api/auth/login")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpectAll(
                                                status().isOk())
                                .andDo(
                                                result -> {
                                                        WebResponse<TokenResponse> response = objectMapper.readValue(
                                                                        result.getResponse().getContentAsString(),
                                                                        new TypeReference<>() {
                                                                        });
                                                        assertNull(response.getErrors());
                                                        assertNotNull(response.getData().getToken());
                                                        assertNotNull(response.getData().getExpiredAt());

                                                        User userInDb = userRepository.findById("test").orElse(null);
                                                        assertNotNull(userInDb);

                                                        assertEquals(userInDb.getToken(),
                                                                        response.getData().getToken());
                                                        assertEquals(userInDb.getTokenExpiredAt(),
                                                                        response.getData().getExpiredAt());
                                                });
        }

        @Test
        void logoutFailed() throws Exception {
                mockMvc.perform(
                                delete("/api/auth/logout")
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
        void logoutSuccess() throws Exception {
                User user = new User();
                user.setUsername("test");
                user.setName("Test");
                user.setPassword(BCrypt.hashpw("test", BCrypt.gensalt()));
                user.setToken("test-token");
                user.setTokenExpiredAt(System.currentTimeMillis() + 100000000L);
                userRepository.save(user);

                mockMvc.perform(
                                delete("/api/auth/logout")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .header("X-API-TOKEN", "test-token"))
                                .andExpectAll(status().isOk())
                                .andDo(result -> {
                                        WebResponse<String> response = objectMapper
                                                        .readValue(result.getResponse().getContentAsString(),
                                                                        new TypeReference<WebResponse<String>>() {
                                                                        });
                                        assertNull(response.getErrors());
                                        assertEquals("OK", response.getData());

                                        User userInDb = userRepository.findById("test").orElse(null);
                                        assertNotNull(userInDb);
                                        assertNull(userInDb.getToken());
                                        assertNull(userInDb.getTokenExpiredAt());
                                });
        }
}