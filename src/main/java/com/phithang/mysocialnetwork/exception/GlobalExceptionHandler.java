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
    //Xử lý ngoại lệ Custom
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<String>> handleAppException(AppException ex, WebRequest request) {
        ErrorCode errorCode = ex.getErrorCode();
        ApiResponse<String> response = new ApiResponse<>(errorCode.getResponseCode(), ex.getMessage(), null);
        return new ResponseEntity<>(response, errorCode.getHttpStatusCode());
    }
    // Xử lý ngoại lệ IllegalArgumentException (ví dụ: username đã tồn tại)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<String>> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        ApiResponse<String> response = new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Xử lý ngoại lệ khi không tìm thấy tài nguyên (NoSuchElementException)
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiResponse<String>> handleNoSuchElementException(NoSuchElementException ex, WebRequest request) {
        ApiResponse<String> response = new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "Không tìm thấy tài nguyên: " + ex.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }


    // Xử lý IllegalStateException (ví dụ: không tìm thấy user trong SecurityContext)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<String>> handleIllegalStateException(IllegalStateException ex, WebRequest request) {
        ApiResponse<String> response = new ApiResponse<>(HttpStatus.UNAUTHORIZED.value(), ex.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    //Xử lý validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        ApiResponse<Map<String, String>> response = new ApiResponse<>(HttpStatus.BAD_REQUEST.value(),errors, "Dữ liệu không hợp lệ");
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Xử lý ngoại lệ chung (Exception) - fallback cho các trường hợp không được xử lý cụ thể
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGlobalException(Exception ex, WebRequest request) {
        ApiResponse<String> response = new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Lỗi hệ thống: " + ex.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }


}