package org.uu.nl.communities;

import com.apporiented.algorithm.clustering.AverageLinkageStrategy;
import com.apporiented.algorithm.clustering.Cluster;
import com.apporiented.algorithm.clustering.ClusteringAlgorithm;
import com.apporiented.algorithm.clustering.DefaultClusteringAlgorithm;
import com.apporiented.algorithm.clustering.visualization.DendrogramPanel;
import org.uu.nl.embedding.Embedding;

import javax.swing.*;

public class HierarchicalCommunities {

    public HierarchicalCommunities(Embedding embedding) {
        ClusteringAlgorithm alg = new DefaultClusteringAlgorithm();
        Cluster cluster = alg.performClustering(embedding.getVectors(), embedding.getKeyIndex().getKeys(),
                new AverageLinkageStrategy());

        JFrame frame = new JFrame();
        DendrogramPanel dp = new DendrogramPanel();
        dp.setModel(cluster);
        frame.add(dp);
        frame.setVisible(true);
    }
}
