package com.phithang.mysocialnetwork.service.Impl;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.phithang.mysocialnetwork.dto.request.IntrospectRequest;
import com.phithang.mysocialnetwork.dto.request.LoginRequest;
import com.phithang.mysocialnetwork.dto.request.SignupRequest;
import com.phithang.mysocialnetwork.dto.UserDto;
import com.phithang.mysocialnetwork.entity.UserEntity;
import com.phithang.mysocialnetwork.repository.UserRepository;
import com.phithang.mysocialnetwork.service.IAuthenticateService;
import com.phithang.mysocialnetwork.service.IUserService;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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

    @Override
    public String introspectToken(IntrospectRequest token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SECRET.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token.getToken());
        Date expirationDate = signedJWT.getJWTClaimsSet().getExpirationTime();
        if (signedJWT.verify(verifier) && expirationDate.after(new Date())) {
            return signedJWT.getJWTClaimsSet().getSubject();
        } else {
            return null;
        }
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
        UserDto userDto = new UserDto(user);
        userDto.setToken(accessToken);
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
    public UserDto login(LoginRequest loginRequest) throws JOSEException {
        UserEntity userEntity = userService.findUserByEmail(loginRequest.getEmail());
        if (userEntity != null && userEntity.isActive()) {
            if (passwordEncoder.matches(loginRequest.getPassword(), userEntity.getPassword())) {
                String token = this.generateToken(userEntity);
                UserDto userDto = new UserDto(userEntity);
                userDto.setToken(token);
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
        if (user != null) {
            String otp = otpService.generateOtp();
            otpService.saveOtp(email, otp);
            otpService.sendOtpForPasswordReset(email, otp);
        }
    }

    @Override
    public boolean verifyOtpForPasswordReset(String email, String otp) {
        return otpService.verifyOtp(email, otp);
    }

    @Override
    public boolean resetPassword(String email, String otp, String newPassword) {
        if (otpService.verifyOtp(email, otp)) {
            UserEntity user = userService.findUserByEmail(email);
            if (user != null) {
                user.setPassword(passwordEncoder.encode(newPassword));
                userService.saveUser(user);
                otpService.deleteOtp(email);
                return true;
            }
        }
        return false;
    }
}