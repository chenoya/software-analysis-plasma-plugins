package be.uclouvain.gdbmiapi.commands;

import be.uclouvain.gdbmiapi.*;
import org.apache.commons.text.StringEscapeUtils;

import java.io.IOException;

import static be.uclouvain.gdbmiapi.Utils.assertOrThrow;

public class Breakpoint {
    public static void break_(GdbProcess gdbProcess, String expr) throws IOException, GdbException {
        String res = gdbProcess.executeGDBCommand("-break-insert \"" + StringEscapeUtils.escapeJava(expr) + "\"");
        MIOutputParser.OutputContext output = ParseMI.parse(res);

        assertOrThrow(res, output.result_record() != null);
        assertOrThrow(res, output.result_record().result_class().DONE() != null);
        assertOrThrow(res, output.result_record().result().size() == 1);
    }
}
