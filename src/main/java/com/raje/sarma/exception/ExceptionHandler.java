package com.raje.sarma.exception;

import com.raje.sarma.constants.Constants;
import com.raje.sarma.service.LoggerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class ExceptionHandler {

  private final LoggerService loggerService;

  @org.springframework.web.bind.annotation.ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException ex) {
    log.error(ex.getMessage());
    Map<String, String> errors = getErrors(ex.getBindingResult());
    return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
  }

  @org.springframework.web.bind.annotation.ExceptionHandler(BindException.class)
  public ResponseEntity<Map<String, String>> handleBindException(HttpServletRequest request,
      HttpServletResponse response, BindException ex) {
    log.error(ex.getMessage());
    Map<String, String> errors = getErrors(ex.getBindingResult());
    loggerService.saveError(request, response, ex);
    return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
  }

  private Map<String, String> getErrors(BindingResult bindingResult) {

    Map<String, String> errors = new HashMap<>();
    bindingResult.getAllErrors().forEach(error -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });
    return errors;
  }

  @org.springframework.web.bind.annotation.ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Map<String, String>> handleConstraintViolationException(
      HttpServletRequest request,
      HttpServletResponse response,
      ConstraintViolationException ex) {
    log.error(ex.getMessage());
    Map<String, String> errors = ex.getConstraintViolations().stream().collect(
        Collectors.toMap(voilation -> voilation.getPropertyPath().toString(),
            ConstraintViolation::getMessage));
    loggerService.saveError(request, response, ex);
    return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
  }

  @org.springframework.web.bind.annotation.ExceptionHandler({RuntimeException.class})
  public ResponseEntity<String> handleRuntimeException(HttpServletRequest request,
      HttpServletResponse response, Exception ex) {
    return handleLogging(request, response, ex,
        Constants.INTERNAL_API_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);

  }

  @org.springframework.web.bind.annotation.ExceptionHandler({IllegalArgumentException.class,
      IOException.class, HandlerMethodValidationException.class})
  public ResponseEntity<String> handleIllegalArgumentException(HttpServletRequest request,
      HttpServletResponse response, Exception ex) {
    HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
    return handleLogging(request, response, ex,
        ex.getMessage(), httpStatus);
  }

  private ResponseEntity<String> handleLogging(HttpServletRequest request,
      HttpServletResponse response, Exception ex, String errorMessage, HttpStatus httpStatus) {
    log.error(ex.getMessage());
    loggerService.saveError(request, response, ex);
    return new ResponseEntity<>(errorMessage, httpStatus);
  }

}
