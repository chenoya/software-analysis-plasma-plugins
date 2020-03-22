package be.uclouvain.gdbmiapi.commands;

import be.uclouvain.gdbmiapi.*;
import org.apache.commons.text.StringEscapeUtils;

import java.io.IOException;

import static be.uclouvain.gdbmiapi.Utils.assertOrThrow;

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
}
