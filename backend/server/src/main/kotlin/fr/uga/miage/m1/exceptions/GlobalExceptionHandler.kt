package fr.uga.miage.m1.exceptions

import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

data class ApiErrorResponse(
    val status: Int,
    val message: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val path: String? = null
)

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException::class)
    fun handleValidation(ex: WebExchangeBindException): ResponseEntity<ApiErrorResponse> {
        val errors = ex.bindingResult.fieldErrors
            .joinToString(", ") { "${it.field}: ${it.defaultMessage}" }

        return ResponseEntity(
            ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), errors),
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatus(ex: ResponseStatusException): ResponseEntity<ApiErrorResponse> =
        ResponseEntity(
            ApiErrorResponse(ex.statusCode.value(), ex.reason ?: "Erreur de requête"),
            ex.statusCode
        )

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(ex: NotFoundException): ResponseEntity<ApiErrorResponse> =
        ResponseEntity(
            ApiErrorResponse(HttpStatus.NOT_FOUND.value(), ex.message ?: "Ressource non trouvée"),
            HttpStatus.NOT_FOUND
        )

    @ExceptionHandler(IllegalStateException::class)
    fun handleConflict(ex: IllegalStateException): ResponseEntity<ApiErrorResponse> =
        ResponseEntity(
            ApiErrorResponse(HttpStatus.CONFLICT.value(), ex.message ?: "Conflit"),
            HttpStatus.CONFLICT
        )

    @ExceptionHandler(DuplicateKeyException::class)
    fun handleDuplicateKey(ex: DuplicateKeyException): ResponseEntity<ApiErrorResponse> =
        ResponseEntity(
            ApiErrorResponse(HttpStatus.CONFLICT.value(), "Ressource déjà existante"),
            HttpStatus.CONFLICT
        )

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ApiErrorResponse> =
        ResponseEntity(
            ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.message ?: "Requête invalide"),
            HttpStatus.BAD_REQUEST
        )

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ResponseEntity<ApiErrorResponse> =
        ResponseEntity(
            ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Erreur interne du serveur"),
            HttpStatus.INTERNAL_SERVER_ERROR
        )
}
