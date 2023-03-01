package com.mtv.graph.eog;

import com.mtv.DebugHelper.DebugHelper;
import com.mtv.graph.cfg.build.ControlFlowGraph;

import java.util.Random;
import java.util.random.RandomGenerator;

public class EventOrderGraph {
    public EventOrderNode startNode;
    public EventOrderNode endNode;


    public EventOrderGraph() {
        startNode = null;
        endNode = null;
    }

    public EventOrderGraph(EventOrderNode startNode, EventOrderNode endNode) {
        this.startNode = startNode;
        this.endNode = endNode;
    }

    public EventOrderGraph(EventOrderNode startNode) {
        this.startNode = startNode;
        this.endNode = startNode;
        while (endNode.nextNode != null) {
            endNode = endNode.nextNode;
        }
    }

    public void printEOG(boolean isReversed) {
        if (startNode == null || endNode == null) {
            DebugHelper.print("Empty Event order graph.");
            return;
        }
        if (!isReversed) {
            EventOrderNode node = startNode;
            while (node != null) {
                node.printNode(8);
                node = node.nextNode;
            }
        } else {
            EventOrderNode node = endNode;
            while (node != null) {
                node.printNode(8);
                node = node.previousNode;
            }
        }

    }

    public void AddEOG(EventOrderGraph other) {
        if (other.startNode == null) {

        } else {
            this.endNode.nextNode = other.startNode;
            other.startNode.previousNode = this.endNode;
            this.endNode = other.endNode;
        }
    }



    public static void main(String[] args) {
        EventOrderGraph graph = new EventOrderGraph(new EventOrderNode("x", EventOrderAction.Read));
        String[] varPres = {"x", "y", "m", "n"};
        EventOrderAction[] eOA = {EventOrderAction.Read, EventOrderAction.Write};
        RandomGenerator rGen = new Random();
        for (int i = 0; i < 10; i++) {
            String varPre = varPres[rGen.nextInt(4)];
            EventOrderAction action = eOA[rGen.nextInt(2)];
            DebugHelper.print("Add node: " + varPre + (action == EventOrderAction.Read?" Read": " Write"));
            EventOrderNode node = new EventOrderNode(varPre, action);
            EventOrderGraph sub = new EventOrderGraph(node);
            graph.AddEOG(sub);
        }
        DebugHelper.print("--------------------------------");
        graph.printEOG(false);
        DebugHelper.print("--------------------------------");
        graph.printEOG(true);
        DebugHelper.print("--------------------------------");
    }

}
