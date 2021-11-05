package org.uu.nl.nodecontext;

import org.apache.jena.graph.Node;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Resource;
import org.uu.nl.util.parallel.DatasetThread;

import java.util.concurrent.Callable;

public class MetaTreeJob implements Callable<ContextVector> {

    private final Node root;
    private final MetaTree metaTree;
    private final NodeIndex nodeIndexes;

    public MetaTreeJob(Node root, MetaTree metaTree, NodeIndex nodeIndexes) {
        this.root = root;
        this.metaTree = metaTree;
        this.nodeIndexes = nodeIndexes;
    }

    @Override
    public ContextVector call() {
        ContextVector vector = new ContextVector(nodeIndexes.getNodeID(root));
        DatasetThread thread = (DatasetThread) Thread.currentThread();
        Dataset dataset = thread.getDataset();
        dataset.begin(ReadWrite.READ);

        int numberOfTrees = 0;
        try {
            try(QueryExecution qExec = QueryExecutionFactory.create(metaTree.getQuery(root), dataset)) {
                ResultSet trees = qExec.execSelect();

                while(trees.hasNext()) {

                    QuerySolution tree = trees.nextSolution();
                    numberOfTrees++;
                    for(String key : trees.getResultVars()) {
                        Resource element = tree.getResource(key);
                        if(element != null) {
                            Node node = element.asNode();
                            vector.addNode(nodeIndexes.getNodeID(node));
                        }

                    }
                }
            }

        } finally {
            dataset.end();
        }

        return vector.normalize(numberOfTrees);
    }
}
