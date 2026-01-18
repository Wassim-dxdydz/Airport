package fr.uga.miage.m1.exceptions

sealed class DomainException(
    message: String
) : RuntimeException(message)

class NotFoundException(message: String) : DomainException(message)
