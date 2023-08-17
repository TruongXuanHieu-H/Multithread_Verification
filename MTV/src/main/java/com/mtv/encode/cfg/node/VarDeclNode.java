package com.mtv.encode.cfg.node;

import com.mtv.encode.cfg.utils.ExpressionHelper;
import com.mtv.encode.cfg.utils.ExpressionModifier;
import com.mtv.debug.DebugHelper;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

public class VarDeclNode extends CFGNode{

    private IASTStatement statement;
    private IASTFunctionDefinition func;

    public VarDeclNode(IASTStatement statement) {
        this.statement = statement;
    }
    public VarDeclNode(IASTStatement statement, IASTFunctionDefinition func) {
        this.statement = changeName(statement, func);
        this.setFunc(func);
    }

    private IASTStatement changeName(IASTStatement statement, IASTFunctionDefinition func) {
        return (IASTStatement) ExpressionModifier.changeVariableName(statement, func);
    }
    public IASTFunctionDefinition getFunc() {
        return func;
    }

    public void setFunc(IASTFunctionDefinition func) {
        this.func = func;
    }
    @Override
    public void printNode() {
        if (statement != null) {
            DebugHelper.print("Variable Decl Node: " + ExpressionHelper.toString(statement));
        }
    }
}