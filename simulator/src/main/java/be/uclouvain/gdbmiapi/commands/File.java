package be.uclouvain.gdbmiapi.commands;

import be.uclouvain.gdbmiapi.*;
import org.apache.commons.text.StringEscapeUtils;

import java.io.IOException;
import java.nio.file.Path;

import static be.uclouvain.gdbmiapi.Utils.assertOrThrow;

public class File {
    /**
     * Specify the executable file to be debugged. This file is the one from which the symbol table is also read.
     * If no file is specified, the command clears the executable and symbol information.
     * If breakpoints are set when using this command with no arguments, GDB will produce error messages.
     * Otherwise, no output is produced, except a completion notification.<br><br>
     * The corresponding GDB command is <b>‘file’</b>.
     *
     * @param gdbProcess the gdb instance
     * @param file  the file path
     * @throws IOException
     * @throws GdbException
     */
    public static void file(GdbProcess gdbProcess, Path file) throws IOException, GdbException {
        String res = gdbProcess.executeGDBCommand("-file-exec-and-symbols \"" + StringEscapeUtils.escapeJava(file.toString()) + "\"");
        Utils.checkDone(res);
    }

    /**
     * Specify the executable file to be debugged. Unlike ‘-file-exec-and-symbols’,
     * the symbol table is not read from this file.
     * If used without argument, GDB clears the information about the executable file.
     * No output is produced, except a completion notification.<br><br>
     * The corresponding GDB command is <b>‘exec-file’</b>.
     *
     * @param gdbProcess the gdb instance
     * @param file  the file path
     * @throws IOException
     * @throws GdbException
     */
    public static void file_exec(GdbProcess gdbProcess, Path file) throws IOException, GdbException {
        String res = gdbProcess.executeGDBCommand("-file-exec-file \"" + StringEscapeUtils.escapeJava(file.toString()) + "\"");
        Utils.checkDone(res);
    }

    public static class InfoSourceOutput {
        private long line;
        private String file;
        private String fullName;
        private boolean macroInfo;

        public InfoSourceOutput(long line, String file, String fullName, boolean macroInfo) {
            this.line = line;
            this.file = file;
            this.fullName = fullName;
            this.macroInfo = macroInfo;
        }

        public long getLine() {
            return line;
        }

        public String getFile() {
            return file;
        }

        public String getFullName() {
            return fullName;
        }

        public boolean getMacroInfo() {
            return macroInfo;
        }
    }

    /**
     * List the line number, the current source file, and the absolute path to the current source file for the current
     * executable. The macro information field has a value of ‘1’ or ‘0’ depending on whether or not the file includes
     * preprocessor macro information.<br><br>
     * The GDB equivalent is <b>‘info source’</b>
     *
     * @param gdbProcess the gdb instance
     * @throws IOException
     * @throws GdbException
     */
    public static InfoSourceOutput info_source(GdbProcess gdbProcess) throws IOException, GdbException {
        String res = gdbProcess.executeGDBCommand("-file-list-exec-source-file");
        MIOutputParser.OutputContext output = ParseMI.parse(res);
        assertOrThrow(res, output.result_record() != null);
        assertOrThrow(res, output.result_record().result_class().DONE() != null);
        assertOrThrow(res, output.result_record().result().size() == 4);
        assertOrThrow(res, output.result_record().result(0).variable().getText().equals("line"));
        assertOrThrow(res, output.result_record().result(1).variable().getText().equals("file"));
        assertOrThrow(res, output.result_record().result(2).variable().getText().equals("fullname"));
        assertOrThrow(res, output.result_record().result(3).variable().getText().equals("macro-info"));
        return new InfoSourceOutput(
                Long.parseUnsignedLong(Utils.extractValue(output.result_record().result(0).value().getText())),
                Utils.extractValue(output.result_record().result(1).value().getText()),
                Utils.extractValue(output.result_record().result(2).value().getText()),
                Boolean.parseBoolean(Utils.extractValue(output.result_record().result(3).value().getText()))
        );
    }

}
