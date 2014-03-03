package org.apache.opennlp.utils;

/**
 * A {@link TrainingExample} holds some inputs and a corresponding output
 */
public class TrainingExample {
  private final double[] inputs;
  private final double output;

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
