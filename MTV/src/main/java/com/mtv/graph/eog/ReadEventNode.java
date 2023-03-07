package com.mtv.graph.eog;

import com.mtv.DebugHelper.DebugHelper;

import java.util.ArrayList;

public class ReadEventNode extends EventOrderNode{

    public ReadEventNode(String varPreference) {
        super(varPreference);
    }

    public ReadEventNode(String varPreference, ArrayList<EventOrderNode> previous, ArrayList<EventOrderNode> next) {
        super(varPreference, previous, next);
    }

    @Override
    public void printNode(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print(" ");
        }
        DebugHelper.print(interleavingTracker.GetMarker().toString() + " \t- Read: " + (suffixVarPref == null ? varPreference : suffixVarPref));
    }
}
