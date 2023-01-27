package org.example.interpreter

import org.example.errorhandler.ErrorHandler
import org.example.errorhandler.exception.interpreter.FunctionNotFoundException
import org.example.errorhandler.exception.interpreter.ImmutableVariableException
import org.example.errorhandler.exception.interpreter.InvalidCastException
import org.example.errorhandler.exception.interpreter.InvalidFunctionArgumentsNumberException
import org.example.errorhandler.exception.interpreter.InvalidOperationException
import org.example.errorhandler.exception.interpreter.InvalidRuntimeTypeException
import org.example.errorhandler.exception.interpreter.NoReturnFromFunctionException
import org.example.errorhandler.exception.interpreter.StandardLibraryFunctionOverriddenException
import org.example.errorhandler.exception.interpreter.VariableNotFoundException
import org.example.errorhandler.exception.interpreter.ZeroDivisionException
import org.example.interpreter.data.Environment
import org.example.interpreter.data.GlobalScope
import org.example.interpreter.data.Variable
import org.example.interpreter.utils.TypeConst
import org.example.interpreter.utils.canBeAssignedTo
import org.example.interpreter.utils.castType
import org.example.interpreter.utils.compare
import org.example.parser.data.AdditiveOperator
import org.example.parser.data.AdditiveOperatorWithExpression
import org.example.parser.data.BuiltInFunction
import org.example.parser.data.Expression
import org.example.parser.data.MultiplicativeOperator
import org.example.parser.data.MultiplicativeOperatorWithExpression
import org.example.parser.data.Program
import org.example.parser.data.ProgramFunction
import org.example.parser.data.RuntimeValue
import org.example.parser.data.Statement
import org.example.parser.data.Type
import org.example.parser.data.UnaryOperator
import org.example.parser.data.UserDefinedFunction

