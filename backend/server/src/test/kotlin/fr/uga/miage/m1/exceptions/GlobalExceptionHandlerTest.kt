package fr.uga.miage.m1.exceptions

import org.junit.jupiter.api.Test
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.http.codec.HttpMessageReader
import org.springframework.web.server.ServerWebInputException
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.FieldError
import org.springframework.web.server.ResponseStatusException
import kotlin.test.assertEquals

class GlobalExceptionHandlerTest {

    private val handler = GlobalExceptionHandler()

    class DummyClass {
        fun dummyMethod(field: String) {}
    }

    @Test
    fun `handle validation exception returns BAD_REQUEST`() {
        val target = Any()
        val binding = BeanPropertyBindingResult(target, "req")
        binding.addError(FieldError("req", "field", "must not be blank"))

        val method = DummyClass::class.java.getMethod("dummyMethod", String::class.java)
        val methodParameter = MethodParameter(method, 0)

        val ex = WebExchangeBindException(methodParameter, binding)

        val response = handler.handleValidation(ex)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assert(response.body!!.message.contains("field: must not be blank"))
    }

    @Test
    fun `handle ResponseStatusException returns same status`() {
        val ex = ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied")

        val response = handler.handleResponseStatus(ex)

        assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
        assertEquals("Access denied", response.body!!.message)
    }

    @Test
    fun `handle IllegalArgumentException returns BAD_REQUEST`() {
        val ex = IllegalArgumentException("Invalid argument")

        val response = handler.handleIllegalArgument(ex)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("Invalid argument", response.body!!.message)
    }

    @Test
    fun `handle NotFoundException returns NOT_FOUND`() {
        val ex = NotFoundException("Hangar not found")

        val response = handler.handleNotFound(ex)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertEquals("Hangar not found", response.body!!.message)
    }

    @Test
    fun `handle generic exception returns INTERNAL_SERVER_ERROR`() {
        val ex = Exception("Something went wrong")

        val response = handler.handleGeneric(ex)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals(500, response.body!!.status)
    }
}
