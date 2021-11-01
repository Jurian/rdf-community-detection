package org.uu.nl.nodecontext;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;

import java.util.HashMap;
import java.util.Map;

public class NodeIndex implements KeyIndex {

    private final String focusType;
    public final int nFocusNodes, nContextNodes;
    public final String[] focusKeys;
    private final Map<Node, Integer> focusNodes = new HashMap<>();
    private final Map<Node, Integer> nodeIndexes = new HashMap<>();

    public NodeIndex(String focusType, Dataset dataset) {
        this.focusType = focusType;

        try {
            dataset.begin(ReadWrite.READ);

            StmtIterator resources = dataset.getUnionModel().listStatements();
            try {
                while(resources.hasNext()) {
                    final Resource res = resources.nextStatement().getSubject();

                    if(isFocusNode(res)) {
                        focusNodes.putIfAbsent(res.asNode(), focusNodes.size());
                    } else {
                        nodeIndexes.putIfAbsent(res.asNode(), nodeIndexes.size());
                    }

                    StmtIterator properties = res.listProperties();

                    try {
                        while(properties.hasNext()){
                            Statement property = properties.nextStatement();
                            // Ignore type information!
                            if(property.getPredicate() != RDF.type)
                                nodeIndexes.putIfAbsent(property.getObject().asNode(), nodeIndexes.size());
                        }
                    } finally {
                        properties.close();
                    }
                }
            } finally {
                resources.close();
            }
        } finally {
            dataset.end();
        }



        nFocusNodes = focusNodes.size();
        focusKeys = new String[nFocusNodes];

        for(Map.Entry<Node, Integer> entry : focusNodes.entrySet()) {
            focusKeys[entry.getValue()] = entry.getKey().getURI();
        }

        // Shift the index all non-focus nodes
        for(Map.Entry<Node, Integer> entry : nodeIndexes.entrySet()) {
            entry.setValue(entry.getValue() + nFocusNodes);
        }
        nodeIndexes.putAll(focusNodes);
        nContextNodes = nodeIndexes.size();
    }

    public void clear() {
        this.focusNodes.clear();
        this.nodeIndexes.clear();
    }

    public int getNodeID(Node node) {
        return nodeIndexes.get(node);
    }
    public Map<Node, Integer> getFocusNodes() {
        return focusNodes;
    }
    private boolean isFocusNode(Resource res) {
        return res.hasProperty(RDF.type, focusType);
    }

    @Override
    public String getKey(int index) {
        return focusKeys[index];
    }
}
