package com.github.tteofili.nlputils;

import org.junit.Test;

/**
 * Testcase for {@link GradientDescentUtils}
 */
public class GradientDescentUtilsTest {

    @Test
    public void testConvergence() throws Exception {
        Hypothesis linearCombination = new Hypothesis() {
            private double[] weights;

            @Override
            public double calculateOutput(double[] inputs) {
                int output = 0;
                for (int i = 0; i < weights.length; i++) {
                    output += weights[i] * inputs[i];
                }
                return output;
            }

            @Override
            public void updateParameters(double[] parameters) {
                weights = parameters;
            }
        };
        TrainingSet trainingSet = new TrainingSet();
        fillTrainingSet(trainingSet, 5);
        GradientDescentUtils.batchGradientDescent(linearCombination, trainingSet, 0.3);
    }

    private void fillTrainingSet(TrainingSet trainingSet, int n) {
        for (int i = 0; i < 100; i++) {
            double[] inputs = new double[n];
            for (int j = 0; j < n; j++) {
                inputs[j] = Math.random();
            }
            trainingSet.add(new TrainingExample(inputs, Math.random()));
        }
    }
}
