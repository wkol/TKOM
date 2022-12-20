package org.example.inputsource

import java.io.BufferedReader
import java.io.FileReader
import java.io.StringReader

class InputSourceImpl private constructor(private val source: BufferedReader) : InputSource {
    // TODO add support for multiplatform newlines (CR, LF, CRLF, 155, 25)
    private var codePosition: CodePosition = CodePosition(0, 1, -1)

    init {
        consumeCharacter()
    }

    override var currentChar: Char = 0.toChar()

    override fun getPosition() = codePosition

    override fun consumeCharacter(): Char {
        val readValue = source.read()

        currentChar = when (readValue) {
            -1 -> {
                codePosition = codePosition.handleNextChar()
                EOF
            }
            10 -> {
                codePosition = codePosition.handleNewLine()
                '\n'
            }
            else -> {
                codePosition = codePosition.handleNextChar()
                readValue.toChar()
            }
        }
        return currentChar
    }

    companion object {
        fun fromSource(source: BufferedReader): InputSource {
            return InputSourceImpl(source)
        }

        fun fromFile(path: String): InputSource {
            return FileReader(path).use {
                InputSourceImpl(BufferedReader(it))
            }

        }

        fun fromString(source: String): InputSource {
            return source.byteInputStream().use {
                InputSourceImpl(StringReader(source).buffered())
            }
        }
    }
}
