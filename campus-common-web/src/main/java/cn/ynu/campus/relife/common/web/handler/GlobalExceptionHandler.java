package cn.ynu.campus.relife.common.web.handler;

import cn.ynu.campus.relife.common.core.exception.BusinessException;
import cn.ynu.campus.relife.common.core.exception.ErrorCode;
import cn.ynu.campus.relife.common.core.result.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import feign.FeignException;

/**
 * 全局异常处理，HTTP 状态码与 API 规范 §1.3 对齐
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        HttpStatus status = resolveHttpStatus(ex.getCode());
        return ResponseEntity.status(status).body(ApiResponse.fail(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : ErrorCode.PARAM_INVALID.getMessage();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(ErrorCode.PARAM_INVALID.getCode(), message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(ErrorCode.PARAM_INVALID.getCode(), "请求体 JSON 格式错误"));
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ApiResponse<Void>> handleFeignException(FeignException ex) {
        log.warn("Feign downstream failed: status={} message={}", ex.status(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.fail(ErrorCode.DOWNSTREAM_UNAVAILABLE));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(ErrorCode.INTERNAL_ERROR));
    }

    private HttpStatus resolveHttpStatus(int code) {
        if (code == 0) {
            return HttpStatus.OK;
        }
        if (code >= 40001 && code < 40100) {
            return HttpStatus.BAD_REQUEST;
        }
        if (code >= 40101 && code < 40200) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (code >= 40301 && code < 40400) {
            return HttpStatus.FORBIDDEN;
        }
        if (code >= 40401 && code < 40500) {
            return HttpStatus.NOT_FOUND;
        }
        if (code >= 40901 && code < 41000) {
            return HttpStatus.CONFLICT;
        }
        if (code >= 42201 && code < 42300) {
            return HttpStatus.valueOf(422);
        }
        if (code == 42900) {
            return HttpStatus.TOO_MANY_REQUESTS;
        }
        if (code == 10001) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (code == 10002 || code == 20003) {
            return HttpStatus.FORBIDDEN;
        }
        if (code == 10003 || code == 10004 || code == 40902) {
            return HttpStatus.CONFLICT;
        }
        if (code == 20001 || code == 30001 || code == 41002) {
            return HttpStatus.NOT_FOUND;
        }
        if (code >= 20002 && code <= 42206) {
            return HttpStatus.valueOf(422);
        }
        if (code == 41001) {
            return HttpStatus.valueOf(422);
        }
        if (code == 50001) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
