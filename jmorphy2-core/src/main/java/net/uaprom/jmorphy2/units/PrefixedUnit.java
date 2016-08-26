package net.uaprom.jmorphy2.units;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.uaprom.jmorphy2.ParsedWord;
import net.uaprom.jmorphy2.Tag;


abstract class PrefixedUnit extends AnalyzerUnit {
    protected final AnalyzerUnit unit;
    
    public PrefixedUnit(Tag.Storage tagStorage, AnalyzerUnit unit, boolean terminate, float score) {
        super(tagStorage, terminate, score);
        this.unit = unit;
    }

    protected List<ParsedWord> parseWithPrefix(String word, String prefix) throws IOException {
        List<ParsedWord> parseds = new ArrayList<ParsedWord>();
        for (ParsedWord p : unit.parse(word.substring(prefix.length()))) {
            if (!p.tag.isProductive()) {
                continue;
            }
            parseds.add(new PrefixedParsedWord(prefix, p, score));
        }
        return parseds;
    }

    class PrefixedParsedWord extends AnalyzerParsedWord {
        private final String prefix;
        private final ParsedWord parsedWord;
            
        public PrefixedParsedWord(String prefix, ParsedWord p, float score) {
            super(prefix + p.word, p.tag, prefix + p.normalForm, p.foundWord, score);
            this.prefix = prefix;
            this.parsedWord = p;
        }

        @Override
        public ParsedWord rescore(float newScore) {
            return new PrefixedParsedWord(prefix, parsedWord, newScore);
        }

        @Override
        public List<ParsedWord> getLexeme() {
            List<ParsedWord> lexeme = new ArrayList<>();
            for (ParsedWord p : parsedWord.getLexeme()) {
                lexeme.add(new PrefixedParsedWord(prefix, p, 1.0f));
            }
            return lexeme;
        }
    }
}