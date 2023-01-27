package org.example.errorhandler.exception.interpreter

class VariableNotFoundException(
    val variableName: String
) : InterpreterException
