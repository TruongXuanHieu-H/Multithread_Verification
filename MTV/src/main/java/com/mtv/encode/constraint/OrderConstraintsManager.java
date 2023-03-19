package com.mtv.encode.constraint;

import com.microsoft.z3.*;
import com.mtv.debug.DebugHelper;
import com.mtv.encode.eog.EventOrderGraph;
import com.mtv.encode.eog.EventOrderNode;
import com.mtv.encode.eog.ReadEventNode;
import com.mtv.encode.eog.WriteEventNode;
import org.antlr.v4.runtime.misc.Triple;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OrderConstraintsManager {
    private static void AddEmptyConstraint(Context ctx, Solver solver) {
        solver.add(ctx.mkBool(true));
    }




    // Create all read/write link constraints in the event order graph using given context then store them in the given solver
    public static void CreateOrderConstraints(Context ctx, Solver solver,
                                              ArrayList<Triplet<String, ReadEventNode, WriteEventNode>> RWLSignatures,
                                              EventOrderGraph eog) {
        HashMap<ReadEventNode, Pair<ArrayList<EventOrderNode>, ArrayList<EventOrderNode>>> readEventTracker = new HashMap<>();
        HashMap<WriteEventNode, Pair<ArrayList<EventOrderNode>, ArrayList<EventOrderNode>>> writeEventTracker = new HashMap<>();
        ArrayList<String> availableRWLSignatures = new ArrayList<>();
        for (Triplet RWLSignature: RWLSignatures) {
            CreateReadTracker((ReadEventNode) RWLSignature.getValue(1), readEventTracker);
            CreateWriteTracker((WriteEventNode) RWLSignature.getValue(2), writeEventTracker);
            availableRWLSignatures.add(RWLSignature.getValue(0).toString());
            DebugHelper.print(RWLSignature.getValue(0).toString());
        }

        DebugHelper.print("Print entries:");
        for (Map.Entry<ReadEventNode, Pair<ArrayList<EventOrderNode>, ArrayList<EventOrderNode>>> readEntry: readEventTracker.entrySet()) {
            for (Map.Entry<WriteEventNode, Pair<ArrayList<EventOrderNode>, ArrayList<EventOrderNode>>> writeEntry: writeEventTracker.entrySet()) {
                if (readEntry.getKey().varPreference.equals(writeEntry.getKey().varPreference)) {
                    String positiveSignature = RWLConstraintsManager.CreateRWLCSignature(readEntry.getKey(), writeEntry.getKey());
                    ArrayList<String> negativeSignatures = CreateNegativeSignatures(readEntry, writeEntry);

                    ArrayList<BoolExpr> availableNegativeSignatures = new ArrayList<>();
                    System.out.printf(positiveSignature + " => ");
                    for (String negativeSignature: negativeSignatures) {
                        if (availableRWLSignatures.contains(negativeSignature)) {
                            availableNegativeSignatures.add(ctx.mkBoolConst(negativeSignature));
                        }
                        System.out.printf(negativeSignature + " - ");
                    }
                    System.out.println("");
                    System.out.println("");
                    if (availableNegativeSignatures.size() > 0) {
                        BoolExpr negativeExpression = ctx.mkNot(ctx.mkOr(availableNegativeSignatures.toArray(new BoolExpr[0])));
                        BoolExpr fullExpression = ctx.mkImplies(ctx.mkBoolConst(positiveSignature), negativeExpression);
                        solver.add(fullExpression);
                    }

                }
            }
        }
    }

    public static void main(String[] args) {
        // Create a new context
        Context ctx = new Context();

        // Create a list of boolean expressions
        ArrayList<BoolExpr> exprList = new ArrayList<>();

        // Add some expressions to the list
        exprList.add(ctx.mkBoolConst("p"));
        exprList.add(ctx.mkBoolConst("q"));
        exprList.add(ctx.mkBoolConst("r"));

        // Add additional expressions to the list dynamically (e.g. from user input)
        exprList.add(ctx.mkBoolConst("s"));
        exprList.add(ctx.mkBoolConst("t"));

        // Create an OR expression using the mkOr method
        BoolExpr orExpr = ctx.mkOr(exprList.toArray(new BoolExpr[0]));

        // Check if the OR expression is satisfiable
        Solver solver = ctx.mkSolver();
        solver.add(orExpr);
        Status status = solver.check();

        // Print the solution if it exists
        if (status == Status.SATISFIABLE) {
            Model model = solver.getModel();
            for (BoolExpr expr : exprList) {
                System.out.println(expr + " = " + model.eval(expr, true));
            }
        } else {
            System.out.println("No solution found.");
        }

        // Dispose the context to release resources
        ctx.close();
    }

    private static void CreateReadTracker(ReadEventNode readEventNode, HashMap<ReadEventNode, Pair<ArrayList<EventOrderNode>, ArrayList<EventOrderNode>>> readEventTrackerMap) {
        if (readEventTrackerMap.containsKey(readEventNode)) {
            return;
        }
        ArrayList<EventOrderNode> previousNodes = FindPreviousNodes(readEventNode);
        ArrayList<EventOrderNode> followingNodes = FindFollowingNodes(readEventNode);
        readEventTrackerMap.put(readEventNode, new Pair<>(previousNodes, followingNodes));
    }


    private static void CreateWriteTracker(WriteEventNode writeEventNode, HashMap<WriteEventNode, Pair<ArrayList<EventOrderNode>, ArrayList<EventOrderNode>>> writeEventTrackerMap) {
        if (writeEventTrackerMap.containsKey(writeEventNode)) {
            return;
        }
        ArrayList<EventOrderNode> previousNodes = FindPreviousNodes(writeEventNode);
        ArrayList<EventOrderNode> followingNodes = FindFollowingNodes(writeEventNode);
        writeEventTrackerMap.put(writeEventNode, new Pair<>(previousNodes, followingNodes));
    }

    private static ArrayList<EventOrderNode> FindPreviousNodes(EventOrderNode node) {
        ArrayList<EventOrderNode> previousNodes = new ArrayList<>();
        ArrayList<EventOrderNode> searchNodes = new ArrayList<>(node.previousNodes);

        while (searchNodes.size() > 0) {
            EventOrderNode searchNode = searchNodes.get(0);
            previousNodes.add(searchNode);
            searchNodes.remove(searchNode);
            searchNodes.addAll(searchNode.previousNodes);
        }
        return previousNodes;
    }
    private static ArrayList<EventOrderNode> FindFollowingNodes(EventOrderNode node) {
        ArrayList<EventOrderNode> followingNodes = new ArrayList<>();
        ArrayList<EventOrderNode> searchNodes = new ArrayList<>(node.nextNodes);

        while (searchNodes.size() > 0) {
            EventOrderNode searchNode = searchNodes.get(0);
            followingNodes.add(searchNode);
            searchNodes.remove(searchNode);
            searchNodes.addAll(searchNode.nextNodes);
        }
        return followingNodes;
    }

    private static ArrayList<String> CreateNegativeSignatures(Map.Entry<ReadEventNode, Pair<ArrayList<EventOrderNode>, ArrayList<EventOrderNode>>> readEntry,
                                                       Map.Entry<WriteEventNode, Pair<ArrayList<EventOrderNode>, ArrayList<EventOrderNode>>> writeEntry) {
        DebugHelper.print("Create negative signatures for:");
        readEntry.getKey().printNode(4);
        writeEntry.getKey().printNode(4);
        ArrayList<String> negativeSignatures = new ArrayList<>();
        for (EventOrderNode previousRead: readEntry.getValue().getValue0()) {
            for (EventOrderNode afterWrite: writeEntry.getValue().getValue1()) {
                if ((CheckChildNode(previousRead, writeEntry.getKey()) || CheckChildNode(writeEntry.getKey(), previousRead))
                        && (CheckChildNode(afterWrite, readEntry.getKey()) || CheckChildNode(readEntry.getKey(), afterWrite))) {
                    // Duplicate constraints
                } else if (previousRead instanceof ReadEventNode pR_Read) {
                    if (afterWrite instanceof ReadEventNode aW_Read) {
                        // Nothing happens due to no connection established between 2 read nodes
                    } else if (afterWrite instanceof WriteEventNode aW_Write) {
                        if (pR_Read.varPreference.equals(aW_Write.varPreference)
                            && pR_Read.varPreference.equals(readEntry.getKey().varPreference)) {
                            DebugHelper.print(pR_Read.suffixVarPref + " = " + aW_Write.suffixVarPref);
                            negativeSignatures.add(RWLConstraintsManager.CreateRWLCSignature(pR_Read, aW_Write));
                        }
                    }
                } else if (previousRead instanceof WriteEventNode pR_Write) {
                    if (afterWrite instanceof ReadEventNode aW_Read) {
                        if (pR_Write.varPreference.equals(writeEntry.getKey().varPreference)
                                && pR_Write.varPreference.equals(aW_Read.varPreference)) {
                            DebugHelper.print(pR_Write.suffixVarPref + " = " + aW_Read.suffixVarPref);
                            negativeSignatures.add(RWLConstraintsManager.CreateRWLCSignature(aW_Read, pR_Write));
                        }
                    } else if (afterWrite instanceof WriteEventNode aW_Write) {
                        // Nothing happens due to no connection established between 2 write nodes
                    }
                }
            }
        }
        for (EventOrderNode afterRead: readEntry.getValue().getValue1()) {
            for (EventOrderNode previousWrite: writeEntry.getValue().getValue0()) {
                if ((CheckChildNode(afterRead, writeEntry.getKey()) || CheckChildNode(writeEntry.getKey(), afterRead))
                        && (CheckChildNode(previousWrite, readEntry.getKey()) || CheckChildNode(readEntry.getKey(), previousWrite))) {
                    // Duplicate constraints
                } else if (afterRead instanceof ReadEventNode aR_Read) {
                    if (previousWrite instanceof ReadEventNode pW_Read) {
                        // Nothing happens due to no connection established between 2 read nodes
                    } else if (previousWrite instanceof WriteEventNode pW_Write) {
                        if (pW_Write.varPreference.equals(writeEntry.getKey().varPreference)
                                && pW_Write.varPreference.equals(aR_Read.varPreference)) {
                            negativeSignatures.add(RWLConstraintsManager.CreateRWLCSignature(aR_Read, pW_Write));
                            DebugHelper.print(pW_Write.suffixVarPref + " = " + aR_Read.suffixVarPref);
                        }
                    }
                } else if (afterRead instanceof WriteEventNode aR_Write) {
                    if (previousWrite instanceof ReadEventNode pW_Read) {
                        if (pW_Read.varPreference.equals(aR_Write.varPreference)) {
                            negativeSignatures.add(RWLConstraintsManager.CreateRWLCSignature(pW_Read, aR_Write));
                            DebugHelper.print(aR_Write.suffixVarPref + " = " + pW_Read.suffixVarPref);
                        }
                    } else if (previousWrite instanceof WriteEventNode pW_Write) {
                        // Nothing happens due to no connection established between 2 write nodes
                    }
                }
            }
        }
        return negativeSignatures;
    }

    private static boolean CheckChildNode(EventOrderNode root, EventOrderNode check) {
        if (check == root) {
            return true;
        }
        boolean result = false;
        for (EventOrderNode nextNode : root.nextNodes) {
            if (CheckChildNode(nextNode, check)) {
                result = true;
                break;
            }
        }
        return result;
    }


}
