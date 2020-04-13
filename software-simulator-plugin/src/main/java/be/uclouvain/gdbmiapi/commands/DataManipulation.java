package be.uclouvain.gdbmiapi.commands;

import be.uclouvain.gdbmiapi.*;
import org.apache.commons.text.StringEscapeUtils;

import java.io.IOException;

import static be.uclouvain.gdbmiapi.Utils.assertOrThrow;
import static be.uclouvain.gdbmiapi.Utils.extractValue;

public class DataManipulation {
    public static String data_eval_expr(GdbProcess gdbProcess, String expr) throws IOException, GdbException {
        String res = gdbProcess.executeGDBCommand("-data-evaluate-expression \"" + StringEscapeUtils.escapeJava(expr) + "\"");
        MIOutputParser.OutputContext output = ParseMI.parse(res);

        assertOrThrow(res, output.result_record() != null);
        assertOrThrow(res, output.result_record().result_class().DONE() != null);
        assertOrThrow(res, output.result_record().result().size() == 1);
        assertOrThrow(res, output.result_record().result(0).variable().getText().equals("value"));
        return Utils.extractValue(output.result_record().result(0).value().getText());
    }

    public static byte[] data_read_memory(GdbProcess gdbProcess, long address, long count) throws IOException, GdbException {
        return data_read_memory(gdbProcess, Long.toUnsignedString(address), count);
    }

    public static byte[] data_read_memory(GdbProcess gdbProcess, String address, long count) throws IOException, GdbException {
        String res = gdbProcess.executeGDBCommand("-data-read-memory-bytes " + address + " " + Long.toUnsignedString(count));
        MIOutputParser.OutputContext output = ParseMI.parse(res);

        assertOrThrow(res, output.result_record() != null);
        assertOrThrow(res, output.result_record().result_class().DONE() != null);
        assertOrThrow(res, output.result_record().result().size() == 1);
        assertOrThrow(res, output.result_record().result(0).variable().getText().equals("memory"));
        assertOrThrow(res, output.result_record().result(0).value().list() != null);
        assertOrThrow(res, output.result_record().result(0).value().list().value() != null);
        assertOrThrow(res, output.result_record().result(0).value().list().value().size() == 1);
        assertOrThrow(res, output.result_record().result(0).value().list().value().get(0).tuple() != null);
        assertOrThrow(res, output.result_record().result(0).value().list().value().get(0).tuple().result().size() == 4);
        assertOrThrow(res, output.result_record().result(0).value().list().value().get(0).tuple().result(0).variable().getText().equals("begin"));
        assertOrThrow(res, output.result_record().result(0).value().list().value().get(0).tuple().result(1).variable().getText().equals("offset"));
        assertOrThrow(res, output.result_record().result(0).value().list().value().get(0).tuple().result(2).variable().getText().equals("end"));
        assertOrThrow(res, output.result_record().result(0).value().list().value().get(0).tuple().result(3).variable().getText().equals("contents"));

        long begin = Long.parseUnsignedLong(extractValue(output.result_record().result(0).value().list().value().get(0).tuple().result(0).value().getText()).substring(2), 16);
        long offset = Long.parseUnsignedLong(extractValue(output.result_record().result(0).value().list().value().get(0).tuple().result(1).value().getText()).substring(2), 16);
        assertOrThrow(res, offset == 0L);
        long end = Long.parseUnsignedLong(extractValue(output.result_record().result(0).value().list().value().get(0).tuple().result(2).value().getText()).substring(2), 16);
        assertOrThrow(res, end - begin == count);
        String contents = extractValue(output.result_record().result(0).value().list().value().get(0).tuple().result(3).value().getText());
        assertOrThrow(res,contents.length() % 2 == 0);
        assertOrThrow(res, contents.length() / 2 == count);

        byte[] c = new byte[(int) count];
        for (int i = 0; i < count; i++) {
            c[i] = (byte) ((Character.digit(contents.charAt(i*2), 16) * 16) + Character.digit(contents.charAt(i*2+1), 16));
        }
        return c;
    }
}
