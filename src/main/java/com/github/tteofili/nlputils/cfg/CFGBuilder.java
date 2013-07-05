package com.github.tteofili.nlputils.cfg;

import java.util.Set;

/**
 * A builder for {@link ContextFreeGrammar}s
 */
public class CFGBuilder {

    private Set<String> nonTerminalSymbols;
    private Set<String> terminalSymbols;
    private Set<Rule> rules;
    private String startSymbol;

    public static CFGBuilder createCFG() {
        return new CFGBuilder();
    }

    public CFGBuilder withTerminals(Set<String> terminalSymbols) {
        this.terminalSymbols = terminalSymbols;
        return this;
    }

    public CFGBuilder withNonTerminals(Set<String> nonTerminalSymbols) {
        this.nonTerminalSymbols = nonTerminalSymbols;
        return this;
    }

    public CFGBuilder withRules(Set<Rule> rules) {
        this.rules = rules;
        return this;
    }

    public CFGBuilder withStartSymbol(String startSymbol) {
        this.startSymbol = startSymbol;
        return this;
    }

    public ContextFreeGrammar build() {
        return new ContextFreeGrammar(nonTerminalSymbols, terminalSymbols, rules, startSymbol);
    }
}
