package com.phithang.mysocialnetwork.controller;

import com.nimbusds.jose.JOSEException;
import com.phithang.mysocialnetwork.dto.*;
import com.phithang.mysocialnetwork.dto.request.IntrospectRequest;
import com.phithang.mysocialnetwork.dto.request.LoginRequest;
import com.phithang.mysocialnetwork.dto.request.SignupRequest;
import com.phithang.mysocialnetwork.dto.response.ApiResponse;
import com.phithang.mysocialnetwork.entity.UserEntity;
import com.phithang.mysocialnetwork.service.IAuthenticateService;
import com.phithang.mysocialnetwork.service.IUserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
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

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true); // Ngăn JavaScript truy cập
        cookie.setSecure(true);   // Chỉ gửi qua HTTPS (bỏ qua nếu dùng localhost HTTP)
        cookie.setPath("/");      // Áp dụng cho toàn ứng dụng
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7 ngày, khớp TTL của refresh token
        response.addCookie(cookie);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserDto>> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) throws JOSEException {
        UserDto userDto = authenticateService.login(loginRequest);
        if (userDto != null) {
            setRefreshTokenCookie(response, userDto.getRefreshToken());
            userDto.setRefreshToken(null); // Không trả refreshToken trong JSON
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
            authenticateService.sendOtpForSignup(signupDto.getEmail());
            return ResponseEntity.ok(new ApiResponse<>(200, null, "Signup successful. Please check your email for OTP."));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(400, null, "Invalid email or password"));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<Void>> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        if (authenticateService.verifyOtpAndActivate(email, otp)) {
            return ResponseEntity.ok(new ApiResponse<>(200, null, "OTP verified. Account activated."));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(400, null, "Invalid OTP or email"));
    }

    @GetMapping("/oauth2-login")
    public ResponseEntity<ApiResponse<UserDto>> oauth2Login(
            @AuthenticationPrincipal OidcUser oidcUser,
            @AuthenticationPrincipal OAuth2User oAuth2User,
            HttpServletResponse response) throws JOSEException {
        UserDto userDto = authenticateService.oauth2Login(oidcUser, oAuth2User);
        if (userDto != null) {
            setRefreshTokenCookie(response, userDto.getRefreshToken());
            userDto.setRefreshToken(null); // Không trả refreshToken trong JSON
            return ResponseEntity.ok(new ApiResponse<>(200, userDto, "OAuth login successful"));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(HttpStatus.UNAUTHORIZED.value(), null, "OAuth login failed"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@RequestParam String email) {
        try {
            authenticateService.sendOtpForPasswordReset(email);
            return ResponseEntity.ok(new ApiResponse<>(200, null, "OTP has been sent to your email for password reset"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, null, "Failed to send OTP: " + e.getMessage()));
        }
    }

    @PostMapping("/verify-reset-otp")
    public ResponseEntity<ApiResponse<Void>> verifyResetOtp(
            @RequestParam String email,
            @RequestParam String otp) {
        if (authenticateService.verifyOtpForPasswordReset(email, otp)) {
            return ResponseEntity.ok(new ApiResponse<>(200, null, "OTP verified successfully"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(400, null, "Invalid OTP or email"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @RequestParam String email,
            @RequestParam String otp,
            @RequestParam String newPassword) {
        if (authenticateService.resetPassword(email, otp, newPassword)) {
            return ResponseEntity.ok(new ApiResponse<>(200, null, "Password has been reset successfully"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(400, null, "Failed to reset password. Invalid OTP or email"));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<String>> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) throws JOSEException {
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(401, null, "No refresh token provided"));
        }
        try {
            RefreshTokenDto refreshTokenDto = new RefreshTokenDto();
            refreshTokenDto.setRefreshToken(refreshToken);
            String newAccessToken = authenticateService.refreshAccessToken(refreshTokenDto);
            return ResponseEntity.ok(new ApiResponse<>(200, newAccessToken, "Token refreshed successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(401, null, "Invalid refresh token: " + e.getMessage()));
        }
    }

    @PostMapping("/revoke-token")
    public ResponseEntity<ApiResponse<Void>> revokeToken(HttpServletResponse response) {
        try {
            authenticateService.revokeRefreshToken();
            Cookie cookie = new Cookie("refreshToken", null);
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setPath("/");
            cookie.setMaxAge(0); // Xóa cookie
            response.addCookie(cookie);
            return ResponseEntity.ok(new ApiResponse<>(200, null, "Refresh token revoked successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(401, null, "Unable to revoke token: " + e.getMessage()));
        }
    }
}