package org.uu.nl.communities;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.uu.nl.embedding.Optimizer;
import org.uu.nl.embedding.Embedding;
import org.uu.nl.util.config.Configuration;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class DBScanCommunities {

    public static void write(String filename, double[]x) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        for (double d : x) {
            writer.write(Double.toString(d));
            writer.newLine();
        }
        writer.flush();
        writer.close();
    }

    public static void writeClusters(String filename, List<Cluster<Embedding.EmbeddedEntity>> clusters) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        System.out.println("Writing to file " + clusters.size() + " clusters");
        int clusterID = 1;
        for (Cluster<Embedding.EmbeddedEntity> cluster : clusters) {
            writer.write("# Cluster " + clusterID +", size: " + cluster.getPoints().size());
            writer.newLine();
            for(Embedding.EmbeddedEntity entity : cluster.getPoints()) {
                writer.write(entity.getKey());
                writer.newLine();
            }
            writer.newLine();
            clusterID++;
        }
        writer.flush();
        writer.close();
    }

    public DBScanCommunities(Embedding embedding, int minPts, int k) {
        double epsilon = findEpsilon(embedding, 1, k);

        List<Cluster<Embedding.EmbeddedEntity>> clusters = findClusters(embedding, epsilon, minPts);
        try {
            writeClusters("clusters.csv", clusters);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<Cluster<Embedding.EmbeddedEntity>> findClusters(Embedding embedding, double epsilon, int minPts) {

        //System.out.println("Found " + clusters.size() + " clusters");

        //for(Cluster<Embedding.EmbeddedEntity> cluster : clusters) {
        //    List<Embedding.EmbeddedEntity> points = cluster.getPoints();
        //    System.out.println(points.size());
        //}
        return new DBSCANClusterer<Embedding.EmbeddedEntity>(epsilon, minPts)
                .cluster(StreamSupport.stream(Spliterators.spliteratorUnknownSize(embedding.iterator(), Spliterator.ORDERED), true)
                .collect(Collectors.toList()));
    }

    private double findEpsilon(Embedding embedding, double sampleFrac, int k) {
        final DistanceMeasure distanceMeasure = new EuclideanDistance();

        // Draw a random sample of points to compare
        final int n = embedding.getNumberOfPoints();
        final int sampleSize = (int) (n * sampleFrac);
        final double[] nearestNeighborDistances = new double[sampleSize * k];
        for(int i = 0; i < sampleSize; i++) {
            int sample = Configuration.getThreadLocalRandom().nextInt(n);

            double currentMaxDist = 0;
            int currentMaxIndex = 0;
            int neighborsFound = 0;

            for(int j = 0; j < n; j++) {

                if(j == sample) continue;

                double dist = distanceMeasure.compute(embedding.getVectors()[sample], embedding.getVectors()[j]);

                if(neighborsFound < k) {
                    //System.out.println(i + " " + neighborsFound + " " + (i * k + neighborsFound));
                    nearestNeighborDistances[i * k + neighborsFound] = dist;
                    if(dist > currentMaxDist) {
                        currentMaxDist = dist;
                        currentMaxIndex = neighborsFound;
                    }
                    neighborsFound++;
                } else if (dist < currentMaxDist) {

                    nearestNeighborDistances[i * k + currentMaxIndex] = dist;

                    for(int a = 0; a < k; a++) {
                        if(nearestNeighborDistances[i * k + a] > currentMaxDist) {
                            currentMaxDist = nearestNeighborDistances[i * k + a];
                            currentMaxIndex = a;
                        }
                    }
                }
            }
        }

        Arrays.sort(nearestNeighborDistances);
        double[] deltas = delta2(nearestNeighborDistances,100);
        double maxDelta2 = 0;
        int elbow = 0;
        for(int i = 1; i < deltas.length - (nearestNeighborDistances.length/2); i++) {
            double delta2 = deltas[i] - deltas[i-1];
            if(delta2 > maxDelta2) {
                maxDelta2 = delta2;
                elbow = i;
            }
        }
        //try {
        //    write("distances.csv", nearestNeighborDistances);
        //    write("deltas.csv", deltas);
       // } catch (IOException e) {
         //   e.printStackTrace();
        //}
        return nearestNeighborDistances[elbow];
    }

    private double[] delta2(double[] x, int smooth) {
        double[] delta = new double[x.length - smooth*2];
        for(int i = smooth; i < x.length-smooth; i++) {
           delta[i-smooth] = (x[i+smooth] + x[i-smooth] - 2*x[i]);
        }
        return delta;
    }

    private double[] diff(double[] x) {
        double[] diff = new double[x.length-1];
        for(int i = 1; i < x.length; i++) {
            diff[i-1] = x[i] - x[i-1];
        }
        return diff;
    }

}
