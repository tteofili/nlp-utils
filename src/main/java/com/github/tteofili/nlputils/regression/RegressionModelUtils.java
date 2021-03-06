package com.github.tteofili.nlputils.regression;

import com.github.tteofili.nlputils.TrainingExample;
import com.github.tteofili.nlputils.TrainingSet;

/**
 * Utility class for calculating various regression models costs
 */
public class RegressionModelUtils {

  /**
   * calculate the ordinary least squares (OLS) cost in the given training set for a given hypothesis
   *
   * @param trainingSet the training set used
   * @param hypothesis  the hypothesis function representing the model
   * @return the cost of the hypothesis for the given training set using OLS
   */
  public static double ordinaryLeastSquares(TrainingSet trainingSet, Hypothesis hypothesis) {
    double output = 0;
    for (TrainingExample trainingExample : trainingSet) {
      double difference = hypothesis.calculateOutput(trainingExample.getInputs()) - trainingExample.getOutput();
      output += Math.pow(difference, 2);
    }
    return output / 2d;
  }

  /**
   * calculate the least mean square (LMS) update for a given weight vector
   *
   * @param thetas      the array of weights
   * @param alpha       the learning rate alpha
   * @param trainingSet the training set to use for learning
   * @param hypothesis  the hypothesis representing the model
   * @return the updated weights vector
   */
  public static double[] batchLeastMeanSquareUpdate(double[] thetas, double alpha, TrainingSet trainingSet, Hypothesis hypothesis) {
    double[] updatedWeights = new double[thetas.length];
    for (int i = 0; i < updatedWeights.length; i++) {
      double errors = 0;
      for (TrainingExample trainingExample : trainingSet) {
        errors += (trainingExample.getOutput() - hypothesis.calculateOutput(trainingExample.getInputs())) * trainingExample.getInputs()[i];
      }
      updatedWeights[i] = thetas[i] + alpha * errors;
    }
    return updatedWeights;
  }

  /**
   * calculate least mean square update for a given training example for the j-th input
   *
   * @param thetas          the array of weights
   * @param alpha           the learning rate alpha
   * @param trainingExample the training example to use for learning
   * @param hypothesis      the hypothesis representing the model
   * @param j               the index of the j-th input
   * @return the updated weight for the j-th element of the weights vector
   */
  public static double singleLeastMeanSquareUpdate(double[] thetas, double alpha, TrainingExample trainingExample, Hypothesis hypothesis, int j) {
    return thetas[j] + alpha * (trainingExample.getOutput() - hypothesis.calculateOutput(trainingExample.getInputs())) * trainingExample.getInputs()[j];
  }

}
