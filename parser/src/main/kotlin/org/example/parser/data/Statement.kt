package org.example.parser.data

sealed interface Statement : Visitable {
    data class IfStatement(
        val condition: Expression?,
        val body: List<Statement>,
        val elseBody: List<Statement>? = null,
    ) : Statement {
        override fun accept(visitor: Visitor) {
            visitor.visit(this)
        }
    }

    data class WhileStatement(
        val condition: Expression?,
        val body: List<Statement>,
    ) : Statement {
        override fun accept(visitor: Visitor) {
            visitor.visit(this)
        }
    }

    data class ReturnStatement(
        val expression: Expression?,
    ) : Statement {
        override fun accept(visitor: Visitor) {
            visitor.visit(this)
        }
    }

    data class ExpressionStatement(
        val expression: Expression,
        val assignedExpression: Expression? = null,
    ) : Statement {
        override fun accept(visitor: Visitor) {
            visitor.visit(this)
        }
    }

    data class VariableDeclaration(
        val isImmutable: Boolean,
        val name: String,
        val type: Type?,
        val initialValue: Expression? = null,
    ) : Statement {
        override fun accept(visitor: Visitor) {
            visitor.visit(this)
        }
    }
}
