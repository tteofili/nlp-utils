package com.github.tteofili.nlputils;

/**
 * A {@link TrainingExample} holds some inputs and a corresponding output
 */
public class TrainingExample {
    private double[] inputs;
    private double output;

    public TrainingExample(double[] inputs, double output) {
        this.inputs = inputs;
        this.output = output;
    }

    public double[] getInputs() {
        return inputs;
    }

    public double getOutput() {
        return output;
    }
}
