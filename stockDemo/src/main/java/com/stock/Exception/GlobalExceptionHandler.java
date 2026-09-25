package com.stock.Exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<ApiError> handleMarketError(RestClientResponseException restClientException) {
        HttpStatusCode statusCode = restClientException.getStatusCode();
        int status = statusCode != null ? statusCode.value() : 500;
        return ResponseEntity
                .status(statusCode != null ? statusCode : HttpStatusCode.valueOf(500))
                .body(new ApiError("api错误", status));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleOtherError(Exception exception) {
        log.error("未处理的异常", exception);
        return ResponseEntity
                .internalServerError()
                .body(new ApiError("服务器内部错误", 500));
    }

    public record ApiError(String message, int status) {
    }

    /**
     * 增加400状态码处理
     * @param exception
     * @return
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handIllegalArgument(IllegalArgumentException exception){
        return ResponseEntity.badRequest().body(new ApiError(exception.getMessage(),400));
    }
}
