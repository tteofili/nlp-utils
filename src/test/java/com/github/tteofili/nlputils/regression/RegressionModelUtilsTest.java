package com.github.tteofili.nlputils.regression;

import org.junit.Test;

import com.github.tteofili.nlputils.TrainingExample;
import com.github.tteofili.nlputils.TrainingSet;
import com.github.tteofili.nlputils.regression.LinearCombinationHypothesis;
import com.github.tteofili.nlputils.regression.RegressionModelUtils;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Testcase for {@link com.github.tteofili.nlputils.regression.RegressionModelUtils}
 */
public class RegressionModelUtilsTest {

  @Test
  public void testLMS() throws Exception {
    TrainingSet trainingSet = new TrainingSet();
    trainingSet.add(new TrainingExample(new double[]{10, 10}, 1));
    LinearCombinationHypothesis hypothesis = new LinearCombinationHypothesis();
    hypothesis.updateParameters(new double[]{1, 1});
    double[] updatedParameters = RegressionModelUtils.batchLeastMeanSquareUpdate(new double[]{1, 1}, 0.1, trainingSet, hypothesis);
    assertNotNull(updatedParameters);
    assertTrue(updatedParameters.length == 2);
    assertTrue(updatedParameters[0] == -18d);
    assertTrue(updatedParameters[1] == -18d);
  }
}
