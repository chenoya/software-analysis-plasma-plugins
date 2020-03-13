package be.uclouvain.gdbmiapi;

import org.apache.commons.text.StringEscapeUtils;

public class Utils {
    public static String extractValue(String s) {
        if (s.length() < 2)
            throw new IllegalArgumentException("At least size 2");
        return StringEscapeUtils.unescapeJava(s.substring(1, s.length() - 1));
    }

    public static void checkDone(String res) throws GdbException {
        MIOutputParser.OutputContext output = ParseMI.parse(res);
        assertOrThrow(res, output.result_record() != null);
        assertOrThrow(res, output.result_record().result_class().DONE() != null ||
                output.result_record().result_class().RUNNING() != null);
        assertOrThrow(res, output.result_record().result().size() == 0);
    }

    public static void assertOrThrow(String text, boolean pred) throws GdbException {
        if (!pred) {
            throw new GdbException(text);
        }
    }
}
