package org.example.lexer

import org.example.inputsource.CodePosition
import org.example.lexer.token.Token

abstract class Lexer {
    abstract val config: LexerConfig
    abstract var token: Token?
    abstract fun getNextToken()
    abstract fun getCodePosition(): CodePosition
}