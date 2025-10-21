package com.restful.core.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.restful.core.model.WebResponse;
import com.restful.core.services.AuthServices;
import com.restful.core.entity.User;
import com.restful.core.model.TokenResponse;
import com.restful.core.model.User.LoginUserRequest;

@RestController
public class AuthController {
    @Autowired
    private AuthServices authServices;

    @PostMapping(path = "/api/auth/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse<TokenResponse> login(@RequestBody LoginUserRequest request) {
        TokenResponse response = authServices.login(request);
        return WebResponse.<TokenResponse>builder().data(response).build();
    }

    @DeleteMapping(path = "/api/auth/logout", produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse<String> logout(User user) {
        authServices.logout(user);
        return WebResponse.<String>builder().data("OK").build();
    }
}
