package com.github.tteofili.nlputils;

import java.util.Arrays;

/**
 * Utility class for calculating gradient descent
 */
public class GradientDescentUtils {

    private static final double THRESHOLD = 0.5;
    private static final int MAX_ITERATIONS = 100000;

    public static void batchGradientDescent(Hypothesis hypothesis, TrainingSet trainingSet, double alpha) {
        // set initial random weights
        double[] parameters = initializeRandomWeights(trainingSet.iterator().next().getInputs().length);
        hypothesis.updateParameters(parameters);

        int iterations = 0;

        while (true) {
            // calcualte cost
            double cost = RegressionModelUtils.ordinaryLeastSquares(trainingSet, hypothesis);

            if (cost < THRESHOLD || iterations > MAX_ITERATIONS) {
                System.err.println(cost + " with parameters " + Arrays.toString(parameters));
                break;
            }

            // calculate the updated parameters
            parameters = RegressionModelUtils.leastMeanSquareUpdate(parameters, alpha, trainingSet, hypothesis);

            // update weights in the hypothesis
            hypothesis.updateParameters(parameters);

            iterations++;
        }
    }

    private static double[] initializeRandomWeights(int size) {
        double[] doubles = new double[size];
        for (int i = 0; i < doubles.length; i++) {
            doubles[i] = Math.random() * 0.1d;
        }
        return doubles;
    }

}
