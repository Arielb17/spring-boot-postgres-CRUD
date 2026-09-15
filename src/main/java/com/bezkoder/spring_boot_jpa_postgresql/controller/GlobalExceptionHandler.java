package com.bezkoder.spring_boot_jpa_postgresql.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@RestControllerAdvice
@NullMarked
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    @Nullable
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return response("O corpo da requisição contém JSON inválido.", headers, HttpStatus.BAD_REQUEST, request, ex);
    }

    @Override
    @Nullable
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return response("Parâmetro obrigatório ausente.", headers, HttpStatus.BAD_REQUEST, request, ex);
    }

    @Override
    @Nullable
    protected ResponseEntity<Object> handleServletRequestBindingException(ServletRequestBindingException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return response("Parâmetro da requisição inválido.", headers, HttpStatus.BAD_REQUEST, request, ex);
    }

    @Override
    @Nullable
    protected ResponseEntity<Object> handleTypeMismatch(org.springframework.beans.TypeMismatchException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return response("Parâmetro da requisição inválido.", headers, HttpStatus.BAD_REQUEST, request, ex);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @Nullable
    public ResponseEntity<Object> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex,
            WebRequest request) {
        return response("Parâmetro da requisição inválido.", new HttpHeaders(), HttpStatus.BAD_REQUEST, request, ex);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @Nullable
    public ResponseEntity<Object> handleDataIntegrityViolation(DataIntegrityViolationException ex, WebRequest request) {
        return response("A operação entra em conflito com os dados existentes.", new HttpHeaders(),
                HttpStatus.CONFLICT, request, ex);
    }

    @ExceptionHandler(RuntimeException.class)
    @Nullable
    public ResponseEntity<Object> handleUnexpected(RuntimeException ex, WebRequest request) {
        return response("Ocorreu um erro interno ao processar a requisição.", new HttpHeaders(),
                HttpStatus.INTERNAL_SERVER_ERROR, request, ex);
    }

    @Override
    @Nullable
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        if (status.is5xxServerError()) {
            logger.error("Unexpected application failure while handling request", ex);
        }
        return super.handleExceptionInternal(ex, body, headers, status, request);
    }

    @Nullable
    private ResponseEntity<Object> response(String detail, HttpHeaders headers, HttpStatusCode status,
            WebRequest request, Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        return handleExceptionInternal(ex, problem, headers, status, request);
    }
}
