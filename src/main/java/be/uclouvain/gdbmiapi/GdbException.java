package be.uclouvain.gdbmiapi;

import be.uclouvain.Utils;

import java.io.IOException;

public class GdbException extends IOException {
    public GdbException(String message) {
        super(message);
    }

    public GdbException(String message, boolean parse) {
        super(parse ? parseMessage(message): message);
    }

    private static String parseMessage(String msg) {
        try {
            MIOutputParser.OutputContext output = ParseMI.parse(msg);
            if (output.result_record() != null &&
                    output.result_record().result_class().ERROR() != null &&
                    output.result_record().result().size() == 1 &&
                    output.result_record().result(0).variable().getText().equals("msg")) {
                return Utils.extractValue(output.result_record().result(0).value().getText());
            } else {
                return msg;
            }
        } catch (GdbException e) {
            return msg;
        }
    }

}
