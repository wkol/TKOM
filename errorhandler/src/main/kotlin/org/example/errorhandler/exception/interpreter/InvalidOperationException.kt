package org.example.errorhandler.exception.interpreter

class InvalidOperationException(
    val operation: String,
    val leftType: String,
) : InterpreterException
