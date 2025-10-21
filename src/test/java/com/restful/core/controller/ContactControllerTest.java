package com.restful.core.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restful.core.entity.Contact;
import com.restful.core.entity.User;
import com.restful.core.model.WebResponse;
import com.restful.core.model.Contact.ContactResponse;
import com.restful.core.model.Contact.CreateContactRequest;
import com.restful.core.repository.ContactRepository;
import com.restful.core.repository.UserRepository;
import com.restful.core.security.BCrypt;

@SpringBootTest
@AutoConfigureMockMvc
public class ContactControllerTest {
        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private ContactRepository contactRepository;

        @Autowired
        private ObjectMapper objectMapper;

        @BeforeEach
        void setUp() {
                contactRepository.deleteAll();
                userRepository.deleteAll();

                User user = new User();
                user.setUsername("test");
                user.setPassword(BCrypt.hashpw("test", BCrypt.gensalt()));
                user.setName("Test User");
                user.setToken("test");
                user.setTokenExpiredAt(System.currentTimeMillis() + 100000000L);
                userRepository.save(user);

        }

        @Test
        void createContactBadRequest() throws Exception {
                CreateContactRequest request = CreateContactRequest.builder()
                                .firstName("") // Invalid: firstName is blank
                                .lastName("Doe")
                                .email("invalid-email") // Invalid: not a valid email format
                                .phone("1234567890")
                                .build();

                mockMvc.perform(
                                post("/api/contacts")
                                                .accept(MediaType.APPLICATION_JSON_VALUE)
                                                .header("X-API-TOKEN", "test")
                                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpectAll(status().isBadRequest()).andDo(
                                                result -> {
                                                        WebResponse<String> response = objectMapper
                                                                        .readValue(result.getResponse()
                                                                                        .getContentAsString(),
                                                                                        new TypeReference<>() {

                                                                                        });
                                                        assertNotNull(response.getErrors());
                                                });
        }

        @Test
        void createContactSuccess() throws Exception {
                CreateContactRequest request = CreateContactRequest.builder()
                                .firstName("John") // Invalid: firstName is blank
                                .lastName("Doe")
                                .email("john@example.com") // Invalid: not a valid email format
                                .phone("1234567890")
                                .build();

                mockMvc.perform(
                                post("/api/contacts")
                                                .accept(MediaType.APPLICATION_JSON_VALUE)
                                                .header("X-API-TOKEN", "test")
                                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpectAll(status().isOk()).andDo(
                                                result -> {
                                                        WebResponse<ContactResponse> response = objectMapper
                                                                        .readValue(result.getResponse()
                                                                                        .getContentAsString(),
                                                                                        new TypeReference<>() {

                                                                                        });
                                                        assertNull(response.getErrors());

                                                        assertEquals("John", response.getData().getFirstName());
                                                        assertEquals("Doe", response.getData().getLastName());
                                                        assertEquals("john@example.com", response.getData().getEmail());
                                                        assertEquals("1234567890", response.getData().getPhone());

                                                        assertTrue(contactRepository
                                                                        .existsById(response.getData().getId()));

                                                });
        }

        @Test
        void getContactNotFound() throws Exception {
                mockMvc.perform(
                                get("/api/contacts/98234982398")
                                                .header("X-API-TOKEN", "test")
                                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                                .andExpectAll(status().isNotFound()).andDo(
                                                result -> {
                                                        WebResponse<String> response = objectMapper
                                                                        .readValue(result.getResponse()
                                                                                        .getContentAsString(),
                                                                                        new TypeReference<>() {

                                                                                        });
                                                        assertNotNull(response.getErrors());
                                                });
        }

        @Test
        void getContactSuccess() throws Exception {
                User user = userRepository.findById("test").orElseThrow();

                Contact contact = new Contact();
                contact.setId(UUID.randomUUID().toString());
                contact.setUser(user);
                contact.setFirstName("Test");
                contact.setLastName("Test");
                contact.setEmail("Test");
                contact.setPhone("314223454325");
                contactRepository.save(contact);

                mockMvc.perform(
                                get("/api/contacts/" + contact.getId())
                                                .header("X-API-TOKEN", "test")
                                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                                .andExpectAll(status().isOk()).andDo(
                                                result -> {
                                                        WebResponse<ContactResponse> response = objectMapper
                                                                        .readValue(result.getResponse()
                                                                                        .getContentAsString(),
                                                                                        new TypeReference<>() {

                                                                                        });
                                                        assertNull(response.getErrors());

                                                        assertEquals(contact.getId(), response.getData().getId());
                                                        assertEquals(contact.getFirstName(),
                                                                        response.getData().getFirstName());
                                                        assertEquals(contact.getLastName(),
                                                                        response.getData().getLastName());
                                                        assertEquals(contact.getEmail(), response.getData().getEmail());
                                                        assertEquals(contact.getPhone(), response.getData().getPhone());
                                                });
        }
}
