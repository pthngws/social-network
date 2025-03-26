package com.phithang.mysocialnetwork.service.Impl;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.phithang.mysocialnetwork.dto.RefreshTokenDto;
import com.phithang.mysocialnetwork.dto.request.IntrospectRequest;
import com.phithang.mysocialnetwork.dto.request.LoginRequest;
import com.phithang.mysocialnetwork.dto.request.SignupRequest;
import com.phithang.mysocialnetwork.dto.UserDto;
import com.phithang.mysocialnetwork.entity.UserEntity;
import com.phithang.mysocialnetwork.exception.AppException;
import com.phithang.mysocialnetwork.exception.ErrorCode;
import com.phithang.mysocialnetwork.repository.UserRepository;
import com.phithang.mysocialnetwork.service.IAuthenticateService;
import com.phithang.mysocialnetwork.service.IUserService;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

@Service
public class AuthenticateService implements IAuthenticateService {

    @NonFinal
    @Value("${jwt.secret}")
    protected String SECRET;

    @Autowired
    private IUserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private OtpService otpService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    private static final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String introspectToken(IntrospectRequest token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SECRET.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token.getToken());
        Date expirationDate = signedJWT.getJWTClaimsSet().getExpirationTime();
        if (signedJWT.verify(verifier) && expirationDate.after(new Date())) {
            return signedJWT.getJWTClaimsSet().getSubject();
        }
        throw new AppException(ErrorCode.INVALID_TOKEN);
    }

    @Override
    public UserDto oauth2Login(OidcUser oidcUser, OAuth2User oAuth2User) throws JOSEException {
        String email;
        String name;
        if (oidcUser != null) {
            email = oidcUser.getEmail();
            name = oidcUser.getFullName();
        } else if (oAuth2User != null) {
            email = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("name");
        } else {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Không thể lấy thông tin người dùng từ OAuth2");
        }

        if (email == null || email.isEmpty()) {
            throw new AppException(ErrorCode.EMAIL_INVALID);
        }

        Optional<UserEntity> existingUser = Optional.ofNullable(userService.findUserByEmail(email));
        UserEntity user = existingUser.orElseGet(() -> {
            UserEntity newUser = new UserEntity();
            newUser.setEmail(email);
            newUser.setFirstname(name != null ? name : "Unknown");
            newUser.setRole("USER");
            return userService.saveUser(newUser);
        });
        String accessToken = generateToken(user);
        String refreshToken = generateRefreshToken(user);
        UserDto userDto = new UserDto(user);
        userDto.setToken(accessToken);
        userDto.setRefreshToken(refreshToken);
        return userDto;
    }

    @Override
    public String generateToken(UserEntity userEntity) throws JOSEException {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(userEntity.getEmail())
                .claim("scope", userEntity.getRole())
                .issuer("http://localhost:8080")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()))
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(jwsHeader, payload);
        jwsObject.sign(new MACSigner(SECRET.getBytes()));
        return jwsObject.serialize();
    }

    @Override
    public String generateRefreshToken(UserEntity userEntity) {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String refreshToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        long refreshTokenTTL = 7 * 24 * 60 * 60; // 7 ngày
        refreshTokenService.saveRefreshToken(userEntity.getEmail(), refreshToken, refreshTokenTTL);

        return refreshToken;
    }

    @Override
    public String refreshAccessToken(String refreshToken) throws JOSEException {
        String email = getEmailFromRefreshToken(refreshToken);
        if (email == null) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        UserEntity user = userService.findUserByEmail(email);
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_EXIST);
        }

        return generateToken(user);
    }

    @Override
    public void revokeRefreshToken() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        String email = authentication.getName();

        refreshTokenService.deleteRefreshToken(email);
    }

    private String getEmailFromRefreshToken(String refreshToken) {
        return refreshTokenService.getEmailByRefreshToken(refreshToken);
    }

    @Override
    public UserDto login(LoginRequest loginRequest) throws JOSEException {
        UserEntity userEntity = userService.findUserByEmail(loginRequest.getEmail());
        if (userEntity == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND_BY_EMAIL);
        }
        if (!userEntity.isActive()) {
            throw new AppException(ErrorCode.USER_NOT_ACTIVE, "Tài khoản chưa được kích hoạt");
        }
        if (!passwordEncoder.matches(loginRequest.getPassword(), userEntity.getPassword())) {
            throw new AppException(ErrorCode.PASSWORD_INCORRECT, "Mật khẩu không đúng");
        }

        String token = generateToken(userEntity);
        String refreshToken = generateRefreshToken(userEntity);
        UserDto userDto = new UserDto(userEntity);
        userDto.setToken(token);
        userDto.setRefreshToken(refreshToken);
        return userDto;
    }

    @Override
    public boolean saveUser(SignupRequest signupDto) {
        UserEntity userEntity = signupDto.toUserEntity();
        if (userService.findUserByEmail(userEntity.getEmail()) != null) {
            throw new AppException(ErrorCode.EMAIL_EXIST_REGISTER);
        }
        userEntity.setRole("CLIENT");
        userEntity.setPassword(passwordEncoder.encode(userEntity.getPassword()));
        userEntity.setActive(false);
        userEntity.setFirstname("Người dùng");
        userEntity.setLastname("mới");
        userEntity.setBirthday(new Date());
        userEntity.setImageUrl("https://i.pinimg.com/236x/5e/e0/82/5ee082781b8c41406a2a50a0f32d6aa6.jpg");
        userService.saveUser(userEntity);
        return true;
    }

    @Override
    public void sendOtpForSignup(String email) {
        if (!isValidEmail(email)) {
            throw new AppException(ErrorCode.EMAIL_INVALID);
        }
        String otp = otpService.generateOtp();
        otpService.saveOtp(email, otp);
        try {
            otpService.sendOtpEmail(email, otp);
        } catch (Exception e) {
            throw new AppException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }

    @Override
    public boolean verifyOtpAndActivate(String email, String otp) {
        if (!otpService.verifyOtp(email, otp)) {
            throw new AppException(ErrorCode.INVALID_OTP);
        }
        UserEntity user = userService.findUserByEmail(email);
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND_BY_EMAIL);
        }
        if (user.isActive()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Tài khoản đã được kích hoạt");
        }
        user.setActive(true);
        userService.saveUser(user);
        otpService.deleteOtp(email);
        return true;
    }

    @Override
    public void sendOtpForPasswordReset(String email) {
        UserEntity user = userService.findUserByEmail(email);
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND_BY_EMAIL);
        }
        String otp = otpService.generateOtp();
        otpService.saveOtp(email, otp);
        try {
            otpService.sendOtpForPasswordReset(email, otp);
        } catch (Exception e) {
            throw new AppException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }

    @Override
    public boolean verifyOtpForPasswordReset(String email, String otp) {
        if (!otpService.verifyOtp(email, otp)) {
            throw new AppException(ErrorCode.INVALID_OTP);
        }
        return true;
    }

    @Override
    public boolean resetPassword(String email, String otp, String newPassword) {
        if (!otpService.verifyOtp(email, otp)) {
            throw new AppException(ErrorCode.INVALID_OTP);
        }
        UserEntity user = userService.findUserByEmail(email);
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND_BY_EMAIL);
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.saveUser(user);
        otpService.deleteOtp(email);
        return true;
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}