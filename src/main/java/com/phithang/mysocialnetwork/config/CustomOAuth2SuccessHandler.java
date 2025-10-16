package com.phithang.mysocialnetwork.config;

import com.nimbusds.jose.JOSEException;
import com.phithang.mysocialnetwork.dto.UserDto;
import com.phithang.mysocialnetwork.service.IAuthenticateService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private IAuthenticateService authenticateService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, 
                                      Authentication authentication) throws IOException {
        try {
            OidcUser oidcUser = null;
            OAuth2User oAuth2User = null;

            if (authentication.getPrincipal() instanceof OidcUser) {
                oidcUser = (OidcUser) authentication.getPrincipal();
            } else if (authentication.getPrincipal() instanceof OAuth2User) {
                oAuth2User = (OAuth2User) authentication.getPrincipal();
            }

            // Xử lý OAuth2 login
            UserDto userDto = authenticateService.oauth2Login(oidcUser, oAuth2User);
            
            if (userDto != null) {
                // Set refresh token cookie
                setRefreshTokenCookie(response, userDto.getRefreshToken());
                userDto.setRefreshToken(null); // Không trả refreshToken trong JSON

                // Set response headers
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.setStatus(HttpServletResponse.SC_OK);

                // Redirect về frontend với token trong URL
                String redirectUrl = getBaseUrl() + "/login?oauth2=success&token=" + userDto.getToken();
                response.sendRedirect(redirectUrl);
            } else {
                response.sendRedirect(getBaseUrl() + "/login?error=oauth_failed");
            }
        } catch (JOSEException e) {
            response.sendRedirect(getBaseUrl() + "/login?error=token_generation_failed");
        } catch (Exception e) {
            response.sendRedirect(getBaseUrl() + "/login?error=oauth_failed");
        }
    }

    private String getBaseUrl() {
        return frontendUrl.endsWith("/login") ? 
            frontendUrl.substring(0, frontendUrl.length() - 6) : frontendUrl;
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);   // Set true cho HTTPS production
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);
        // Thêm SameSite=None để hỗ trợ cross-origin
        response.addHeader("Set-Cookie", 
            String.format("refreshToken=%s; Path=/; Max-Age=%d; HttpOnly; SameSite=None; Secure=true", 
                refreshToken, 7 * 24 * 60 * 60));
    }
}
