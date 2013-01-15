package com.github.tteofili.nlputils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

/**
 * An {@link Iterable} over {@link TrainingExample}s
 */
public class TrainingSet implements Iterable<TrainingExample> {

  private Collection<TrainingExample> trainingExamples = new HashSet<TrainingExample>();

  @Override
  public Iterator<TrainingExample> iterator() {
    return trainingExamples.iterator();
  }

  public void add(TrainingExample trainingExample) {
    trainingExamples.add(trainingExample);
  }

  public int size() {
    return trainingExamples.size();
  }
}
