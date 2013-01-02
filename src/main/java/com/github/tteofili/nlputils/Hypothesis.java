package com.github.tteofili.nlputils;

/**
 * An {@link Hypothesis} maps a series of inputs in an output
 */
public interface Hypothesis {

    /**
     * calculate the output given some inputs according to the underlying model.
     *
     * @param inputs an array of inputs as <code>double</code>
     * @return a <code>double</code> representing the output
     */
    public double calculateOutput(double[] inputs);

    /**
     * update the internal model's parameters.
     *
     * @param parameters an array of <code>double</code> containing the updated parameters
     */
    public void updateParameters(double[] parameters);
}
