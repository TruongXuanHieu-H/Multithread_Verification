package com.mtv.encode.constraint;

import com.microsoft.z3.Context;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.Solver;
import com.mtv.encode.eog.EventOrderGraph;
import com.mtv.encode.eog.EventOrderNode;
import com.mtv.encode.eog.WriteEventNode;
import org.eclipse.cdt.core.dom.ast.*;

import java.util.ArrayList;


public class WriteConstraintsManager {
    private static void AddEmptyConstraint(Context ctx, Solver solver) {
        solver.add(ctx.mkBool(true));
    }

    // Create all write constraints in the event order graph using given context then store them in the given solver
    public static void CreateWriteConstraints(Context ctx, Solver solver, EventOrderGraph eventOrderGraph) {
        EventOrderNode trackNode = eventOrderGraph.startNode;
        CreateWriteConstraint(ctx, solver, trackNode);
        eventOrderGraph.ResetVisited();
        AddEmptyConstraint(ctx, solver);
    }
    private static void CreateWriteConstraint(Context ctx, Solver solver, EventOrderNode node) {
        if (node == null) {
            return;
        }

        if (node.isVisited) return;
        node.isVisited = true;

        if (node instanceof WriteEventNode writeNode) {
            IntExpr writeVar = ctx.mkIntConst(writeNode.suffixVarPref);
            solver.add(ctx.mkEq(CreateCalculation(ctx, writeNode.expression), writeVar));
        }

        ArrayList<EventOrderNode> nextNodes = node.nextNodes;
        for (EventOrderNode nextNode: nextNodes) {
            CreateWriteConstraint(ctx, solver, nextNode);
        }
    }
    private static IntExpr CreateCalculation(Context ctx, IASTExpression expression) {
        if (expression instanceof IASTIdExpression) {
            return ctx.mkIntConst(((IASTIdExpression)expression).getName().toString());
        } else if (expression instanceof IASTBinaryExpression binaryExpression) {
            IASTExpression operand1 = binaryExpression.getOperand1();
            IASTExpression operand2 = binaryExpression.getOperand2();
            IntExpr expr1 = CreateCalculation(ctx, operand1);
            IntExpr expr2 = CreateCalculation(ctx, operand2);
            if (binaryExpression.getOperator() == 1) {
                return (IntExpr) ctx.mkMul(expr1, expr2);
            } else if (binaryExpression.getOperator() == 2) {
                return (IntExpr) ctx.mkDiv(expr1, expr2);
            } else if (binaryExpression.getOperator() == 4) {
                return (IntExpr) ctx.mkAdd(expr1, expr2);
            } else if (binaryExpression.getOperator() == 5) {
                return (IntExpr) ctx.mkSub(expr1, expr2);
            } else if (binaryExpression.getOperator() == 3) {
                return ctx.mkMod(expr1, expr2);
            } else {
                System.out.println(binaryExpression.getOperator());
                return null;
            }
        } else if (expression instanceof IASTUnaryExpression unaryExpression) {
            return CreateCalculation(ctx, unaryExpression.getOperand());
        } else if (expression instanceof IASTLiteralExpression literalExpression) {
            return ctx.mkInt(literalExpression.toString());
        } else {
            System.out.println(expression.getClass().toString());
            return null;
        }
    }
}
