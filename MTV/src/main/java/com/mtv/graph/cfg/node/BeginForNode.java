package com.mtv.graph.cfg.node;

import java.io.Serializable;

@SuppressWarnings("serial")
public class BeginForNode extends BeginNode implements Serializable {
    public BeginForNode() {
    }

    @Override
    public void printNode() {
        System.out.println("BeginForNode ");
    }

}
