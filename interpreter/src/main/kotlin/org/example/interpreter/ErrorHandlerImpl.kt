package org.example.interpreter

import org.example.errorhandler.ErrorHandler
import org.example.errorhandler.ErrorHandlerConfig
import org.example.errorhandler.exception.interpreter.FunctionNotFoundException
import org.example.errorhandler.exception.interpreter.ImmutableVariableException
import org.example.errorhandler.exception.interpreter.InterpreterException
import org.example.errorhandler.exception.interpreter.InvalidCastException
import org.example.errorhandler.exception.interpreter.InvalidFunctionArgumentsNumberException
import org.example.errorhandler.exception.interpreter.InvalidOperationException
import org.example.errorhandler.exception.interpreter.InvalidRuntimeTypeException
import org.example.errorhandler.exception.interpreter.NoReturnFromFunctionException
import org.example.errorhandler.exception.interpreter.StandardLibraryFunctionOverriddenException
import org.example.errorhandler.exception.interpreter.VariableNotFoundException
import org.example.errorhandler.exception.interpreter.ZeroDivisionException
import org.example.errorhandler.exception.lexer.CommentLengthOverflow
import org.example.errorhandler.exception.lexer.DoublePrecisionOverflow
import org.example.errorhandler.exception.lexer.IdentifierLengthOverflow
import org.example.errorhandler.exception.lexer.InvalidIdentifier
import org.example.errorhandler.exception.lexer.InvalidStringChar
import org.example.errorhandler.exception.lexer.LexerException
import org.example.errorhandler.exception.lexer.NumberOverflow
import org.example.errorhandler.exception.lexer.StringLengthOverflow
import org.example.errorhandler.exception.lexer.UnclosedQuoteString
import org.example.errorhandler.exception.lexer.UnexpectedChar
import org.example.errorhandler.exception.parser.DuplicateFunctionDeclarationException
import org.example.errorhandler.exception.parser.InvalidConditionException
import org.example.errorhandler.exception.parser.InvalidExpressionException
import org.example.errorhandler.exception.parser.InvalidFunctionDeclarationException
import org.example.errorhandler.exception.parser.InvalidFunctionParamDeclarationException
import org.example.errorhandler.exception.parser.InvalidTypeException
import org.example.errorhandler.exception.parser.InvalidVariableDeclaration
import org.example.errorhandler.exception.parser.MissingExpressionException
import org.example.errorhandler.exception.parser.ParserException
import org.example.errorhandler.exception.parser.UnmatchedParenthesisException
import org.example.inputsource.CodePosition
import kotlin.system.exitProcess

class ErrorHandlerImpl(override val errorHandlerConfig: ErrorHandlerConfig) : ErrorHandler {

    override var errorCount = 0

    override fun handleParserError(exception: ParserException) {
        errorCount++

        when (exception) {
            is UnmatchedParenthesisException -> {
                printErrorFormattedWithCodePosition("Unmatched parenthesis", exception.codePosition)
            }

            is MissingExpressionException -> {
                printErrorFormattedWithCodePosition("Missing expression", exception.codePosition)
            }

            is InvalidVariableDeclaration -> {
                printErrorFormattedWithCodePosition("Invalid variable declaration", exception.codePosition)
            }

            is DuplicateFunctionDeclarationException -> {
                printErrorFormattedWithCodePosition("Duplicate function declaration", exception.codePosition)
            }

            is InvalidTypeException -> {
                printErrorFormattedWithCodePosition("Unknown type", exception.codePosition)
            }

            is InvalidFunctionDeclarationException -> {
                printErrorFormattedWithCodePosition("Invalid function declaration", exception.codePosition)
            }

            is InvalidFunctionParamDeclarationException -> {
                printErrorFormattedWithCodePosition("Invalid function parameter declaration", exception.codePosition)
            }

            is InvalidConditionException -> {
                printErrorFormattedWithCodePosition("Invalid condition", exception.codePosition)
            }

            is InvalidExpressionException -> {
                printErrorFormattedWithCodePosition("Invalid expression", exception.codePosition)
            }
        }
        if (errorCount > errorHandlerConfig.maximumNumberOfErrors) {
            println("Encountered too many errors, exiting...")
            exitProcess(1)
        }
    }

