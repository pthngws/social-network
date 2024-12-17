package com.phithang.mysocialnetwork.Config;//package mysocialnetwork.demo.Config;
//import java.io.IOException;
//
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//
//import mysocialnetwork.demo.dto.ResponseDto;
//import org.springframework.http.MediaType;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.web.AuthenticationEntryPoint;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
//    @Override
//    public void commence(
//            HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
//            throws IOException, ServletException {
//
//        response.setStatus(403);
//        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//
//        ResponseDto<?> apiResponse = new ResponseDto<>();
//        apiResponse.setStatus(403);
//        apiResponse.setMessage("Unauthorized");
//        ObjectMapper objectMapper = new ObjectMapper();
//
//        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
//        response.flushBuffer();
//    }
//}