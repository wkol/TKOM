package org.example.inputsource

const val EOF = '\u0004'

interface InputSource {

    fun peekNextChar(): Char

    fun getNextChar(): Char

    fun getPosition(): CodePosition

    fun getCurrentCharacter(): Char

    fun peekNextChars(count: Int): String
}
