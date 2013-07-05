package com.github.tteofili.nlputils.regression;

import org.junit.Test;

import com.github.tteofili.nlputils.TestUtils;
import com.github.tteofili.nlputils.TrainingSet;
import com.github.tteofili.nlputils.regression.GradientDescentUtils;
import com.github.tteofili.nlputils.regression.LinearCombinationHypothesis;

/**
 * Testcase for {@link com.github.tteofili.nlputils.regression.GradientDescentUtils}
 */
public class GradientDescentUtilsTest {

  @Test
  public void testConvergence() throws Exception {
    TrainingSet trainingSet = new TrainingSet();
    TestUtils.fillTrainingSet(trainingSet, 100, 5);
    GradientDescentUtils.batchGradientDescent(new LinearCombinationHypothesis(), trainingSet, 0.00002);
  }

}
