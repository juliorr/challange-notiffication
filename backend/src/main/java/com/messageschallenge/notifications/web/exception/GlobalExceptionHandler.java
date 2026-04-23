package com.messageschallenge.notifications.web.exception;

import com.messageschallenge.notifications.config.AppProperties;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalExceptionHandler {

  private final String detailsBaseUrl;

  public GlobalExceptionHandler(AppProperties props) {
    this.detailsBaseUrl = props.errors().detailsBaseUrl();
  }

  @ExceptionHandler(DomainException.class)
  public ProblemDetail onDomain(DomainException ex) {
    return problem(ex.httpStatus(), ex.getMessage(), ex.slug(), ex.title());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail onBeanValidation(MethodArgumentNotValidException ex) {
    List<Map<String, String>> errors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(
                fe ->
                    Map.of(
                        "field",
                        fe.getField(),
                        "message",
                        fe.getDefaultMessage() == null ? "invalid" : fe.getDefaultMessage()))
            .toList();
    var pd =
        problem(
            HttpStatus.BAD_REQUEST, "Request validation failed", "validation", "Validation failed");
    pd.setProperty("errors", errors);
    return pd;
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ProblemDetail onConstraintViolation(ConstraintViolationException ex) {
    return problem(HttpStatus.BAD_REQUEST, ex.getMessage(), "validation", "Validation failed");
  }

  private ProblemDetail problem(HttpStatus status, String detail, String slug, String title) {
    var pd = ProblemDetail.forStatusAndDetail(status, detail);
    pd.setType(URI.create(detailsBaseUrl + slug));
    pd.setTitle(title);
    return pd;
  }
}
