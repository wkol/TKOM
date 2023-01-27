package org.example.interpreter

import org.example.errorhandler.ErrorHandler
import org.example.interpreter.data.Environment
import org.example.parser.data.Program
import org.example.parser.data.Visitor

abstract class Interpreter : Visitor {

    abstract val errorHandler: ErrorHandler
    abstract val environment: Environment

    abstract fun interpret(program: Program): Any?
}
