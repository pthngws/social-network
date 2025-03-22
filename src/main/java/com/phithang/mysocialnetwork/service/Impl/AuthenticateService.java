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
    private UserRepository userRepository;

    @Autowired
    private OtpService otpService;

    @Autowired
    private RefreshTokenService refreshTokenService; // Giả định bạn đã có RefreshTokenService

    private static final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String introspectToken(IntrospectRequest token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SECRET.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token.getToken());
        Date expirationDate = signedJWT.getJWTClaimsSet().getExpirationTime();
        if (signedJWT.verify(verifier) && expirationDate.after(new Date())) {
            return signedJWT.getJWTClaimsSet().getSubject(); // Trả về email
        }
        return null;
    }

    @Override
    public UserDto oauth2Login(OidcUser oidcUser, OAuth2User oAuth2User) throws JOSEException {
        String email;
        String name;
        if (oidcUser != null) {
            email = oidcUser.getEmail();
            name = oidcUser.getFullName();
        } else {
            name = "";
            email = "";
        }

        Optional<UserEntity> existingUser = Optional.ofNullable(userRepository.findByEmail(email));
        UserEntity user = existingUser.orElseGet(() -> {
            UserEntity newUser = new UserEntity();
            newUser.setEmail(email);
            newUser.setFirstname(name);
            newUser.setRole("USER");
            return userRepository.save(newUser);
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
    public String refreshAccessToken(RefreshTokenDto refreshTokenDto) throws JOSEException {
        String email = getEmailFromRefreshToken(refreshTokenDto.getRefreshToken());
        if (email == null) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        UserEntity user = userRepository.findByEmail(email);

        return generateToken(user);
    }

    @Override
    public void revokeRefreshToken() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        String email = authentication.getName(); // Lấy email từ SecurityContext
        refreshTokenService.deleteRefreshToken(email);
    }

    private String getEmailFromRefreshToken(String refreshToken) {
        // Kiểm tra refresh token trong service, trả về email nếu hợp lệ
        return refreshTokenService.getEmailByRefreshToken(refreshToken);
    }

    @Override
    public UserDto login(LoginRequest loginRequest) throws JOSEException {
        UserEntity userEntity = userService.findUserByEmail(loginRequest.getEmail());
        if (userEntity != null && userEntity.isActive()) {
            if (passwordEncoder.matches(loginRequest.getPassword(), userEntity.getPassword())) {
                String token = generateToken(userEntity);
                String refreshToken = generateRefreshToken(userEntity);
                UserDto userDto = new UserDto(userEntity);
                userDto.setToken(token);
                userDto.setRefreshToken(refreshToken);
                return userDto;
            }
        }
        return null;
    }

    @Override
    public boolean saveUser(SignupRequest signupDto) {
        UserEntity userEntity = signupDto.toUserEntity();
        userEntity.setRole("CLIENT");
        userEntity.setPassword(passwordEncoder.encode(userEntity.getPassword()));
        userEntity.setActive(false);
        userService.saveUser(userEntity);
        return true;
    }

    @Override
    public void sendOtpForSignup(String email) {
        String otp = otpService.generateOtp();
        otpService.saveOtp(email, otp);
        otpService.sendOtpEmail(email, otp);
    }

    @Override
    public boolean verifyOtpAndActivate(String email, String otp) {
        if (otpService.verifyOtp(email, otp)) {
            UserEntity user = userService.findUserByEmail(email);
            if (user != null && !user.isActive()) {
                user.setActive(true);
                userService.saveUser(user);
                otpService.deleteOtp(email);
                return true;
            }
        }
        return false;
    }

    @Override
    public void sendOtpForPasswordReset(String email) {
        UserEntity user = userService.findUserByEmail(email);
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_EXIST);
        }
        String otp = otpService.generateOtp();
        otpService.saveOtp(email, otp);
        otpService.sendOtpForPasswordReset(email, otp);
    }

    @Override
    public boolean verifyOtpForPasswordReset(String email, String otp) {
        return otpService.verifyOtp(email, otp);
    }

    @Override
    public boolean resetPassword(String email, String otp, String newPassword) {
        if (!otpService.verifyOtp(email, otp)) {
            return false;
        }
        UserEntity user = userService.findUserByEmail(email);
        if (user == null) {
            return false;
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.saveUser(user);
        otpService.deleteOtp(email);
        return true;
    }
}