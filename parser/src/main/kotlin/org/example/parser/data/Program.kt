package org.example.parser.data


data class Program(
    val statements: List<Statement>,
    val functions: Map<String, ProgramFunction>
) : Visitable {
    override fun accept(visitor: Visitor): Any {
        return visitor.visit(this)
    }
}
