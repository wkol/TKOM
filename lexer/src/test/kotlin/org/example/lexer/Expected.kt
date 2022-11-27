package org.example.lexer

import org.example.inputsource.CodePosition
import org.example.lexer.token.Token

class Expected private constructor(
    val input: String,
    val tokens: List<Token>
) {
    companion object {
        val VARIABLE_DECLARATION = Expected(
            input = "var _abasgdf_21 = 0",
            tokens = listOf(
                Token.Keyword(
                    value = Token.Keyword.Type.VAR,
                    position = CodePosition(
                        column = 1,
                        line = 1,
                        offset = 0,
                    )
                ),
                Token.Identifier(
                    value = "_abasgdf_21",
                    position = CodePosition(
                        column = 5,
                        line = 1,
                        offset = 4,
                    )
                ),
                Token.Special(
                    value = Token.Special.Type.ASSIGN,
                    position = CodePosition(
                        column = 17,
                        line = 1,
                        offset = 16,
                    )
                ),
                Token.Literal(
                    value = 0,
                    position = CodePosition(
                        column = 19,
                        line = 1,
                        offset = 18,
                    ),
                    literalType = Token.Literal.Type.INTEGER
                ),
                Token.EOF(
                    position = CodePosition(
                        column = 20,
                        line = 1,
                        offset = 19,
                    )
                )
            )
        )
        val CONST_DECLARATION = Expected(
            input = "const _abasgdf_21 = 0",
            tokens = listOf(
                Token.Keyword(
                    value = Token.Keyword.Type.CONST,
                    position = CodePosition(
                        column = 1,
                        line = 1,
                        offset = 0,
                    )
                ),
                Token.Identifier(
                    value = "_abasgdf_21",
                    position = CodePosition(
                        column = 7,
                        line = 1,
                        offset = 6,
                    )
                ),
                Token.Special(
                    value = Token.Special.Type.ASSIGN,
                    position = CodePosition(
                        column = 19,
                        line = 1,
                        offset = 18,
                    )
                ),
                Token.Literal(
                    value = 0,
                    position = CodePosition(
                        column = 21,
                        line = 1,
                        offset = 20,
                    ),
                    literalType = Token.Literal.Type.INTEGER
                ),
                Token.EOF(
                    position = CodePosition(
                        column = 22,
                        line = 1,
                        offset = 21,
                    )
                )
            )
        )

        val FUNCTION_DECLARATION = Expected(
            input = """
                fun ______a(int c, int d) -> int { 
                    return c+d 
                }
            """.trimIndent(),
            tokens = listOf(
                Token.Keyword(
                    value = Token.Keyword.Type.FUN,
                    position = CodePosition(
                        column = 1,
                        line = 1,
                        offset = 0,
                    )
                ),
                Token.Identifier(
                    value = "______a",
                    position = CodePosition(
                        column = 5,
                        line = 1,
                        offset = 4,
                    )
                ),
                Token.Special(
                    value = Token.Special.Type.LPAREN,
                    position = CodePosition(
                        column = 12,
                        line = 1,
                        offset = 11,
                    )
                ),
                Token.Keyword(
                    value = Token.Keyword.Type.INT,
                    position = CodePosition(
                        column = 13,
                        line = 1,
                        offset = 12,
                    )
                ),
                Token.Identifier(
                    value = "c",
                    position = CodePosition(
                        column = 17,
                        line = 1,
                        offset = 16,
                    )
                ),
                Token.Special(
                    value = Token.Special.Type.COMMA,
                    position = CodePosition(
                        column = 18,
                        line = 1,
                        offset = 17,
                    )
                ),
                Token.Keyword(
                    value = Token.Keyword.Type.INT,
                    position = CodePosition(
                        column = 20,
                        line = 1,
                        offset = 19,
                    )
                ),
                Token.Identifier(
                    value = "d",
                    position = CodePosition(
                        column = 24,
                        line = 1,
                        offset = 23,
                    )
                ),
                Token.Special(
                    value = Token.Special.Type.RPAREN,
                    position = CodePosition(
                        column = 25,
                        line = 1,
                        offset = 24,
                    )
                ),
                Token.FunReturnTypeArrow(
                    position = CodePosition(
                        column = 27,
                        line = 1,
                        offset = 26,
                    )
                ),
                Token.Keyword(
                    value = Token.Keyword.Type.INT,
                    position = CodePosition(
                        column = 30,
                        line = 1,
                        offset = 29,
                    )
                ),
                Token.Special(
                    value = Token.Special.Type.LBRACE,
                    position = CodePosition(
                        column = 34,
                        line = 1,
                        offset = 33,
                    )
                ),
                Token.Keyword(
                    value = Token.Keyword.Type.RETURN,
                    position = CodePosition(
                        column = 5,
                        line = 2,
                        offset = 40,
                    )
                ),
                Token.Identifier(
                    value = "c",
                    position = CodePosition(
                        column = 12,
                        line = 2,
                        offset = 47,
                    )
                ),
                Token.AdditiveOperator(
                    position = CodePosition(
                        column = 13,
                        line = 2,
                        offset = 48,
                    )
                ),
                Token.Identifier(
                    value = "d",
                    position = CodePosition(
                        column = 14,
                        line = 2,
                        offset = 49,
                    )
                ),
                Token.Special(
                    value = Token.Special.Type.RBRACE,
                    position = CodePosition(
                        column = 1,
                        line = 3,
                        offset = 52,
                    )
                ),
                Token.EOF(
                    position = CodePosition(
                        column = 2,
                        line = 3,
                        offset = 53,
                    )
                )
            )
        )
    }
}