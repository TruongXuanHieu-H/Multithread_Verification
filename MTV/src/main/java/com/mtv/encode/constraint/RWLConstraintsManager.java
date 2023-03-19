package com.mtv.encode.constraint;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Solver;
import com.mtv.debug.DebugHelper;
import com.mtv.encode.eog.*;
import org.javatuples.Triplet;

import java.util.ArrayList;

public class RWLConstraintsManager {
    private static void AddEmptyConstraint(Context ctx, Solver solver) {
        solver.add(ctx.mkBool(true));
    }
    // Create all read/write link constraints in the event order graph using given context then store them in the given solver
    // Return all read/write link constraints' signatures
    public static ArrayList<Triplet<String, ReadEventNode, WriteEventNode>> CreateRWLC_ProgramFromProgram(Context ctx, Solver solver, EventOrderGraph eog) {
        // This function creates all read/write link constraints (RWLC) between all read and write events in the program
        if (eog.interleavingNode == null) {
            DebugHelper.print("No interleaving detected!");
        } else if (eog.interleavingNode.nextNodes.size() < 2) {
            DebugHelper.print("No multi thread implemented.");
        }
        ArrayList<Triplet<String, ReadEventNode, WriteEventNode>> RWLSignatures = new ArrayList<>();

        // TODO: Create pre-interleaving read-write link constraints
        EventOrderNode preInterSearchNode = eog.startNode;
        while (preInterSearchNode != null) {
            if (preInterSearchNode.varPreference != null) {
                if (preInterSearchNode instanceof ReadEventNode) {
                    EventOrderNode previosNode = preInterSearchNode.previousNodes.get(0);
                    while ((!(previosNode instanceof WriteEventNode))
                            || (!(previosNode.varPreference.equals(preInterSearchNode.varPreference)))) {
                        if (previosNode.previousNodes.size() == 0) {
                            break;
                        } else {
                            previosNode = previosNode.previousNodes.get(0);
                        }
                    }
                    if (previosNode instanceof WriteEventNode
                            && previosNode.varPreference.equals(preInterSearchNode.varPreference)) {
                        String signature = CreateRWLC_NodeFromNode(ctx, solver, RWLSignatures,
                                (ReadEventNode) preInterSearchNode, (WriteEventNode) previosNode);
                        CreateRWLC_OneAmongAll(ctx, solver, new String[] {signature});
                    }
                }
            } else {
                DebugHelper.print("Empty event node.");
            }
            if (preInterSearchNode == eog.interleavingNode || preInterSearchNode.nextNodes.size() == 0) break;
            preInterSearchNode = preInterSearchNode.nextNodes.get(0);
        }
        AddEmptyConstraint(ctx, solver);

        // TODO: Create interleaving read-write link constraints
        // If interleavingNode is not exist, then the program is not interleave,
        // so we don't have to create interleaving RWLC
        if (eog.interleavingNode != null) {
            ArrayList<EventOrderNode> firstThreadNodes = eog.interleavingNode.nextNodes;
            for (EventOrderNode beginThreadNode : firstThreadNodes) {
                CreateRWLC_ThreadFromProgram(ctx, solver, RWLSignatures, beginThreadNode, eog);
            }
        }

        AddEmptyConstraint(ctx, solver);
        // TODO: Create post-interleaving read-write link constraints
        // If interleavingNode is not exist, then the program is not interleave,
        // so we don't have to create post-interleaving RWLC
        if (eog.interleavingNode != null) {
            // First, we have to find all end interleaving nodes
            ArrayList<EventOrderNode> endInterleavingNodes = eog.interleavingNode.interleavingTracker.GetRelatedNodes();

            // Then, we find all nodes after interleaving
            ArrayList<EventOrderNode> searchNodes = new ArrayList<>();
            ArrayList<ReadEventNode> postInterReadNodes = new ArrayList<>();

            for (EventOrderNode endInterNode: endInterleavingNodes) {
                searchNodes.addAll(endInterNode.nextNodes);
            }
            while (searchNodes.size() > 0) {
                EventOrderNode node = searchNodes.get(0);
                searchNodes.remove(node);
                searchNodes.addAll(node.nextNodes);
                if (node instanceof ReadEventNode && !postInterReadNodes.contains((ReadEventNode) node))
                    postInterReadNodes.add((ReadEventNode) node);
            }
            for (ReadEventNode readEventNode: postInterReadNodes) {
                CreateRWLC_NodeFromProgram(ctx, solver, RWLSignatures,readEventNode, eog);
            }
        }
        AddEmptyConstraint(ctx, solver);
        return RWLSignatures;
    }
    private static void CreateRWLC_ThreadFromProgram(Context ctx, Solver solver,
                                                     ArrayList<Triplet<String, ReadEventNode, WriteEventNode>> RWLSignatures,
                                                     EventOrderNode beginThreadNode, EventOrderGraph eog) {
        // This function creates all read/write link constraints (RWLC) between all read events in a single thread and all write events in the program
        EventOrderNode searchNode = beginThreadNode;
        // If thread is not joined, it doesn't have node that is marked as InterleavingMarker.End
        // So, we travel until reach null node or node that marked as InterleavingMarker.End
        while (searchNode != null) {
            if (searchNode instanceof ReadEventNode) {
                CreateRWLC_NodeFromProgram(ctx, solver, RWLSignatures, (ReadEventNode) searchNode, eog);
            }

            // Stop when
            if (searchNode.interleavingTracker.GetMarker() == InterleavingTracker.InterleavingMarker.End) {
                break;
            }

            if (searchNode.nextNodes.size() == 0) break;
            searchNode = searchNode.nextNodes.get(0);
        }
    }
    private static void CreateRWLC_NodeFromProgram(Context ctx, Solver solver,
                                                   ArrayList<Triplet<String, ReadEventNode, WriteEventNode>> RWLSignatures,
                                                   ReadEventNode readNode, EventOrderGraph eog) {
        // This function creates all read/write link constraints (RWLC) between a read event and all write events in the program
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
                if (searchNode instanceof WriteEventNode
                        && searchNode.varPreference.equals(readNode.varPreference) ) {
                    validWriteNodes.add((WriteEventNode) searchNode);
                }
            }
        }

        // Remove wrong write node from corresponding write nodes
        // Eg: Assume we have Write_x1, Write_x2 correspond with Read_x3,
        // and we have chain Write_x1 -> Write_x2 -> Read_x3.
        // Definitely, Read_x3 never read the value written by Write_x1,
        // so we remove Write_x1 from corresponding write nodes
        ArrayList<WriteEventNode> wrongNodes = new ArrayList<>();
        for (WriteEventNode first: validWriteNodes) {
            for (WriteEventNode second: validWriteNodes) {
                if (first == second) continue;
                if (CheckChildNode(first, second) && CheckChildNode(second, readNode)) {
                    wrongNodes.add(first);
                }
            }
        }
        for (WriteEventNode wrongNode: wrongNodes) {
            validWriteNodes.remove(wrongNode);
        }

        String[] signatures = new String[validWriteNodes.size()];
        for (int i = 0; i < validWriteNodes.size(); i++) {
            signatures[i] = CreateRWLC_NodeFromNode(ctx, solver, RWLSignatures,readNode, validWriteNodes.get(i));
        }
        CreateRWLC_OneAmongAll(ctx, solver, signatures);
    }
    private static String CreateRWLC_NodeFromNode(Context ctx, Solver solver,
                                                  ArrayList<Triplet<String, ReadEventNode, WriteEventNode>> RWLSignatures,
                                                  ReadEventNode readNode, WriteEventNode writeNode) {
        // This function creates all read/write link constraints (RWLC) between a read event and a write event
        // This function returns the signature of created RWLC
        String signature = CreateRWLCSignature(readNode, writeNode);
        solver.add(ctx.mkImplies(ctx.mkBoolConst(signature),
                                 ctx.mkEq(ctx.mkIntConst(readNode.suffixVarPref),
                                          ctx.mkIntConst(writeNode.suffixVarPref))));
        RWLSignatures.add(new Triplet<>(signature, readNode, writeNode));
        return signature;
    }
    private static void CreateRWLC_OneAmongAll(Context ctx, Solver solver, String[] signatures) {
        // This function create relationship between read/write link constraints based on their signatures,
        // guarantees that among all the given RWLC, there is one and only one RWLC is true, all others are false
        Expr[] signals = new Expr[signatures.length];
        for (int i = 0; i < signatures.length; i++) {
            Expr expr= ctx.mkBoolConst(signatures[i]);
            signals[i] = expr;
        }
        solver.add(ctx.mkAtLeast(signals, 1));
        solver.add(ctx.mkAtMost(signals, 1));
    }
    public static String CreateRWLCSignature(ReadEventNode readEventNode, WriteEventNode writeEventNode) {
        return "E_" + readEventNode.suffixVarPref + "_" + writeEventNode.suffixVarPref;
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
