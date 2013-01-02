package com.github.tteofili.nlputils;

/**
 * Utility class for tests
 */
public class TestUtils {

    public static void fillTrainingSet(TrainingSet trainingSet, int n) {
        for (int i = 0; i < 100; i++) {
            double[] inputs = new double[n];
            for (int j = 0; j < n; j++) {
                inputs[j] = Math.random();
            }
            trainingSet.add(new TrainingExample(inputs, Math.random()));
        }
    }
}
