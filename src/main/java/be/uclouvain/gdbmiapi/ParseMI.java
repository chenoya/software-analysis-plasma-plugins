package be.uclouvain.gdbmiapi;

import org.antlr.v4.runtime.*;

public class ParseMI {
    public static MIOutputParser.OutputContext parse(String text) throws GdbException {
        TokenSource lexer = new MIOutputLexer(CharStreams.fromString(text));

        TokenStream tokens = new CommonTokenStream(lexer);
        MIOutputParser parser = new MIOutputParser(tokens);
        parser.setErrorHandler(new DefaultErrorStrategy() {
            @Override
            public void recover(Parser recognizer, RecognitionException e) {
                throw new IllegalArgumentException("GDB output parsing error (antlr)");
            }

            @Override
            public Token recoverInline(Parser recognizer) throws RecognitionException {
                throw new IllegalArgumentException("GDB output parsing error (antlr)");
            }
        });
        try {
            return parser.output();
        } catch (IllegalArgumentException e) {
            throw new GdbException(e.getMessage());
        }

    }
}
