package net.uaprom.jmorphy2.solr;

import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import static org.apache.lucene.analysis.BaseTokenStreamTestCase.assertAnalyzesTo;

import net.uaprom.jmorphy2.nlp.Ruleset;
import net.uaprom.jmorphy2.nlp.Tagger;
import net.uaprom.jmorphy2.nlp.SimpleTagger;
import net.uaprom.jmorphy2.nlp.Parser;
import net.uaprom.jmorphy2.nlp.SimpleParser;
import net.uaprom.jmorphy2.nlp.SubjectExtractor;


@RunWith(JUnit4.class)
public class Jmorphy2SubjectFilterTest extends BaseFilterTestCase {
    private static final String TAGGER_RULES_RESOURCE = "/tagger_rules.txt";
    private static final String PARSER_RULES_RESOURCE = "/parser_rules.txt";

    private SubjectExtractor subjExtractor;

    @Before
    public void setUp() throws IOException {
        initMorphAnalyzer();

        Tagger tagger =
            new SimpleTagger(morph,
                             new Ruleset(getClass().getResourceAsStream(TAGGER_RULES_RESOURCE)));
        Parser parser =
            new SimpleParser(morph,
                             tagger,
                             new Ruleset(getClass().getResourceAsStream(PARSER_RULES_RESOURCE)));
        subjExtractor =
            new SubjectExtractor(parser,
                                 "+NP,nomn +NP,accs -PP NOUN,nomn NOUN,accs LATN NUMB",
                                 true);
    }

    protected Analyzer getAnalyzer() {
        return new Analyzer() {
            @Override
            protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
                Tokenizer source = new WhitespaceTokenizer(LUCENE_VERSION, reader);
                TokenFilter filter = new Jmorphy2SubjectFilter(source, subjExtractor);
                return new TokenStreamComponents(source, filter);
            }
        };
    }

    @Test
    public void test() throws IOException {
        Analyzer analyzer = getAnalyzer();

        assertAnalyzesTo(analyzer,
                         "",
                         new String[0],
                         new int[0]);
        assertAnalyzesTo(analyzer,
                         "iphone",
                         new String[]{"iphone"},
                         new int[]{1});
        assertAnalyzesTo(analyzer,
                         "теплые перчатки",
                         new String[]{"перчатка"},
                         new int[]{2});
        assertAnalyzesTo(analyzer,
                         "магнит на холодильник",
                         new String[]{"магнит"},
                         new int[]{1});
        assertAnalyzesTo(analyzer,
                         "чехол кожаный 5 for iphone 4",
                         new String[]{"чехол", "5"},
                         new int[]{1, 2});
    }
}
