package com.messageschallenge.notifications.web.exception;

import org.springframework.http.HttpStatus;

public final class ValidationException extends DomainException {
  public ValidationException(String message) {
    super(message);
  }

  @Override
  public HttpStatus httpStatus() {
    return HttpStatus.BAD_REQUEST;
  }

  @Override
  public String slug() {
    return "validation";
  }

  @Override
  public String title() {
    return "Validation failed";
  }
}
