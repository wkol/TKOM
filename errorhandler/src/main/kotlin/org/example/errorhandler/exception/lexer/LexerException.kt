@file:Suppress("unused")

package org.example.errorhandler.exception.lexer

import org.example.inputsource.CodePosition

open class LexerException(open val codePosition: CodePosition) : Exception()
