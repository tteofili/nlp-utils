package com.github.tteofili.nlputils;

import java.util.Collection;

/**
 * utility class for calculating probabilities of bi/uni-grams
 */
public class NGramUtils {

  private static Double count(String sequentWord, String precedingWord, Collection<String[]> set) {
    Double result = 0d;
    boolean foundPreceding = false;
    for (String[] sentence : set) {
      for (String w : sentence) {
        if (precedingWord.equals(w)) {
          foundPreceding = true;
          continue;
        }
        if (foundPreceding && sequentWord.equals(w)) {
          foundPreceding = false;
          result++;
        }
        else
          foundPreceding = false;
      }
    }
    return result;
  }

  private static Double count(String word, Collection<String[]> set) {
    Double result = 0d;
    for (String[] sentence : set) {
      for (String w : sentence) {
        if (word.equals(w))
          result++;
      }
    }
    return result;
  }

  public static Double calculateLaplaceSmoothingProbability(String sequentWord, String precedingWord, Collection<String[]> set, Double k) {
    return (count(sequentWord, precedingWord, set) + k) / (count(precedingWord, set) + k * set.size());
  }

  public static Double calculateBigramProbability(String sequentWord, String precedingWord, Collection<String[]> set) {
    return count(sequentWord, precedingWord, set)/ count(precedingWord, set);
  }

  public static Double calculateBigramPriorSmoothingProbability(String sequentWord, String precedingWord, Collection<String[]> set, Double k) {
    return (count(sequentWord, precedingWord, set) + k * calculateProbability(sequentWord, set)) / (count(precedingWord, set) + k * set.size());
  }

  private static Double calculateProbability(String word, Collection<String[]> set) {
    return count(word, set) / set.size();
  }


}
