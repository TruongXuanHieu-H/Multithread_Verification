package com.mtv.encode.constraint;

import com.microsoft.z3.*;
import com.mtv.debug.DebugHelper;
import com.mtv.encode.eog.*;
import org.eclipse.cdt.core.dom.ast.*;

import java.util.ArrayList;

public class ConstraintManager {
    public static Context ctx = new Context();
    public static Solver solver = ctx.mkSolver();

    public static Solver BuildConstraints(EventOrderGraph eog) {
        if (eog.startNode == null) {
            return null;
        }

        EventOrderNode searchNode = eog.startNode;
        EventOrderNode interleavingNode = eog.interleavingNode;
        ArrayList<EventOrderNode> endNodes = eog.endNodes;
        CreateWriteConstraints(solver, eog);
        CreateRWLC_ProgramFromProgram(solver, eog);
        PrintAssertions(solver);
        return solver;
    }

    public static void PrintAssertions(Solver solver) {
        for (BoolExpr boolExpr: solver.getAssertions()) {
            System.out.println(boolExpr.toString());
        }
    }

    /*
    Constraints are separated into 3 parts:
    - Write constraints
    - Read/Write link constraints
    - Order constraints
     */

    // Create all write constraints in the event order graph then store them in the solver
    private static void CreateWriteConstraints(Solver solver, EventOrderGraph eventOrderGraph) {
        EventOrderNode trackNode = eventOrderGraph.startNode;
        CreateWriteConstraint(solver, trackNode);
        eventOrderGraph.ResetVisited();
    }
    private static void CreateWriteConstraint(Solver solver, EventOrderNode node) {
        if (node == null) {
            return;
        }

        if (node.isVisited) return;
        node.isVisited = true;

        if (node instanceof WriteEventNode) {
            WriteEventNode writeNode = (WriteEventNode)node;
            IntExpr writeVar = ctx.mkIntConst(writeNode.suffixVarPref);
            solver.add(ctx.mkEq(CreateCalculation(writeNode.expression), writeVar));
        }

        ArrayList<EventOrderNode> nextNodes = node.nextNodes;
        for (EventOrderNode nextNode: nextNodes) {
            CreateWriteConstraint(solver, nextNode);
        }
    }
    private static IntExpr CreateCalculation(IASTExpression expression) {
        if (expression instanceof IASTIdExpression) {
            return ctx.mkIntConst(((IASTIdExpression)expression).getName().toString());
        } else if (expression instanceof IASTBinaryExpression) {
            IASTBinaryExpression binaryExpression = (IASTBinaryExpression) expression;
            IASTExpression operand1 = binaryExpression.getOperand1();
            IASTExpression operand2 = binaryExpression.getOperand2();
            IntExpr expr1 = CreateCalculation(operand1);
            IntExpr expr2 = CreateCalculation(operand2);
            if (binaryExpression.getOperator() == 1) {
                return (IntExpr) ctx.mkMul(expr1, expr2);
            } else if (binaryExpression.getOperator() == 2) {
                return (IntExpr) ctx.mkDiv(expr1, expr2);
            } else if (binaryExpression.getOperator() == 4) {
                return (IntExpr) ctx.mkAdd(expr1, expr2);
            } else if (binaryExpression.getOperator() == 5) {
                return (IntExpr) ctx.mkSub(expr1, expr2);
            } else if (binaryExpression.getOperator() == 3) {
                return (IntExpr) ctx.mkMod(expr1, expr2);
            } else {
                System.out.println(binaryExpression.getOperator());
                return null;
            }
        } else if (expression instanceof IASTUnaryExpression) {
            IASTUnaryExpression unaryExpression = (IASTUnaryExpression) expression;
            return CreateCalculation(unaryExpression.getOperand());
        } else if (expression instanceof IASTLiteralExpression) {
            IASTLiteralExpression literalExpression = (IASTLiteralExpression) expression;
            return ctx.mkInt(literalExpression.toString());
        } else {
            System.out.println(expression.getClass().toString());
            return null;
        }
    }

