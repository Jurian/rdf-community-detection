package org.uu.nl.communities;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.uu.nl.embedding.Optimizer;
import org.uu.nl.embedding.Embedding;

import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class DBScanCommunities {

    private final DBSCANClusterer<Embedding.EmbeddedEntity> clusterer;
    List<Cluster<Embedding.EmbeddedEntity>> clusters;

    public DBScanCommunities(Embedding embedding, double eps, int minPts) {
        this.clusterer = new DBSCANClusterer<>(eps,minPts);

        clusters = clusterer.cluster(
                StreamSupport.stream(Spliterators.spliteratorUnknownSize(embedding.iterator(), Spliterator.ORDERED), true)
                .collect(Collectors.toList()));

        System.out.println("Found " + clusters.size() + " clusters");

        for(Cluster<Embedding.EmbeddedEntity> cluster : clusters) {
            List<Embedding.EmbeddedEntity> points = cluster.getPoints();
            System.out.println(points.size());
        }
    }

}
