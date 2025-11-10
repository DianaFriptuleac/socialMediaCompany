package dianafriptuleac.socialMediaCompany.exceptions;


import dianafriptuleac.socialMediaCompany.payloads.ErrorsResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
// RestControllerAdvice fornisce "consigli globali" (advice) per tutti i controller REST.
// Spring intercetta automaticamente le eccezioni lanciate nei controller e le passa a qui per gestirle in modo centralizzato.

public class ExceptionsHandler {

    //BadRequestException
    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorsResponseDTO handleBadrequest(BadRequestException ex) {
        // parametro `ex` -> l’eccezione catturata
        return new ErrorsResponseDTO(ex.getMessage(), LocalDateTime.now());
        // ex viene creata e restituita una risposta JSON con messaggio e timestamp
    }

    //UnauthorizedException
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorsResponseDTO handleUnauthorized(UnauthorizedException ex) {
        return new ErrorsResponseDTO(ex.getMessage(), LocalDateTime.now());
    }

    //AuthorizationDeniedException
    @ExceptionHandler(AuthorizationDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorsResponseDTO handleForbidden(AuthorizationDeniedException ex) {
        return new ErrorsResponseDTO("You don't have permission to log in! Administrator only!", LocalDateTime.now());
    }

    //NotFoundException
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorsResponseDTO handleNotFound(NotFoundException ex) {
        return new ErrorsResponseDTO(ex.getMessage(), LocalDateTime.now());
    }

    //Exception
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorsResponseDTO handleGeneric(Exception ex) {
        ex.printStackTrace();
        return new ErrorsResponseDTO("Server-side problem!", LocalDateTime.now());
    }
}
