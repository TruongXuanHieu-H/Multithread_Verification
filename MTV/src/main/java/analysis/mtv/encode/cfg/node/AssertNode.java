package analysis.mtv.encode.cfg.node;

import analysis.mtv.debug.DebugHelper;
import org.eclipse.cdt.core.dom.ast.IASTExpression;

public class AssertNode extends CFGNode{
    public IASTExpression assertCondition;

    public AssertNode(IASTExpression assertCondition) {
        this.assertCondition = assertCondition;
    }
    @Override
    public void printNode() {
        DebugHelper.print("Assert node: " + assertCondition.getRawSignature());
    }
}
