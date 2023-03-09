package com.mtv.encode.cfg.node;

import com.mtv.encode.cfg.index.VariableManager;

public abstract class CFGNode {
    protected CFGNode next;

    public CFGNode() {

    }

    public CFGNode(CFGNode next) {
        this.next = next;
    }

    public CFGNode getNext() {
        return next;
    }

    public void setNext(CFGNode next) {
        this.next = next;
    }

    public abstract void printNode();

    public String toString() {
        return "";
    }

    public void index(VariableManager vm) {
    }

    public String getFormula() {
        return null;
    }

    public String getInfixFormula() {
        return null;
    }

}
