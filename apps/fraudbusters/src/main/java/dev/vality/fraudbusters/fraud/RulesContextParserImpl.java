package dev.vality.fraudbusters.fraud;

import dev.vality.fraudo.FraudoRulesLexer;
import dev.vality.fraudo.FraudoRulesParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.springframework.stereotype.Component;

@Component
public class RulesContextParserImpl implements FraudContextParser<FraudoRulesParser.ParseContext> {

    @Override
    public FraudoRulesParser.ParseContext parse(String template) {
        FraudoRulesLexer lexer = new FraudoRulesLexer(CharStreams.fromString(template));
        FraudoRulesParser parser = new FraudoRulesParser(new CommonTokenStream(lexer));
        return parser.parse();
    }

}
