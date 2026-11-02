package cl.duoc.gamerevents.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice(annotations = RestController.class)
public class ApiExceptionHandler {
    private static final String BAD_REQUEST = "Solicitud inválida.";

    @ExceptionHandler(InvalidCredentialsException.class)
    ResponseEntity<ApiError> invalidCredentials(InvalidCredentialsException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, "Bearer")
                .body(new ApiError(exception.getMessage()));
    }

    @ExceptionHandler({ BindException.class, HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class })
    ResponseEntity<ApiError> badRequest(Exception exception) {
        return ResponseEntity.badRequest().body(new ApiError(BAD_REQUEST));
    }

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ApiError> responseStatus(ResponseStatusException exception) {
        String message = exception.getStatusCode().value() == 400 ? BAD_REQUEST : "No se pudo procesar la solicitud.";
        return ResponseEntity.status(exception.getStatusCode()).body(new ApiError(message));
    }
}
