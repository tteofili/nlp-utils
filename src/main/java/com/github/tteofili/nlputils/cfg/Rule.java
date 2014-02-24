package com.github.tteofili.nlputils.cfg;

/**
 * A rule for context free grammars
 */
public class Rule implements Comparable<Rule> {
  private final String entry;
  private final String[] expansion;

  public Rule(String entry, String... expansion) {
    this.entry = entry;
    this.expansion = expansion;
  }

  public String getEntry() {
    return entry;
  }

  public String[] getExpansion() {
    return expansion;
  }

  @Override
  public int compareTo(Rule o) {
    return entry.compareTo(o.getEntry());
  }
}