    override fun handleLexerError(exception: LexerException) {
        errorCount++
        when (exception) {
            is CommentLengthOverflow -> {
                printErrorFormattedWithCodePosition("Comment length overflow. Maximum length ${exception.maxCommentLength}", exception.codePosition)
            }

            is DoublePrecisionOverflow -> {
                printErrorFormattedWithCodePosition("Double precision overflow. Maximum precision: ${exception.maxDoublePrecision}", exception.codePosition)
            }

            is IdentifierLengthOverflow -> {
                printErrorFormattedWithCodePosition("Identifier length overflow. Maximum length: ${exception.maxLength}", exception.codePosition)
            }

            is NumberOverflow -> {
                printErrorFormattedWithCodePosition("Number overflow. Maximum value: ${exception.maxInt}", exception.codePosition)
            }

            is UnexpectedChar -> {
                printErrorFormattedWithCodePosition("Unexpected character: ${exception.char}", exception.codePosition)
            }

            is UnclosedQuoteString -> {
                printErrorFormattedWithCodePosition("Unclosed quote string", exception.codePosition)
            }

            is StringLengthOverflow -> {
                printErrorFormattedWithCodePosition("String length overflow. Maximum string length: ${exception.maxLength}", exception.codePosition)
            }

            is InvalidIdentifier -> {
                printErrorFormattedWithCodePosition("Invalid identifier: ${exception.identifier}", exception.codePosition)
            }

            is InvalidStringChar -> {
                printErrorFormattedWithCodePosition("Invalid string character: ${exception.char}", exception.codePosition)
            }
        }
        if (errorCount > errorHandlerConfig.maximumNumberOfErrors) {
            println("Encountered too many errors, exiting...")
            exitProcess(1)
        }
    }

    override fun handleInterpreterError(exception: InterpreterException) {
        when (exception) {
            is InvalidRuntimeTypeException -> {
                printInterpreterError("Invalid type: expected ${exception.expected} found: ${exception.actual}")
            }

            is VariableNotFoundException -> {
                printInterpreterError("Variable ${exception.variableName} not found")
            }

            is FunctionNotFoundException -> {
                printInterpreterError("Function ${exception.name} not found")
            }

            is InvalidFunctionArgumentsNumberException -> {
                printInterpreterError(
                    "Invalid function arguments number: expected: ${exception.expected} found: ${exception.actual}"
                )
            }

            is InvalidCastException -> {
                printInterpreterError("Value: ${exception.actual} cannot to be cast to ${exception.expected}")
            }

            is ZeroDivisionException -> {
                printInterpreterError("Division by zero")
            }
            is ImmutableVariableException -> {
                printInterpreterError("Variable ${exception.variableName} is immutable")
            }
            is NoReturnFromFunctionException -> {
                printInterpreterError("No return from function ${exception.functionName}")
            }

            is StandardLibraryFunctionOverriddenException -> {
                printInterpreterError("Function ${exception.functionName} is already defined in StandardLibrary")
            }
            is InvalidOperationException -> {
                printInterpreterError("Invalid operation: ${exception.operation} on type ${exception.leftType}")
            }
        }
        exitProcess(1)
    }
}


private fun CodePosition?.errorLocationString(): String {
    return "at line: ${this?.line}, column: ${this?.line}"
}

fun printErrorFormattedWithCodePosition(message: String, codePosition: CodePosition?) {
    println("Error ${codePosition.errorLocationString()}: $message")
}

fun printInterpreterError(message: String) {
    println("Interpreter error: $message")
    println("Exiting...")
}
