package org.uu.nl.communities;

import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.uu.nl.embedding.Embedding;
import org.uu.nl.util.config.Configuration;
import smile.clustering.DBSCAN;
import smile.manifold.TSNE;
import smile.plot.swing.ScatterPlot;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class SpectralCommunities {


    public void test(Embedding embedding) {


        try {
            double[][] data = embedding.getVectors();
            TSNE tsne = new TSNE(data, 3, 100, 200, 400);
            DBSCAN<double[]> clusters = DBSCAN.fit(tsne.coordinates, 3, findEpsilon(embedding, 1, 5));
            ScatterPlot.of(tsne.coordinates, clusters.y, '*').canvas().window();
        } catch (InterruptedException | InvocationTargetException e) {
            e.printStackTrace();
        }

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

        return nearestNeighborDistances[elbow];
    }

    private double[] delta2(double[] x, int smooth) {
        double[] delta = new double[x.length - smooth*2];
        for(int i = smooth; i < x.length-smooth; i++) {
            delta[i-smooth] = (x[i+smooth] + x[i-smooth] - 2*x[i]);
        }
        return delta;
    }
}
