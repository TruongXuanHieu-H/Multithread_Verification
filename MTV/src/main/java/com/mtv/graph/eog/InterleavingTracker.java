package com.mtv.graph.eog;

import java.util.ArrayList;

public class InterleavingTracker {
    public enum InterleavingMarker {
        // Skip describes straight action
        // Eg: Event A -> Event B
        Skip,
        // Begin describes branching action
        // Eg: Event A <- Event B -> Event C
        Begin,
        // End describes merging action
        // Eg: Event A -> Event B <- Event C
        End
    }

    public InterleavingTracker() {
        marker = InterleavingMarker.Skip;
        relatedNodes = null;
    }

    private InterleavingMarker marker;
    public void SetMarker(InterleavingMarker marker) {
        this.marker = marker;
        if (marker == InterleavingMarker.Skip) {
            relatedNodes = null;
        } else {
            relatedNodes = new ArrayList<>();
        }
    }
    public InterleavingMarker GetMarker() {
        return marker;
    }

    public ArrayList<EventOrderNode> relatedNodes;
    public boolean AddRelatedNode(EventOrderNode node) {
        if (relatedNodes == null) {
            return false;
        } else {
            if (!relatedNodes.contains(node)) {
                relatedNodes.add(node);
            }
            return true;
        }
    }
    public ArrayList<EventOrderNode> getRelatedNodes() {
        return relatedNodes;
    }

    public void PrintTracker() {
        if (GetMarker() == InterleavingTracker.InterleavingMarker.Begin) {
            for (EventOrderNode node: getRelatedNodes()) {
                for (int i = 0; i < 4; i++) {
                    System.out.print(" ");
                }
                System.out.print("Track to:");
                node.printNode(4);
            }
        } else if (GetMarker() == InterleavingTracker.InterleavingMarker.End) {
            for (int i = 0; i < 4; i++) {
                System.out.print(" ");
            }
            System.out.print("Track from:");
            for (EventOrderNode node: getRelatedNodes()) {
                node.printNode(4);
            }
        } else {

        }
    }
}
