package com.phithang.mysocialnetwork.Config;//package mysocialnetwork.demo.Config;
//import java.text.ParseException;
//import java.util.Objects;
//import javax.crypto.spec.SecretKeySpec;
//
//import mysocialnetwork.demo.dto.IntrospectDto;
//import mysocialnetwork.demo.service.IAuthenticateService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
//import org.springframework.security.oauth2.jwt.Jwt;
//import org.springframework.security.oauth2.jwt.JwtDecoder;
//import org.springframework.security.oauth2.jwt.JwtException;
//import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
//import org.springframework.stereotype.Component;
//import com.nimbusds.jose.JOSEException;
//
//@Component
//public class CustomJwtDecoder implements JwtDecoder {
//    @Value("${jwt.secret}")
//    private String signerKey;
//
//    @Autowired
//    private IAuthenticateService authenticationService;
//
//    private NimbusJwtDecoder nimbusJwtDecoder = null;
//
//    @Override
//    public Jwt decode(String token) throws JwtException {
//
//        try {
//            IntrospectDto introspectDto = new IntrospectDto();
//            introspectDto.setToken(token);
//            var response = authenticationService.introspectToken(introspectDto);
//
//            if (response!=null) throw new JwtException("Token invalid");
//        } catch (JOSEException | ParseException e) {
//            throw new JwtException(e.getMessage());
//        }
//
//        if (Objects.isNull(nimbusJwtDecoder)) {
//            SecretKeySpec secretKeySpec = new SecretKeySpec(signerKey.getBytes(), "HS512");
//            nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKeySpec)
//                    .macAlgorithm(MacAlgorithm.HS512)
//                    .build();
//        }
//
//        return nimbusJwtDecoder.decode(token);
//    }
//}