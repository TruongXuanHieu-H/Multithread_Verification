package com.mtv.graph.cfg.node;

import com.mtv.DebugHelper.DebugHelper;
import com.mtv.graph.ast.FunctionHelper;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;

public class EndFunctionNode extends CFGNode {
    IASTFunctionDefinition func;

    public EndFunctionNode() {
    }

    public EndFunctionNode(CFGNode node) {
        super(node);
    }

    public EndFunctionNode(IASTFunctionDefinition func) {
        this.func = func;
        DebugHelper.print("Function " + func.getDeclarator().getName() + " is closed");
    }

    public IASTFunctionDefinition getFunction() {
        return func;
    }

    @Override
    public void printNode() {
        System.out.println("}  <--" + FunctionHelper.getFunctionName(func));
    }

    public IASTFunctionDefinition getFunc() {
        return func;
    }

    public void setFunc(IASTFunctionDefinition func) {
        this.func = func;
    }
}
