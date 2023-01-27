package org.example.errorhandler.exception.interpreter

class InvalidFunctionArgumentsNumberException(
    val expected: Int,
    val actual: Int
) : InterpreterException
