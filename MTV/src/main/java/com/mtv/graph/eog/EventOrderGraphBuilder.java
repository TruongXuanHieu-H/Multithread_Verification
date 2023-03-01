package com.mtv.graph.eog;

import com.mtv.graph.cfg.build.ControlFlowGraph;
import com.mtv.graph.cfg.node.CFGNode;

public class EventOrderGraphBuilder {
    public static EventOrderGraph Build(ControlFlowGraph baseCFG) {
        EventOrderGraph eog = new EventOrderGraph();
        CFGNode start = baseCFG.getStart();
        start.printNode();
        return eog;
    }
}
