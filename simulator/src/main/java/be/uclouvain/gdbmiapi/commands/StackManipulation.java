package be.uclouvain.gdbmiapi.commands;

import be.uclouvain.gdbmiapi.GdbException;
import be.uclouvain.gdbmiapi.GdbProcess;
import be.uclouvain.gdbmiapi.MIOutputParser;
import be.uclouvain.gdbmiapi.ParseMI;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static be.uclouvain.gdbmiapi.Utils.assertOrThrow;
import static be.uclouvain.gdbmiapi.Utils.extractValue;

public class StackManipulation {
    // return only the first frame
    public static Map<String, Object> backtrace(GdbProcess gdbProcess) throws IOException, GdbException {
        String res = gdbProcess.executeGDBCommand("-stack-list-frames");
        MIOutputParser.OutputContext output = ParseMI.parse(res);

        assertOrThrow(res, output.result_record() != null);
        assertOrThrow(res, output.result_record().result_class().DONE() != null);
        assertOrThrow(res, output.result_record().result().size() == 1);
        assertOrThrow(res, output.result_record().result(0).variable().getText().equals("stack"));
        assertOrThrow(res, output.result_record().result(0).value().list() != null);
        //assertOrThrow(res, output.result_record().result(0).value().list().result().size() == 1);
        assertOrThrow(res, output.result_record().result(0).value().list().result(0).variable().getText().equals("frame"));
        assertOrThrow(res, output.result_record().result(0).value().list().result(0).value().tuple() != null);
        assertOrThrow(res, output.result_record().result(0).value().list().result(0).value().tuple().result().size() == 6);

        assertOrThrow(res, output.result_record().result(0).value().list().result(0).value().tuple().result(0).variable().getText().equals("level"));
        assertOrThrow(res, output.result_record().result(0).value().list().result(0).value().tuple().result(1).variable().getText().equals("addr"));
        assertOrThrow(res, output.result_record().result(0).value().list().result(0).value().tuple().result(2).variable().getText().equals("func"));
        assertOrThrow(res, output.result_record().result(0).value().list().result(0).value().tuple().result(3).variable().getText().equals("file"));
        assertOrThrow(res, output.result_record().result(0).value().list().result(0).value().tuple().result(4).variable().getText().equals("fullname"));
        assertOrThrow(res, output.result_record().result(0).value().list().result(0).value().tuple().result(5).variable().getText().equals("line"));

        Map<String, Object> map = new HashMap<>(6);
        map.put("level", Long.parseLong(extractValue(output.result_record().result(0).value().list().result(0).value().tuple().result(0).value().getText())));
        map.put("addr", Long.parseUnsignedLong(extractValue(output.result_record().result(0).value().list().result(0).value().tuple().result(1).value().getText()).substring(2), 16));
        map.put("func", extractValue(output.result_record().result(0).value().list().result(0).value().tuple().result(2).value().getText()));
        map.put("file", extractValue(output.result_record().result(0).value().list().result(0).value().tuple().result(3).value().getText()));
        map.put("fullname", extractValue(output.result_record().result(0).value().list().result(0).value().tuple().result(4).value().getText()));
        map.put("line", Long.parseLong(extractValue(output.result_record().result(0).value().list().result(0).value().tuple().result(5).value().getText())));

        return map;
    }
}
