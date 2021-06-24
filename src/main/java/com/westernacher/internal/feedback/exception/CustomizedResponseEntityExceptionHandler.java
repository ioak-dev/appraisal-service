package com.westernacher.internal.feedback.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Date;

@ControllerAdvice
@RestController
public class CustomizedResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(Exception.class)
  public final ResponseEntity<ExceptionResponse> handleAllExceptions(Exception ex, WebRequest request) {
    ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), ex.getMessage(),
        request.getDescription(false));
    ex.printStackTrace();
    return new ResponseEntity<>(exceptionResponse, HttpStatus.NOT_ACCEPTABLE);
  }

  @ExceptionHandler(HttpClientErrorException.class)
  public final ResponseEntity<ExceptionResponse> handleHttpClientErrorException(HttpClientErrorException ex, WebRequest request) {
    ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), ex.getMessage(),
            request.getDescription(false));
    ex.printStackTrace();
    return new ResponseEntity<>(exceptionResponse, ex.getStatusCode());
  }

  @ExceptionHandler(HttpServerErrorException.class)
  public final ResponseEntity<ExceptionResponse> handleHttpServerErrorException(HttpServerErrorException ex, WebRequest request) {
    ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), ex.getMessage(),
            request.getDescription(false));
    ex.printStackTrace();
    return new ResponseEntity<>(exceptionResponse, ex.getStatusCode());
  }

  @ExceptionHandler(RestClientException.class)
  public final ResponseEntity<ExceptionResponse> handleRestClientException(RestClientException ex, WebRequest request) {
    ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), ex.getMessage(),
            request.getDescription(false));
    ex.printStackTrace();
    return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(AppraisalNotFoundException.class)
  public final ResponseEntity<ExceptionResponse> handleUserNotFoundException(AppraisalNotFoundException ex, WebRequest request) {
    ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), ex.getMessage(),
        request.getDescription(false));
    return new ResponseEntity<>(exceptionResponse, HttpStatus.NOT_FOUND);
  }

}
