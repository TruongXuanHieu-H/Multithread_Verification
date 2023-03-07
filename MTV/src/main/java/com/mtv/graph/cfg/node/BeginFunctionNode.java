package com.mtv.graph.cfg.node;

import com.mtv.DebugHelper.DebugHelper;
import com.mtv.graph.ast.FunctionHelper;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;

public class BeginFunctionNode extends CFGNode {
    private IASTFunctionDefinition funcDefinition;

    public BeginFunctionNode(IASTFunctionDefinition function) {
        funcDefinition = function;
    }
    public void setFunction(IASTFunctionDefinition function) {
        funcDefinition = function;
    }
    public String getName() {
        return FunctionHelper.getFunctionName(funcDefinition);
    }

    @Override
    public void printNode() {
        System.out.println(getName() + " {");
    }

    public IASTFunctionDefinition getFuncDefinition() {
        return funcDefinition;
    }

    public void setFuncDefinition(IASTFunctionDefinition funcDefinition) {
        this.funcDefinition = funcDefinition;
    }
}
