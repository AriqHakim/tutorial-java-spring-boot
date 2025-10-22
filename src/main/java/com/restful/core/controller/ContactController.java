package com.restful.core.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.restful.core.services.ContactServices;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;

import com.restful.core.model.Contact.CreateContactRequest;
import com.restful.core.model.Contact.SearchContactRequest;
import com.restful.core.model.Contact.UpdateContactRequest;
import com.restful.core.model.Contact.ContactResponse;
import com.restful.core.model.PagingResponse;
import com.restful.core.model.WebResponse;
import com.restful.core.entity.User;

@RestController
public class ContactController {
        @Autowired
        private ContactServices contactServices;

        @PostMapping(path = "/api/contacts", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<ContactResponse> create(User user, @RequestBody CreateContactRequest request) {
                ContactResponse contactResponse = contactServices.create(user, request);
                return WebResponse.<ContactResponse>builder()
                                .data(contactResponse)
                                .build();
        }

        @GetMapping(path = "/api/contacts/{contactId}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<ContactResponse> get(User user, @PathVariable("contactId") String contactId) {
                ContactResponse contactResponse = contactServices.get(user, contactId);
                return WebResponse.<ContactResponse>builder()
                                .data(contactResponse)
                                .build();
        }

        @PutMapping(path = "/api/contacts/{contactId}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<ContactResponse> update(User user,
                        @RequestBody UpdateContactRequest request,
                        @PathVariable("contactId") String contactId) {
                request.setId(contactId);
                ContactResponse contactResponse = contactServices.udpate(user, request);
                return WebResponse.<ContactResponse>builder()
                                .data(contactResponse)
                                .build();
        }

        @DeleteMapping(path = "/api/contacts/{contactId}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<String> delete(User user, @PathVariable("contactId") String contactId) {
                contactServices.delete(user, contactId);
                return WebResponse.<String>builder()
                                .data("OK")
                                .build();
        }

        @GetMapping(path = "/api/contacts", produces = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<List<ContactResponse>> search(User user,
                        @RequestParam(value = "name", required = false) String name,
                        @RequestParam(value = "email", required = false) String email,
                        @RequestParam(value = "phone", required = false) String phone,
                        @RequestParam(value = "page", required = true, defaultValue = "0") Integer page,
                        @RequestParam(value = "size", required = true, defaultValue = "10") Integer size) {
                SearchContactRequest request = SearchContactRequest.builder()
                                .page(page)
                                .size(size)
                                .name(name)
                                .phone(phone)
                                .email(email)
                                .build();

                Page<ContactResponse> responses = contactServices.search(user, request);
                return WebResponse.<List<ContactResponse>>builder()
                                .data(responses.getContent())
                                .paging(PagingResponse.builder()
                                                .currentPage(responses.getNumber())
                                                .totalPage(responses.getTotalPages())
                                                .size(responses.getSize())
                                                .build())
                                .build();
        }
}
