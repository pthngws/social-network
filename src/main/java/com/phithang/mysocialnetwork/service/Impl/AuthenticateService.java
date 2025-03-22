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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Autowired
    private UserRepository userRepository;

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
            // Xác thực Google
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
        if (userEntity != null) {
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
        UserEntity userEntity = new UserEntity();
        userEntity = signupDto.toUserEntity();
        userEntity.setRole("CLIENT");
        userEntity.setPassword(passwordEncoder.encode(userEntity.getPassword()));
        userService.saveUser(userEntity);
        return true;
    }
}
