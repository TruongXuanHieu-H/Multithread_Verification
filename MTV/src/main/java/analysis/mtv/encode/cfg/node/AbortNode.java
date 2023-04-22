package analysis.mtv.encode.cfg.node;

import analysis.mtv.debug.DebugHelper;

public class AbortNode extends CFGNode{
    @Override
    public void printNode() {
        DebugHelper.print("Abort node");
    }
}
