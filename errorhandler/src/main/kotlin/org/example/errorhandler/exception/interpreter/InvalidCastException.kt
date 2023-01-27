package org.example.errorhandler.exception.interpreter

class InvalidCastException(
    val expected: String,
    val actual: String
) : InterpreterException
