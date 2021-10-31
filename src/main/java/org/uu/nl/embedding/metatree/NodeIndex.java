package org.uu.nl.embedding.metatree;

import org.apache.jena.graph.Node;

import java.util.*;

public class NodeIndex extends HashMap<Node, Integer> {

    private final Set<Node> focusNodes = new HashSet<>();

    public Set<Node> getFocusNodes() {
        return this.focusNodes;
    }

    public void addFocusNode(Node node) {
        focusNodes.add(node);
    }

}
