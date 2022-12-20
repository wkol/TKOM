package org.example.inputsource

data class CodePosition(
    val column: Long,
    val line: Long,
    val offset: Long,
) {
    fun handleNewLine() = copy(column = 0, line = line + 1, offset = offset + 1)
    fun handleNextChar() = copy(column = column + 1, offset = offset + 1)
}
