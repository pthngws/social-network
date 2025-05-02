package com.phithang.mysocialnetwork.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // General Errors (1000 series)
    INVALID_REQUEST(1000, "Yêu cầu không hợp lệ", HttpStatus.BAD_REQUEST),

    // Authentication and Authorization Errors (1100 series)
    UNAUTHENTICATED(1100, "Chưa được xác thực", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1101, "Bạn không có quyền thực hiện hành động này", HttpStatus.FORBIDDEN),
    INVALID_TOKEN(1102, "Mã token không hợp lệ", HttpStatus.BAD_REQUEST),
    EXPIRED_TOKEN(1103, "Mã token đã hết hạn", HttpStatus.BAD_REQUEST),
    EMAIL_EXIST_REGISTER(1104, "Email này đã được sử dụng", HttpStatus.CONFLICT),
    USERNAME_EXIST_REGISTER(1105, "Tên người dùng này đã được sử dụng", HttpStatus.CONFLICT),

    // User Management Errors (1200 series)
    USER_NOT_EXIST(1200, "Người dùng không tồn tại", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND_BY_EMAIL(1201, "Không tìm thấy người dùng với email đã cung cấp", HttpStatus.NOT_FOUND),
    USER_NOT_ACTIVE(1202,"Người dùng chưa kích hoạt", HttpStatus.NOT_ACCEPTABLE),
    PASSWORD_INCORRECT(1203,"Mật khẩu không chính xác",HttpStatus.BAD_REQUEST),
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
    ALBUM_NOT_FOUND(1702, "Không tìm thấy bộ sưu tập", HttpStatus.NOT_FOUND),

    // Post-Related Errors (1800 series)
    POST_NOT_FOUND(1800, "Không tìm thấy bài viết", HttpStatus.NOT_FOUND),
    POST_CREATION_FAILED(1801, "Tạo bài viết thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    POST_UPDATE_FAILED(1802, "Cập nhật bài viết thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    POST_DELETE_FAILED(1803, "Xóa bài viết thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    POST_MEDIA_LIMIT_EXCEEDED(1804, "Số lượng media trong bài viết vượt quá giới hạn", HttpStatus.BAD_REQUEST),
    POST_MEDIA_NOT_FOUND(1805, "Không tìm thấy media của bài viết", HttpStatus.NOT_FOUND),
    POST_VISIBILITY_UNAUTHORIZED(1806, "Bạn không có quyền xem bài viết này", HttpStatus.FORBIDDEN),
    INVALID_REACTION_TYPE(1807, "Invalid reaction type",HttpStatus.BAD_REQUEST),
    // Comment-Related Errors (1900 series)
    COMMENT_NOT_FOUND(1900, "Không tìm thấy bình luận", HttpStatus.NOT_FOUND),
    COMMENT_CREATION_FAILED(1901, "Tạo bình luận thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    COMMENT_UPDATE_FAILED(1902, "Cập nhật bình luận thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    COMMENT_DELETE_FAILED(1903, "Xóa bình luận thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    COMMENT_MEDIA_NOT_FOUND(1904, "Không tìm thấy media của bình luận", HttpStatus.NOT_FOUND),
    COMMENT_UNAUTHORIZED(1905, "Bạn không có quyền thực hiện hành động này với bình luận", HttpStatus.FORBIDDEN),

    // Friendship-Related Errors (2000 series)
    FRIENDSHIP_NOT_FOUND(2000, "Không tìm thấy quan hệ bạn bè", HttpStatus.NOT_FOUND),
    FRIENDSHIP_ALREADY_EXISTS(2001, "Quan hệ bạn bè đã tồn tại", HttpStatus.CONFLICT),
    FRIENDSHIP_REQUEST_NOT_FOUND(2002, "Không tìm thấy yêu cầu kết bạn", HttpStatus.NOT_FOUND),
    FRIENDSHIP_REQUEST_FAILED(2003, "Gửi yêu cầu kết bạn thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    FRIENDSHIP_REQUEST_ALREADY_SENT(2004, "Yêu cầu kết bạn đã được gửi trước đó", HttpStatus.CONFLICT),
    FRIENDSHIP_UNAUTHORIZED(2005, "Bạn không có quyền thực hiện hành động này với quan hệ bạn bè", HttpStatus.FORBIDDEN),

    // Message-Related Errors (2100 series)
    MESSAGE_NOT_FOUND(2100, "Không tìm thấy tin nhắn", HttpStatus.NOT_FOUND),
    MESSAGE_CREATION_FAILED(2101, "Gửi tin nhắn thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    MESSAGE_DELETE_FAILED(2102, "Xóa tin nhắn thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    MESSAGE_MEDIA_NOT_FOUND(2103, "Không tìm thấy media của tin nhắn", HttpStatus.NOT_FOUND),
    MESSAGE_UNAUTHORIZED(2104, "Bạn không có quyền xem hoặc chỉnh sửa tin nhắn này", HttpStatus.FORBIDDEN),

    // Notification-Related Errors (2200 series)
    NOTIFICATION_NOT_FOUND(2200, "Không tìm thấy thông báo", HttpStatus.NOT_FOUND),
    NOTIFICATION_CREATION_FAILED(2201, "Tạo thông báo thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    NOTIFICATION_DELETE_FAILED(2202, "Xóa thông báo thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    NOTIFICATION_UNAUTHORIZED(2203, "Bạn không có quyền xem hoặc chỉnh sửa thông báo này", HttpStatus.FORBIDDEN),

    // Report-Related Errors (2300 series)
    REPORT_NOT_FOUND(2300, "Không tìm thấy báo cáo", HttpStatus.NOT_FOUND),
    REPORT_CREATION_FAILED(2301, "Tạo báo cáo thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    REPORT_ALREADY_EXISTS(2302, "Bạn đã báo cáo nội dung này trước đó", HttpStatus.CONFLICT),
    REPORT_UNAUTHORIZED(2303, "Bạn không có quyền thực hiện hành động này với báo cáo", HttpStatus.FORBIDDEN),

    // Media-Related Errors (2400 series)
    MEDIA_NOT_FOUND(2400, "Không tìm thấy media", HttpStatus.NOT_FOUND),
    MEDIA_UPLOAD_FAILED(2401, "Tải media lên thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    MEDIA_DELETE_FAILED(2402, "Xóa media thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    MEDIA_FORMAT_INVALID(2403, "Định dạng media không hợp lệ", HttpStatus.BAD_REQUEST),
    MEDIA_SIZE_EXCEEDED(2404, "Kích thước media vượt quá giới hạn cho phép", HttpStatus.BAD_REQUEST),
    MEDIA_DATA_INVALID(2410, "Dữ liệu media không hợp lệ",HttpStatus.BAD_REQUEST),
    CLOUDINARY_AUTH_FAILED(2413, "Xác thực Cloudinary thất bại",HttpStatus.BAD_REQUEST);
    private final int responseCode;
    private final String message;
    private final HttpStatusCode httpStatusCode;
}