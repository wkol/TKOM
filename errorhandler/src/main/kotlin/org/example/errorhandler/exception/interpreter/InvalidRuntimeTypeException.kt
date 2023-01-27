package org.example.errorhandler.exception.interpreter

class InvalidRuntimeTypeException(
    val expected: String,
    val actual: String
) : InterpreterException
