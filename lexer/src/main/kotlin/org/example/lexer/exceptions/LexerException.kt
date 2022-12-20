@file:Suppress("unused")
package org.example.lexer.exceptions

import org.example.inputsource.CodePosition
// TODO message to presenting exceptions
open class LexerException(override val message: String, open val position: CodePosition) : Exception()
