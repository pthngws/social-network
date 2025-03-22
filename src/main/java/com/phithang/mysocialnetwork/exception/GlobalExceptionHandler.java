package com.phithang.mysocialnetwork.exception;

import com.phithang.mysocialnetwork.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Xử lý ngoại lệ AppException (Custom Exception)
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<String>> handleAppException(AppException ex, WebRequest request) {
        ErrorCode errorCode = ex.getErrorCode();
        ApiResponse<String> response = new ApiResponse<>(errorCode.getResponseCode(), null, errorCode.getMessage());
        return new ResponseEntity<>(response, errorCode.getHttpStatusCode());
    }

    // Xử lý ngoại lệ IllegalArgumentException
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<String>> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        ApiResponse<String> response = new ApiResponse<>(ErrorCode.INVALID_REQUEST.getResponseCode(), null, ex.getMessage());
        return new ResponseEntity<>(response, ErrorCode.INVALID_REQUEST.getHttpStatusCode());
    }

    // Xử lý ngoại lệ khi không tìm thấy tài nguyên (NoSuchElementException)
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiResponse<String>> handleNoSuchElementException(NoSuchElementException ex, WebRequest request) {
        ApiResponse<String> response = new ApiResponse<>(ErrorCode.USER_NOT_EXIST.getResponseCode(), null, ex.getMessage());
        return new ResponseEntity<>(response, ErrorCode.USER_NOT_EXIST.getHttpStatusCode());
    }

    // Xử lý IllegalStateException (ví dụ: không tìm thấy user trong SecurityContext)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<String>> handleIllegalStateException(IllegalStateException ex, WebRequest request) {
        ApiResponse<String> response = new ApiResponse<>(ErrorCode.UNAUTHENTICATED.getResponseCode(), null, ex.getMessage());
        return new ResponseEntity<>(response, ErrorCode.UNAUTHENTICATED.getHttpStatusCode());
    }

    // Xử lý validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        ApiResponse<Map<String, String>> response = new ApiResponse<>(ErrorCode.INVALID_REQUEST.getResponseCode(), errors, "Dữ liệu không hợp lệ");
        return new ResponseEntity<>(response, ErrorCode.INVALID_REQUEST.getHttpStatusCode());
    }

    // Xử lý ngoại lệ chung (Exception) - fallback cho các trường hợp không được xử lý cụ thể
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGlobalException(Exception ex, WebRequest request) {
        ApiResponse<String> response = new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, "Lỗi hệ thống: " + ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}