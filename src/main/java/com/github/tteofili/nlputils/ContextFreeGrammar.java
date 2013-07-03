package com.github.tteofili.nlputils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

/**
 * A context free grammar
 */
public class ContextFreeGrammar {
    private Set<String> nonTerminalSymbols;
    private Set<String> terminalSymbols;
    private Set<Rule> rules;
    private String startSymbol;

    public ContextFreeGrammar(Set<String> nonTerminalSymbols, Set<String> terminalSymbols, Set<Rule> rules, String startSymbol) {
        assert nonTerminalSymbols.contains(startSymbol) : "start symbol doesn't belong to non-terminal symbols set";

        this.nonTerminalSymbols = nonTerminalSymbols;
        this.terminalSymbols = terminalSymbols;
        this.rules = rules;
        this.startSymbol = startSymbol;
    }


    public String[] leftMostDerivation(String... words) {
        ArrayList<String> expansion = new ArrayList<String>(words.length);

        assert words.length > 0 && startSymbol.equals(words[0]);

        for (int i = 0; i < words.length; i++) {
            expansion.addAll(getTerminals(words[i]));
        }
        return expansion.toArray(new String[expansion.size()]);

    }

    private Collection<String> getTerminals(String word) {

        if (terminalSymbols.contains(word)) {
            Collection<String> c = new LinkedList<String>();
            c.add(word);
            return c;
        } else {
            assert nonTerminalSymbols.contains(word);
            String[] expansions = getExpansionForSymbol(word);
            Collection<String> c = new LinkedList<String>();
            for (String e : expansions) {
                c.addAll(getTerminals(e));
            }
            return c;
        }
    }

    private String[] getExpansionForSymbol(String currentSymbol) {
        Rule r = getRuleForWord(currentSymbol);
        return r.getExpansion();
    }

    private Rule getRuleForWord(String word) {
        for (Rule r : rules) {
            if (word.equals(r.getEntry())) {
                return r;
            }
        }
        return null;
    }

}
