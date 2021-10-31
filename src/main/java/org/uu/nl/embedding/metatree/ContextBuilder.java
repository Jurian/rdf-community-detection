package org.uu.nl.embedding.metatree;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class ContextBuilder {

    final ExecutorService es = Executors.newWorkStealingPool(3);
    final CompletionService<ContextVector> completionService = new ExecutorCompletionService<>(es);

    public ContextMatrix build(Dataset dataset, List<MetaTree> metaTreeList, String focusType) {

        try {
            NodeIndex nodeIndex = new NodeIndex();

            ResIterator it = dataset.getUnionModel().listSubjects();

            while(it.hasNext()) {
                Resource res = it.nextResource();
                Node node = res.asNode();
                nodeIndex.putIfAbsent(node, nodeIndex.size());
                if(res.hasProperty(RDF.type, focusType)) {
                    nodeIndex.addFocusNode(node);
                }
            }

            it.close();

            final int totalJobs = metaTreeList.size() * nodeIndex.getFocusNodes().size();
            for(MetaTree metaTree : metaTreeList) {
                for(Node root : nodeIndex.getFocusNodes()) {
                    completionService.submit(new MetaTreeJob(root, dataset, metaTree, nodeIndex ));
                }
            }

            Map<Integer, ContextVector> processedVectors = new HashMap<>();
            int jobsDone = 0;
            int nContextNodes = 0;
            while (jobsDone < totalJobs) {
                try {

                    final ContextVector context = completionService.take().get();
                    final ContextVector precomputedContext = processedVectors.get(context.getRoot());

                    if(precomputedContext == null) {
                        nContextNodes += context.size();
                        processedVectors.put(context.getRoot(), context);
                    } else {
                        int sizeBefore = precomputedContext.size();
                        processedVectors.put(context.getRoot(), precomputedContext.merge(context));
                        int sizeAfter = precomputedContext.size();
                        nContextNodes += (sizeAfter - sizeBefore);
                    }

                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    jobsDone++;
                }
            }

            ContextMatrix matrix = new ContextMatrix(nContextNodes);
            matrix.fill(processedVectors);
            return matrix;

        } finally {
            dataset.close();
        }
    }
}
