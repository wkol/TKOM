package org.example.inputsource

import java.io.BufferedReader
import java.io.FileReader
import java.io.StringReader

class InputSourceImpl private constructor(private val source: BufferedReader) : InputSource {
    private var codePosition: CodePosition = CodePosition(0, 1, -1)

    init {
        consumeCharacter()
    }

    override var currentChar: Char = 0.toChar()

    override fun getPosition() = codePosition

    override fun consumeCharacter(): Char = try {
        val readValue = source.read()

        currentChar = when (readValue) {
            ERROR -> {
                codePosition = codePosition.handleNextChar()
                EOF
            }
            NEWLINE -> {
                codePosition = codePosition.handleNewLine()
                '\n'
            }
            else -> {
                codePosition = codePosition.handleNextChar()
                readValue.toChar()
            }
        }
        currentChar
    } catch (e: Exception) {
        currentChar = EOF
        EOF
    }

    companion object {

        const val NEWLINE = 10
        const val ERROR = -1

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
