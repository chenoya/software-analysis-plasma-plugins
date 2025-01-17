package be.uclouvain;

import be.uclouvain.gdbmiapi.GdbException;
import be.uclouvain.gdbmiapi.MIOutputParser;
import be.uclouvain.gdbmiapi.ParseMI;
import org.apache.commons.text.StringEscapeUtils;

import java.util.function.Function;

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
            throw new GdbException(text, true);
        }
    }

    /**
     * Convert the long value to a double one if no precision is lost, return NaN otherwise.
     * @param l the long value to convert
     * @return a double of the same value or Double.NaN
     */
    public static double longToDoubleOrNaN(long l) {
        double res = (double) l;
        long res2 = (long) res;
        return (l == res2) ? res : Double.NaN;
    }

    public static <T> T getFromTOML(Function<String, T> f, String name) {
        try {
            return f.apply(name);
        } catch (ClassCastException e) {
            return null;
        }
    }

}
