package com.grainzon.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Turns every error into an RFC 9457 problem response ({@code application/problem+json}).
 * <p>
 * Spring's base class already maps the standard MVC errors (malformed JSON, wrong parameter types, unknown URLs) to
 * the right 4xx status. This class adds a per-field {@code errors} map to validation failures, so clients can show
 * the reason, and turns anything unexpected into a 500 that is logged with its stack trace but never exposes
 * internals to the client.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	/** Invalid request body, for example a blank or too-long product name. */
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		Map<String, String> errors = new LinkedHashMap<>();
		for (FieldError error : ex.getBindingResult().getFieldErrors()) {
			errors.putIfAbsent(error.getField(), error.getDefaultMessage());
		}
		return validationProblem(ex, ex.getBody(), errors, headers, status, request);
	}

	/** Invalid query parameters, for example {@code page=-1} or {@code size=101}. */
	@Override
	protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		Map<String, String> errors = new LinkedHashMap<>();
		ex.getParameterValidationResults()
			.forEach(result -> result.getResolvableErrors()
				.forEach(error -> errors.putIfAbsent(result.getMethodParameter().getParameterName(),
						error.getDefaultMessage())));
		return validationProblem(ex, ex.getBody(), errors, headers, status, request);
	}

	/** Anything not handled above is a bug: log it in full, tell the client only that it failed. */
	@ExceptionHandler(Exception.class)
	public ProblemDetail handleUnexpected(Exception ex, HttpServletRequest request) {
		log.error("Unhandled error on {} {}", request.getMethod(), request.getRequestURI(), ex);
		return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong.");
	}

	private ResponseEntity<Object> validationProblem(Exception ex, ProblemDetail body, Map<String, String> errors,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		// Client mistakes are expected, so debug rather than warn: visible when debugging, quiet in normal logs.
		log.debug("Rejected request {}: {}", request.getDescription(false), errors);
		body.setDetail("Invalid request.");
		body.setProperty("errors", errors);
		return handleExceptionInternal(ex, body, headers, status, request);
	}

}
