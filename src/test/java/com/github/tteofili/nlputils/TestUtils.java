package com.github.tteofili.nlputils;

import java.util.Arrays;

/**
 * Utility class for tests
 */
public class TestUtils {

    public static void fillTrainingSet(TrainingSet trainingSet, int size, int dimension) {
        for (int i = 0; i < size; i++) {
            double[] inputs = new double[dimension];
            for (int j = 0; j < dimension; j++) {
                inputs[j] = Math.random();
            }
            double out = Math.random();
            trainingSet.add(new TrainingExample(inputs, out));
        }
    }
}
