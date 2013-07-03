package com.github.tteofili.nlputils;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Testcase for {@link ContextFreeGrammar}
 */
public class ContextFreeGrammarTest {

    private ContextFreeGrammar contextFreeGrammar;

    @Before
    public void setUp() throws Exception {

        Set<String> nonTerminals = new HashSet<String>(); // PoS + Parse tags
        nonTerminals.add("S");
        nonTerminals.add("NP");
        nonTerminals.add("VP");
        nonTerminals.add("PP");
        nonTerminals.add("DT");
        nonTerminals.add("Vi");
        nonTerminals.add("Vt");
        nonTerminals.add("NN");
        nonTerminals.add("IN");

        String startSymbol = "S";

        Set<String> terminals = new HashSet<String>();
        terminals.add("sleeps");
        terminals.add("saw");
        terminals.add("man");
        terminals.add("woman");
        terminals.add("telescope");
        terminals.add("the");
        terminals.add("with");
        terminals.add("in");

        Set<Rule> rules = new TreeSet<Rule>();
        rules.add(new Rule("S", "NP", "VP"));
        rules.add(new Rule("VP", "Vi"));
        rules.add(new Rule("VP", "Vt", "NP"));
        rules.add(new Rule("VP", "VP", "PP"));
        rules.add(new Rule("NP", "DT", "NN"));
        rules.add(new Rule("NP", "NP", "PP"));
        rules.add(new Rule("PP", "IN", "NP"));
        rules.add(new Rule("Vi", "sleeps"));
        rules.add(new Rule("Vt", "saw"));
        rules.add(new Rule("NN", "man"));
        rules.add(new Rule("NN", "woman"));
        rules.add(new Rule("NN", "telescope"));
        rules.add(new Rule("DT", "the"));
        rules.add(new Rule("IN", "with"));
        rules.add(new Rule("IN", "in"));

        contextFreeGrammar = new ContextFreeGrammar(nonTerminals, terminals, rules, startSymbol);
    }

    @Test
    public void testExpansion() throws Exception {
        String[] expansion = contextFreeGrammar.leftMostDerivation("S");
        assertNotNull(expansion);
        assertTrue(expansion.length > 0);
    }
}
