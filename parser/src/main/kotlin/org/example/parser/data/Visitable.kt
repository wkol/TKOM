package org.example.parser.data


fun interface Visitable {
    fun accept(visitor: Visitor): Any?
}

interface Visitor {
    fun visit(node: Statement.IfStatement)
    fun visit(node: Statement.WhileStatement)
    fun visit(node: Statement.VariableDeclaration)
    fun visit(node: Statement.ReturnStatement)
    fun visit(node: Statement.ExpressionStatement)
    fun visit(node: Expression.AsExpression)
    fun visit(node: Expression.MultiplicativeExpression)
    fun visit(node: Expression.AdditiveExpression)
    fun visit(node: Expression.NullSafetyExpression)
    fun visit(node: Expression.ComparisonExpression)
    fun visit(node: Expression.ConjunctionExpression)
    fun visit(node: Expression.DisjunctionExpression)
    fun visit(node: Expression.SimpleExpression.Literal)
    fun visit(node: Expression.SimpleExpression.Identifier)
    fun visit(node: Expression.SimpleExpression.FunctionCall)
    fun visit(node: Expression.UnaryExpression)
    fun visit(node: Program)
    fun visit(node: BuiltInFunction)
    fun visit(node: UserDefinedFunction)
}
