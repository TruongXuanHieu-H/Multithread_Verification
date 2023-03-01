package com.mtv.graph.eog;

import com.mtv.DebugHelper.DebugHelper;

/*
    EventOrderNode is the present of a read/write event in EventOrderGraph
    Each node has two pointers point in its previous node and next node


 */
public class EventOrderNode {
    public EventOrderNode previousNode;
    public EventOrderNode nextNode;


    public String varPreference;
    public EventOrderAction eventOrderAction;

    public EventOrderNode(String varPreference, EventOrderAction action) {
        this.varPreference = varPreference;
        this.eventOrderAction = action;
        this.previousNode = null;
        this.nextNode = null;
    }

    public EventOrderNode(String varPreference, EventOrderAction action, EventOrderNode previous, EventOrderNode next) {
        this.varPreference = varPreference;
        this.eventOrderAction = action;
        this.previousNode = previous;
        this.nextNode = next;
    }
    public void printNode(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print(" ");
        }
        if (eventOrderAction == EventOrderAction.Write) {
            DebugHelper.print("Write: " + varPreference);
        } else if (eventOrderAction == EventOrderAction.Read) {
            DebugHelper.print("Read: " + varPreference);
        } else {
            // Undefined action
            DebugHelper.print("Undefined EOAction");
        }
    }


}
