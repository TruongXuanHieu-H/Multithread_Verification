package com.mtv.graph.cfg.node;

import com.mtv.DebugHelper.DebugHelper;

public class AbortNode extends CFGNode{
    @Override
    public void printNode() {
        DebugHelper.print("Abort node");
    }
}
