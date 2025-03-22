package com.phithang.mysocialnetwork.controller;

import com.nimbusds.jose.JOSEException;
import com.phithang.mysocialnetwork.dto.*;
import com.phithang.mysocialnetwork.dto.request.IntrospectRequest;
import com.phithang.mysocialnetwork.dto.request.LoginRequest;
import com.phithang.mysocialnetwork.dto.request.PasswordDto;
import com.phithang.mysocialnetwork.dto.request.SignupRequest;
import com.phithang.mysocialnetwork.dto.response.ApiResponse;
import com.phithang.mysocialnetwork.entity.UserEntity;
import com.phithang.mysocialnetwork.service.IAuthenticateService;
import com.phithang.mysocialnetwork.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
public class AuthenticateController {
    @Autowired
    private IUserService userService;

    @Autowired
    private IAuthenticateService authenticateService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserDto>> login(@RequestBody LoginRequest loginRequest) throws JOSEException {
        UserDto userDto = authenticateService.login(loginRequest);
        if (userDto != null) {
            return ResponseEntity.ok(new ApiResponse<>(200, userDto, "Login successful"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(400, null, "Invalid username or password"));
    }

    @PostMapping("/introspect")
    public ResponseEntity<ApiResponse<String>> introspect(@RequestBody IntrospectRequest token) throws JOSEException, ParseException {
        String email = authenticateService.introspectToken(token);
        if (email != null) {
            return ResponseEntity.ok(new ApiResponse<>(200, email, "Introspect successful"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(400, null, "Invalid token"));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@RequestBody SignupRequest signupDto) {
        UserEntity userEntity = userService.findUserByEmail(signupDto.getEmail());
        if (userEntity != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(400, null, "Email already in use"));
        }
        if (authenticateService.saveUser(signupDto)) {
            return ResponseEntity.ok(new ApiResponse<>(200, null, "Signup successful"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(400, null, "Invalid email or password"));
    }

    @PostMapping("/updatepassword")
    public ResponseEntity<ApiResponse<Void>> changepassword(@RequestBody PasswordDto passwordDto) {
        if (userService.updatePassword(passwordDto)) {
            return ResponseEntity.ok(new ApiResponse<>(200, null, "Update password successful!"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(400, null, "Update password failed!"));
    }

    @GetMapping("/oauth2-login")
    public ResponseEntity<ApiResponse<UserDto>> oauth2Login(
            @AuthenticationPrincipal OidcUser oidcUser,
            @AuthenticationPrincipal OAuth2User oAuth2User) throws JOSEException {
        UserDto userResponse = authenticateService.oauth2Login(oidcUser, oAuth2User);
        if (userResponse == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>(HttpStatus.UNAUTHORIZED.value(), null, "Không thể xác thực bằng Google hoặc Facebook")
            );
        }
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), userResponse, "Đăng nhập thành công"));
    }
}