class InterpreterImpl(
    override val errorHandler: ErrorHandler,
    standardLibrary: StandardLibrary
) : Interpreter() {

    private var returnValue: RuntimeValue? = null
    private var lastValue: RuntimeValue? = null

    private fun getLastValue(): RuntimeValue? {
        if (lastValue == null) {
            errorHandler.handleInterpreterError(InvalidRuntimeTypeException("any", "void"))
            return null
        }
        return lastValue
    }

    override val environment: Environment = Environment(
        GlobalScope.getGlobalScope(),
        standardLibrary
    )

    override fun interpret(program: Program) {
        program.accept(this)
    }

    override fun visit(node: Statement.IfStatement) {
        node.condition?.accept(this)
        val condition = getLastValue() ?: return

        val conditionValue = getCheckedCastedValue<Boolean>(condition, TypeConst.BOOL) ?: return

        if (conditionValue) {
            visitBlock(node.body)
        } else {
            node.elseBody?.let { visitBlock(it) }
        }
    }

    private fun visitBlock(nodes: List<Statement>) {
        environment.newLocalScope()
        nodes.forEach {
            it.accept(this)
            if (returnValue != null) {
                environment.exitLocalScope()
                return
            }
        }
        environment.exitLocalScope()
    }

    override fun visit(node: Statement.WhileStatement) {
        node.condition?.accept(this)
        val condition = getLastValue() ?: return

        var conditionValue = getCheckedCastedValue<Boolean>(condition, TypeConst.BOOL) ?: return

        while (conditionValue) {
            visitBlock(node.body)
            node.condition?.accept(this)
            conditionValue = getLastValue()?.getCastedValue() ?: return
        }
    }

    override fun visit(node: Statement.ReturnStatement) {
        node.expression?.accept(this)
        returnValue = getLastValue() ?: return
    }

    override fun visit(node: Expression.SimpleExpression.Identifier) {
        val variable = environment.getVariable(node.identifier)
        if (variable == null) {
            errorHandler.handleInterpreterError(VariableNotFoundException(node.identifier))
            return
        }
        lastValue = variable.value
    }

    override fun visit(node: Expression.SimpleExpression.FunctionCall) {
        val function = environment.getFunction(node.identifier) ?: run {
            errorHandler.handleInterpreterError(
                FunctionNotFoundException(
                    name = node.identifier
                )
            )
            return
        }
        val args = mutableListOf<RuntimeValue>()
        node.args?.forEach {
            it.accept(this)
            args.add(getLastValue() ?: return)
        }

        if (checkFunctionArgsType(function, args)) {
            environment.newFunctionScope(
                function.parameters.zip(args).associateBy({ it.first }, { it.second })
            )
            function.accept(this)
            lastValue = returnValue
            returnValue = null
            environment.exitFunctionScope()
        }
    }

    override fun visit(node: Statement.ExpressionStatement) {
        if (node.assignedExpression != null) {
            val variableName =
                (node.expression as? Expression.SimpleExpression.Identifier)?.identifier ?: return
            val variable = environment.getVariable(variableName) ?: run {
                errorHandler.handleInterpreterError(
                    VariableNotFoundException(variableName)
                )
                return
            }
            if (variable.isImmutable) {
                errorHandler.handleInterpreterError(
                    ImmutableVariableException(variableName)
                )
                return
            }
            node.assignedExpression?.accept(this)
            val value = getLastValue() ?: return
            if (value.canBeAssignedTo(variable.type)) {
                environment.setVariable(variable.name, value)
            } else {
                errorHandler.handleInterpreterError(
                    InvalidRuntimeTypeException(variable.type.toString(), value.type.toString())
                )
            }
        } else {
            node.expression.accept(this)
        }
    }

    override fun visit(node: Statement.VariableDeclaration) {
        node.initialValue?.accept(this)
        val value = getLastValue() ?: return
        if (!value.canBeAssignedTo(node.type)) {
            errorHandler.handleInterpreterError(
                InvalidRuntimeTypeException(
                    node.type.toString(),
                    value.type.toString()
                )
            )
            return
        }
        environment.addVariable(
            Variable(
                value.type,
                node.isImmutable,
                node.name,
                value
            )
        )
    }

    override fun visit(node: Expression.AsExpression) {
        node.left.accept(this)
        val desiredType = node.type
        val value = getLastValue() ?: return

        val castedValue = value.castType(desiredType) ?: run {
            errorHandler.handleInterpreterError(
                InvalidCastException(
                    expected = desiredType.toString(),
                    actual = value.value.toString()
                )
            )
            return
        }
        lastValue = castedValue
    }

    private fun multiplyInt(left: RuntimeValue, right: List<MultiplicativeOperatorWithExpression>) {
        var result = left.getCastedValue<Int>()
        right.forEach {
            it.expression.accept(this)
            val rightRuntimeValue = getLastValue() ?: return
            val rightValue = getCheckedCastedValue<Int>(rightRuntimeValue, TypeConst.INT) ?: return
            if (it.operator == MultiplicativeOperator.Mul) {
                result *= rightValue
            } else {
                if (rightValue == 0) {
                    errorHandler.handleInterpreterError(ZeroDivisionException())
                    return
                }
                result /= rightValue
            }
        }
        lastValue = RuntimeValue(result, left.type)
    }

    private fun multiplyDouble(left: RuntimeValue, right: List<MultiplicativeOperatorWithExpression>) {
        var result = left.getCastedValue<Double>()
        right.forEach {
            it.expression.accept(this)
            val rightRuntimeValue = getLastValue() ?: return
            val rightValue = getCheckedCastedValue<Double>(rightRuntimeValue, TypeConst.DOUBLE) ?: return
            if (it.operator == MultiplicativeOperator.Mul) {
                result *= rightValue
            } else {
                if (rightValue == 0.0) {
                    errorHandler.handleInterpreterError(ZeroDivisionException())
                    return
                }
                result /= rightValue
            }
        }
        lastValue = RuntimeValue(result, left.type)
    }

    override fun visit(node: Expression.MultiplicativeExpression) {
        node.left.accept(this)
        val left = getLastValue() ?: return
        val leftType = left.type
        if (leftType.isNullable) {
            errorHandler.handleInterpreterError(
                InvalidRuntimeTypeException(
                    "Int, Double or String",
                    leftType.toString()
                )
            )
            return
        }

        when (leftType.type) {
            Type.TypePrimitive.INT -> multiplyInt(left, node.right)
            Type.TypePrimitive.DOUBLE -> multiplyDouble(left, node.right)
            else -> {
                errorHandler.handleInterpreterError(
                    InvalidRuntimeTypeException(
                        "Int or Double",
                        leftType.toString()
                    )
                )
            }
        }
    }

    private fun sumInt(left: RuntimeValue, right: List<AdditiveOperatorWithExpression>) {
        var accumulator = left.getCastedValue<Int>()
        right.forEach {
            it.expression.accept(this)
            val rightRuntimeValue = getLastValue() ?: return
            val rightValue = getCheckedCastedValue<Int>(rightRuntimeValue, TypeConst.INT) ?: return
            accumulator = when (it.operator) {
                AdditiveOperator.Plus -> accumulator + rightValue
                AdditiveOperator.Minus -> accumulator - rightValue
            }
        }
        lastValue = RuntimeValue(accumulator, left.type)
    }

    private fun sumDouble(left: RuntimeValue, right: List<AdditiveOperatorWithExpression>) {
        var accumulator = left.getCastedValue<Double>()
        right.forEach {
            it.expression.accept(this)
            val rightRuntimeValue = getLastValue() ?: return
            val rightValue = getCheckedCastedValue<Double>(rightRuntimeValue, TypeConst.DOUBLE) ?: return
            accumulator = when (it.operator) {
                AdditiveOperator.Plus -> accumulator + rightValue
                AdditiveOperator.Minus -> accumulator - rightValue
            }
        }
        lastValue = RuntimeValue(accumulator, left.type)
    }

    private fun concatStrings(left: RuntimeValue, right: List<AdditiveOperatorWithExpression>) {
        var accumulator = StringBuilder(left.getCastedValue<String>())
        right.forEach {
            it.expression.accept(this)
            val rightRuntimeValue = getLastValue() ?: return
            val rightValue = getCheckedCastedValue<String>(rightRuntimeValue, TypeConst.STRING) ?: return
            accumulator = when (it.operator) {
                AdditiveOperator.Plus -> accumulator.append(rightValue)
                else -> run {
                    errorHandler.handleInterpreterError(
                        InvalidOperationException(
                            operation = it.operator.toString(),
                            leftType = left.type.toString(),
                        )
                    )
                    return
                }
            }
        }
        lastValue = RuntimeValue(accumulator.toString(), left.type)
    }

    override fun visit(node: Expression.AdditiveExpression) {
        node.left.accept(this)
        val leftValue = getLastValue() ?: return
        val leftType = leftValue.type

        if (leftType.isNullable) {
            errorHandler.handleInterpreterError(
                InvalidRuntimeTypeException(
                    "Int, Double or String",
                    leftType.toString()
                )
            )
            return
        }

        when (leftType.type) {
            Type.TypePrimitive.INT -> sumInt(leftValue, node.right)
            Type.TypePrimitive.DOUBLE -> sumDouble(leftValue, node.right)
            Type.TypePrimitive.STRING -> concatStrings(leftValue, node.right)
            else -> errorHandler.handleInterpreterError(
                InvalidRuntimeTypeException(
                    "Int, Double or String",
                    leftType.toString()
                )
            )
        }
    }

    override fun visit(node: Expression.NullSafetyExpression) {
        node.left.accept(this)
        val leftRuntimeValue = getLastValue() ?: return
        if (leftRuntimeValue.value != null) {
            lastValue = RuntimeValue(
                leftRuntimeValue.value,
                Type(leftRuntimeValue.type.type, false)
            )
            return
        } else {
            node.right.accept(this)
        }
    }

    override fun visit(node: Expression.ComparisonExpression) {
        node.left.accept(this)
        val leftRuntimeValue = getLastValue() ?: return

        node.right.accept(this)
        val rightRuntimeValue = getLastValue() ?: return
        val rightType = rightRuntimeValue.type

        if (!leftRuntimeValue.canBeAssignedTo(rightType)) {
            errorHandler.handleInterpreterError(
                InvalidRuntimeTypeException(
                    expected = leftRuntimeValue.type.toString(),
                    actual = rightType.toString()
                )
            )
            return
        } else {
            lastValue = RuntimeValue(
                leftRuntimeValue.compare(rightRuntimeValue, node.operator),
                Type(
                    Type.TypePrimitive.BOOL,
                    false
                )
            )
        }
    }

    override fun visit(node: Expression.ConjunctionExpression) {
        node.left.accept(this)
        val leftValue = getLastValue() ?: return

        val result = getCheckedCastedValue<Boolean>(leftValue, TypeConst.BOOL) ?: return
        if (!result) {
            lastValue = RuntimeValue(false, Type(Type.TypePrimitive.BOOL, false))
            return
        }
        node.right.forEach {
            it.accept(this)
            val rightRuntimeValue = getLastValue() ?: return
            val rightValue = getCheckedCastedValue<Boolean>(rightRuntimeValue, TypeConst.BOOL) ?: return
            if (!rightValue) {
                lastValue = RuntimeValue(false, Type(Type.TypePrimitive.BOOL, false))
                return
            }
        }
        lastValue = RuntimeValue(true, Type(Type.TypePrimitive.BOOL, false))
    }

    override fun visit(node: Expression.DisjunctionExpression) {
        node.left.accept(this)
        val leftRuntimeValue = getLastValue() ?: return
        val result = getCheckedCastedValue<Boolean>(leftRuntimeValue, TypeConst.BOOL) ?: return
        if (result) {
            lastValue = RuntimeValue(true, Type(Type.TypePrimitive.BOOL, false))
            return
        }
        node.right.forEach {
            it.accept(this)
            val rightRuntimeValue = getLastValue() ?: return
            val rightValue = getCheckedCastedValue<Boolean>(rightRuntimeValue, TypeConst.BOOL) ?: return
            if (rightValue) {
                lastValue = RuntimeValue(true, Type(Type.TypePrimitive.BOOL, false))
                return
            }
        }
        lastValue = RuntimeValue(false, Type(Type.TypePrimitive.BOOL, false))
    }

    override fun visit(node: Expression.SimpleExpression.Literal) {
        lastValue = RuntimeValue(
            node.value,
            when (node.value) {
                is Int -> Type(
                    Type.TypePrimitive.INT,
                    false
                )

                is Double -> Type(
                    Type.TypePrimitive.DOUBLE,
                    false
                )

                is Boolean -> Type(
                    Type.TypePrimitive.BOOL,
                    false
                )

                is String -> Type(
                    Type.TypePrimitive.STRING,
                    false
                )

                else -> Type(
                    Type.TypePrimitive.VOID,
                    true
                )
            }
        )
    }

    override fun visit(node: Expression.UnaryExpression) {
        node.right.accept(this)
        val value = getLastValue() ?: return
        when (node.operator) {
            is UnaryOperator.Minus -> {
                if (!value.canBeUsedInArithmetic()) {
                    errorHandler.handleInterpreterError(
                        InvalidRuntimeTypeException(
                            expected = "Int or Double",
                            actual = value.type.toString()
                        )
                    )
                    return
                }
                lastValue = RuntimeValue(
                    when (value.type.type) {
                        Type.TypePrimitive.INT -> (value.getCastedValue<Int>()).unaryMinus()
                        else -> (value.getCastedValue<Double>()).unaryMinus()
                    },
                    value.type
                )
            }

            is UnaryOperator.Not -> {
                val result = getCheckedCastedValue<Boolean>(value, TypeConst.BOOL) ?: return
                lastValue = RuntimeValue(result, value.type)
            }
        }
    }

    override fun visit(node: Program) {
        node.functions.forEach {
            if (!environment.addFunction(it.value)) {
                errorHandler.handleInterpreterError(
                    StandardLibraryFunctionOverriddenException(
                        it.key
                    )
                )
            }
        }
        node.statements.forEach {
            it.accept(this)
        }
    }

    override fun visit(node: UserDefinedFunction) {
        visitBlock(node.body)
        if (returnValue == null && node.returnType != TypeConst.VOID) {
            errorHandler.handleInterpreterError(
                NoReturnFromFunctionException(
                    functionName = node.name
                )
            )
        }
    }

    override fun visit(node: BuiltInFunction) {
        val args = node.parameters.map { environment.getVariable(it.name)?.value ?: return }
        if (node.returnType.type == Type.TypePrimitive.VOID) {
            node.invokeFunction(args)
            return
        } else {
            returnValue = node.invokeFunction(args)
        }
    }

    private fun checkFunctionArgsType(function: ProgramFunction, args: List<RuntimeValue>): Boolean {
        var hasErrors = false
        if (function.parameters.size != args.size)
            errorHandler.handleInterpreterError(
                InvalidFunctionArgumentsNumberException(
                    expected = function.parameters.size,
                    actual = args.size
                )
            )

        function.parameters.zip(args).forEach { (param, arg) ->
            if (param !is TypeAnyParam && !arg.canBeAssignedTo(param.type)) {
                hasErrors = true
                errorHandler.handleInterpreterError(
                    InvalidRuntimeTypeException(
                        expected = param.type.toString(),
                        actual = arg.type.toString()
                    )
                )
            }
        }
        return !hasErrors
    }

    private fun <T> getCheckedCastedValue(value: RuntimeValue, type: Type): T? {
        if (!value.canBeAssignedTo(type)) {
            errorHandler.handleInterpreterError(
                InvalidRuntimeTypeException(
                    expected = type.toString(),
                    actual = value.type.toString()
                )
            )
            return null
        }
        return value.getCastedValue<T>()
    }

}

private fun RuntimeValue.canBeUsedInArithmetic(): Boolean {
    return !type.isNullable &&
            (type.type == Type.TypePrimitive.INT || type.type == Type.TypePrimitive.DOUBLE)
}
