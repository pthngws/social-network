package com.phithang.mysocialnetwork.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Authentication and Authorization Errors (1100 series)
    UNAUTHENTICATED(1100, "Chưa được xác thực", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1101, "Bạn không có quyền thực hiện hành động này", HttpStatus.FORBIDDEN),
    INVALID_TOKEN(1102, "Mã token không hợp lệ", HttpStatus.BAD_REQUEST),
    EXPIRED_TOKEN(1103, "Mã token đã hết hạn", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST(1104, "Yêu cầu không hợp lệ", HttpStatus.BAD_REQUEST),
    EMAIL_EXIST_REGISTER(1105, "Email này đã được sử dụng", HttpStatus.CONFLICT),
    USERNAME_EXIST_REGISTER(1106, "Tên người dùng này đã được sử dụng", HttpStatus.CONFLICT),

    // User Management Errors (1200 series)
    USER_NOT_EXIST(1200, "Người dùng không tồn tại", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND_BY_EMAIL(1201, "Không tìm thấy người dùng với email đã cung cấp", HttpStatus.NOT_FOUND),

    // OTP and Password Reset Errors (1300 series)
    INVALID_OTP(1300, "Mã OTP không hợp lệ", HttpStatus.BAD_REQUEST),
    OTP_NOT_FOUND(1301, "Không tìm thấy mã OTP cho email đã cung cấp", HttpStatus.NOT_FOUND),

    // Refresh Token Errors (1400 series)
    REFRESH_TOKEN_NOT_FOUND(1400, "Không tìm thấy refresh token", HttpStatus.BAD_REQUEST),
    REFRESH_TOKEN_EXPIRED(1401, "Refresh token đã hết hạn", HttpStatus.BAD_REQUEST),

    // Logout Errors (1500 series)
    LOGOUT_FAILED(1500, "Đăng xuất thất bại do thiếu ngữ cảnh xác thực", HttpStatus.UNAUTHORIZED),

    // Email Related Errors (1600 series)
    EMAIL_INVALID(1600, "Địa chỉ email không hợp lệ", HttpStatus.BAD_REQUEST),
    EMAIL_SEND_FAILED(1601, "Gửi email thất bại", HttpStatus.INTERNAL_SERVER_ERROR),

    // Profile, Address, and Album Errors (1700 series)
    PROFILE_NOT_FOUND(1700, "Không tìm thấy profile", HttpStatus.NOT_FOUND),
    ADDRESS_NOT_FOUND(1701, "Không tìm thấy địa chỉ", HttpStatus.NOT_FOUND),
    ALBUM_NOT_FOUND(1702, "Không tìm thấy bộ sưu tập", HttpStatus.NOT_FOUND);

    private final int responseCode;
    private final String message;
    private final HttpStatusCode httpStatusCode;
}