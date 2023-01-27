package org.example.errorhandler.exception.interpreter

class FunctionNotFoundException(
    val name: String
) : InterpreterException
