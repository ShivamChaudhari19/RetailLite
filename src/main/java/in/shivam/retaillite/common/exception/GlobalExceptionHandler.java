package in.shivam.retaillite.common.exception;

import in.shivam.retaillite.category.exception.CategoryAlreadyExists;
import in.shivam.retaillite.payment.InvoiceAlreadyPaidException;
import in.shivam.retaillite.user.exception.UserAlreadyExists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler( AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException exception){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(
                        ErrorResponse.builder()
                                .error("unauthorized")
                                .message( "invalid username or password")
                                .status(HttpStatus.UNAUTHORIZED.value())
                                .timestamp(System.currentTimeMillis()).build()
                );
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<?> handleUsernameNotFoundException(UsernameNotFoundException exception){
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "error","Not Found",
                        "message",exception.getMessage()
                ));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e){
        Map<String,String>errors=new HashMap<>();
        e.getBindingResult().getAllErrors()
                .forEach(error->{
                    String fieldName=((FieldError)error).getField();
                    String errorMessage=error.getDefaultMessage();
                    errors.put(fieldName,errorMessage);
                });
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        ErrorResponse.builder()
                                .error(errors.toString())
                                .message("validation failed")
                                .status(HttpStatus.BAD_REQUEST.value())
                                .timestamp(System.currentTimeMillis())
                                .build()
                );
    }
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(
                        ErrorResponse.builder()
                                .error("Resource not Found")
                                .message(e.getMessage())
                                .status(HttpStatus.NOT_FOUND.value())
                                .timestamp(System.currentTimeMillis())
                                .build()
                );
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e){
        log.error("{}\n{}",e.getMessage(),e.getClass());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(
                        ErrorResponse.builder()
                                .error("Invalid role.")
                                .message("Allowed values: ROLE_USER, ROLE_ADMIN")
                                .status(HttpStatus.BAD_REQUEST.value()).
                                timestamp(System.currentTimeMillis()).build()
                );
    }
    @ExceptionHandler(UserAlreadyExists.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(UserAlreadyExists e){
        log.error("{}\n{}",e.getMessage(),e.getClass());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(
                        ErrorResponse.builder()
                                .error(e.getMessage())
                                .message("Choose another Unique Username")
                                .status(HttpStatus.CONFLICT.value()).
                                timestamp(System.currentTimeMillis()).build()
                );
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder()
                        .error("Internal Server Error")
                        .message("Contact support")
                        .status(500).timestamp(System.currentTimeMillis()).build());
    }
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException e){

        return ResponseEntity
                .status(e.getStatusCode())
                .body(ErrorResponse.builder()
                        .error(e.getReason())
                        .message(e.getMessage())
                        .status(e.getStatusCode().value())
                        .timestamp(System.currentTimeMillis())
                        .build()
                );
    }
    @ExceptionHandler(CategoryAlreadyExists.class)
    public ResponseEntity<ErrorResponse> CategoryAlreadyExists(CategoryAlreadyExists e){
        log.error("{}\n{}",e.getMessage(),e.getClass());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(
                        ErrorResponse.builder()
                                .error(e.getMessage())
                                .message("Category already exists")
                                .status(HttpStatus.CONFLICT.value()).
                                timestamp(System.currentTimeMillis()).build()
                );
    }
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(
            NoResourceFoundException ex
    ){
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(
                        ErrorResponse.builder().error("Resource not found")
                                .message(ex.getMessage())
                                .status(HttpStatus.NOT_FOUND.value())
                                .timestamp(System.currentTimeMillis()).build()
                );
    }
    @ExceptionHandler({AuthorizationDeniedException.class, AccessDeniedException.class})
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(
                        ErrorResponse.builder()
                                .error("access denied").message(e.getMessage()).status(HttpStatus.FORBIDDEN.value())
                                .timestamp(System.currentTimeMillis()).build()
                );
    }
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabledException(DisabledException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(
                        ErrorResponse.builder()
                                .error("user not found").message(e.getMessage()).status(HttpStatus.UNAUTHORIZED.value())
                                .timestamp(System.currentTimeMillis()).build()
                );
    }
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorResponse> handleDisabledException(JwtException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(
                        ErrorResponse.builder()
                                .error("Authentication error:").message(e.getMessage()).status(HttpStatus.UNAUTHORIZED.value())
                                .timestamp(System.currentTimeMillis()).build()
                );
    }
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorResponse> handleDisabledException(ExpiredJwtException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(
                        ErrorResponse.builder()
                                .error("Authentication error:").message(e.getMessage()).status(HttpStatus.UNAUTHORIZED.value())
                                .timestamp(System.currentTimeMillis()).build()
                );
    }
    @ExceptionHandler(InvoiceAlreadyPaidException.class)
    public ResponseEntity<ErrorResponse> handleInvoiceAlreadyPaidException(InvoiceAlreadyPaidException e){
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.builder()
                        .error("InvoiceAlreadyPaidException").message(e.getMessage()).status(HttpStatus.CONFLICT.value()).timestamp(System.currentTimeMillis()).build());
    }
    @ExceptionHandler(QuantityOutOfBoundException.class)
    public ResponseEntity<ErrorResponse> handleQuantityOutOfBoundException(QuantityOutOfBoundException e){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(
                        ErrorResponse.builder()
                                .error("Quantity Out of Bound")
                                .message(e.getMessage())
                                .status(HttpStatus.BAD_REQUEST.value())
                                .timestamp(System.currentTimeMillis())
                                .build()
                );
    }
    @ExceptionHandler(RazorpayException.class)
    public ResponseEntity<ErrorResponse> handleRazorpayException(RazorpayException e){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        ErrorResponse.builder()
                                .error("Razorpay Exception")
                                .message(e.getMessage())
                                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .timestamp(System.currentTimeMillis())
                                .build()
                );
    }
    @ExceptionHandler(InvoiceCanceledException.class)
    public ResponseEntity<ErrorResponse> handleInvoiceCanceledException(InvoiceCanceledException e){
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ErrorResponse.builder()
                                .error("InvoiceCanceledException exception occurred").message(e.getMessage()).status(HttpStatus.OK.value()).timestamp(System.currentTimeMillis()).build()
                );
    }
}
