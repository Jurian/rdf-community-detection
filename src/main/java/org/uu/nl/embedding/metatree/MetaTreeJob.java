package org.uu.nl.embedding.metatree;

import org.apache.jena.graph.Node;
import org.apache.jena.query.*;

import java.util.concurrent.Callable;

public class MetaTreeJob implements Callable<ContextVector> {

    private final Node root;
    private final Dataset dataset;
    private final MetaTree metaTree;
    private final NodeIndex nodeIndexes;

    public MetaTreeJob(Node root, Dataset dataset, MetaTree metaTree, NodeIndex nodeIndexes) {
        this.root = root;
        this.dataset = dataset;
        this.metaTree = metaTree;
        this.nodeIndexes = nodeIndexes;
    }

    @Override
    public ContextVector call() throws Exception {
        ContextVector vector = new ContextVector(nodeIndexes.get(root));
        dataset.begin(ReadWrite.READ);
        int numberOfTrees = 0;
        try {

            try(QueryExecution qExec = QueryExecutionFactory.create(metaTree.getQuery(root), dataset)) {
                ResultSet trees = qExec.execSelect();

                while(trees.hasNext()) {

                    QuerySolution tree = trees.nextSolution();
                    numberOfTrees++;
                    for(String key : trees.getResultVars()) {
                        Node node = tree.getResource(key).asNode();
                        vector.addNode(nodeIndexes.get(node));
                    }
                }
            }

        } finally {
            dataset.end();
        }

        return vector.normalize(numberOfTrees);
    }
}
