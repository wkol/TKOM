package org.example.parser.data

abstract class ProgramFunction : Visitable {
    abstract val name: String
    abstract val parameters: List<Parameter>
    abstract val returnType: Type
    abstract val body: List<Statement>
}

data class UserDefinedFunction(
    override val name: String,
    override val parameters: List<Parameter>,
    override val returnType: Type,
    override val body: List<Statement>
) : ProgramFunction() {
    override fun accept(visitor: Visitor): Any {
        return visitor.visit(this)
    }
}

data class BuiltInFunction(
    val invokeFunction: (List<RuntimeValue>) -> RuntimeValue,
    override val name: String,
    override val parameters: List<Parameter>,
    override val returnType: Type
) : ProgramFunction() {
    override val body: List<Statement> = emptyList()
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}