    // Create all read/write link constraints in the event order graph then store them in the solver
    private static void CreateRWLC_ProgramFromProgram(Solver solver, EventOrderGraph eog) {
        // This function creates all read/write link constraints (RWLC) between all read and write events in the program
        if (eog.interleavingNode == null) {
            DebugHelper.print("No interleaving detected!");
        } else if (eog.interleavingNode.nextNodes.size() < 2) {
            DebugHelper.print("No multi thread implemented.");
        }

        // TODO: Create pre-interleaving read-write link constraints
        EventOrderNode preInterSearchNode = eog.startNode;
        while (preInterSearchNode != null) {
            if (preInterSearchNode.varPreference != null) {
                if (preInterSearchNode instanceof ReadEventNode) {
                    EventOrderNode previosNode = preInterSearchNode.previousNodes.get(0);
                    while ((!(previosNode instanceof WriteEventNode)) || (!(previosNode.varPreference.equals(preInterSearchNode.varPreference)))) {
                        if (previosNode.previousNodes.size() == 0) {
                            break;
                        } else {
                            previosNode = previosNode.previousNodes.get(0);
                        }
                    }
                    if (previosNode instanceof WriteEventNode && previosNode.varPreference.equals(preInterSearchNode.varPreference)) {
                        String signature = CreateRWLC_NodeFromNode(solver, (ReadEventNode) preInterSearchNode, (WriteEventNode) previosNode);
                        CreateRWLC_OneAmongAll(solver, new String[] {signature});
                    }
                }
            } else {
                DebugHelper.print("Empty event node.");
            }
            if (preInterSearchNode == eog.interleavingNode || preInterSearchNode.nextNodes.size() == 0) break;
            preInterSearchNode = preInterSearchNode.nextNodes.get(0);
        }

        if (eog.interleavingNode != null) {
            // TODO: Create interleaving read-write link constraints
            ArrayList<EventOrderNode> firstThreadNodes = eog.interleavingNode.nextNodes;
            for (EventOrderNode beginThreadNode : firstThreadNodes) {
                CreateRWLC_ThreadFromProgram(solver, beginThreadNode, eog);
            }
        }


        // TODO: Create post-interleaving read-write link constraints
        if (eog.interleavingNode != null) {

        }
    }
    private static void CreateRWLC_ThreadFromProgram(Solver solver, EventOrderNode beginThreadNode, EventOrderGraph eog) {
        // This function creates all read/write link constraints (RWLC) between all read events in a single thread and all write events in the program
        EventOrderNode searchNode = beginThreadNode;
        // If thread is not joined, it doesn't have node that is marked as InterleavingMarker.End
        // So, we travel until reach null node or node that marked as InterleavingMarker.End
        while (searchNode != null) {
            if (searchNode instanceof ReadEventNode) {
                CreateRWLC_NodeFromProgram(solver, (ReadEventNode) searchNode, eog);
            }

            // Stop when
            if (searchNode.interleavingTracker.GetMarker() == InterleavingTracker.InterleavingMarker.End) {
                break;
            }

            if (searchNode.nextNodes.size() == 0) break;
            searchNode = searchNode.nextNodes.get(0);
        }
    }
    private static void CreateRWLC_NodeFromProgram(Solver solver, ReadEventNode readNode, EventOrderGraph eog) {
        // This function creates all read/write link constraints (RWLC) between a read events and all write events in the program
        readNode.printNode(4);
        ArrayList<EventOrderNode> searchNodes = new ArrayList<>();
        ArrayList<WriteEventNode> validWriteNodes = new ArrayList<>();
        if (eog.startNode == null) {
            return;
        }

        // Find all write nodes corresponding with this read node
        searchNodes.add(eog.startNode);
        while (searchNodes.size() > 0) {
            EventOrderNode searchNode = searchNodes.get(0);
            searchNodes.remove(searchNode);

            if (!CheckChildNode(readNode, searchNode)) {
                searchNodes.addAll(searchNode.nextNodes);
                if (searchNode instanceof WriteEventNode && searchNode.varPreference.equals(readNode.varPreference) ) {
                    validWriteNodes.add((WriteEventNode) searchNode);
                }
            }
        }

        // Remove wrong write node from corresponding write nodes
        // Eg: Assume we have Write_x1, Write_x2 correspond with Read_x3,
        // and we have chain Write_x1 -> Write_x2 -> Read_x3.
        // Definitely, Read_x3 never read the value written by Write_x1,
        // so we remove Write_x1 from corresponding write nodes
        ArrayList<EventOrderNode> wrongNodes = new ArrayList<>();
        for (EventOrderNode first: validWriteNodes) {
            for (EventOrderNode second: validWriteNodes) {
                if (first == second) continue;
                if (CheckChildNode(first, second) && CheckChildNode(second, readNode)) {
                    wrongNodes.add(first);
                }
            }
        }
        for (EventOrderNode wrongNode: wrongNodes) {
            validWriteNodes.remove(wrongNode);
        }

        String[] signatures = new String[validWriteNodes.size()];
        for (int i = 0; i < validWriteNodes.size(); i++) {
            signatures[i] = CreateRWLC_NodeFromNode(solver, readNode, validWriteNodes.get(i));
        }
        CreateRWLC_OneAmongAll(solver, signatures);
    }
    private static String CreateRWLC_NodeFromNode(Solver solver, ReadEventNode readNode, WriteEventNode writeNode) {
        // This function creates all read/write link constraints (RWLC) between a read events and a write events
        // This function returns the signature of created RWLC
        String signature = "E_" + readNode.suffixVarPref + "_" + writeNode.suffixVarPref;
        solver.add(ctx.mkImplies(ctx.mkBoolConst(signature), ctx.mkEq(ctx.mkIntConst(readNode.suffixVarPref), ctx.mkIntConst(writeNode.suffixVarPref))));
        return signature;
    }
    private static void CreateRWLC_OneAmongAll(Solver solver, String[] signatures) {
        // This function create relationship between read/write link constraints based on their signatures;
        // It guarantees that among all the RWLC, there is one and only one RWLC is true, all others are false
        Expr[] signals = new Expr[signatures.length];
        for (int i = 0; i < signatures.length; i++) {
            Expr expr= ctx.mkBoolConst(signatures[i]);
            signals[i] = expr;
        }
        solver.add(ctx.mkAtLeast(signals, 1));
        solver.add(ctx.mkAtMost(signals, 1));
    }

    private static boolean CheckChildNode(EventOrderNode root, EventOrderNode check) {
        boolean result = false;
        if (check == root) {
            result = true;
        }
        for (EventOrderNode nextNode : root.nextNodes) {
            if (CheckChildNode(nextNode, check)) {
                result = true;
            }
        }
        return result;
    }


}
