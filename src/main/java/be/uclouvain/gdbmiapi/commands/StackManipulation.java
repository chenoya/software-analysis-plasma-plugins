package be.uclouvain.gdbmiapi.commands;

import be.uclouvain.gdbmiapi.GdbException;
import be.uclouvain.gdbmiapi.GdbProcess;
import be.uclouvain.gdbmiapi.MIOutputParser;
import be.uclouvain.gdbmiapi.ParseMI;

import java.io.IOException;
import java.util.Optional;

import static be.uclouvain.gdbmiapi.Utils.assertOrThrow;
import static be.uclouvain.gdbmiapi.Utils.extractValue;

public class StackManipulation {
    public static class Frame {
        private long level;
        private long addr;
        private String func;
        private String file;
        private String fullName;
        private Long line;

        public Frame(long level, long addr, String func, String file, String fullName, Long line) {
            this.level = level;
            this.addr = addr;
            this.func = func;
            this.file = file;
            this.fullName = fullName;
            this.line = line;
        }

        public long getLevel() {
            return level;
        }

        public long getAddr() {
            return addr;
        }

        public String getFunc() {
            return func;
        }

        public Optional<String> getFile() {
            return Optional.ofNullable(file);
        }

        public Optional<String> getFullName() {
            return Optional.ofNullable(fullName);
        }

        public Optional<Long> getLine() {
            return Optional.ofNullable(line);
        }
    }

    public static Frame[] backtrace(GdbProcess gdbProcess) throws IOException, GdbException {
        String res = gdbProcess.executeGDBCommand("-stack-list-frames");
        MIOutputParser.OutputContext output = ParseMI.parse(res);

        assertOrThrow(res, output.result_record() != null);
        assertOrThrow(res, output.result_record().result_class().DONE() != null);
        assertOrThrow(res, output.result_record().result().size() == 1);
        assertOrThrow(res, output.result_record().result(0).variable().getText().equals("stack"));
        assertOrThrow(res, output.result_record().result(0).value().list() != null);

        int nbFrame = output.result_record().result(0).value().list().result().size();
        assertOrThrow(res, nbFrame > 0);
        Frame[] frames = new Frame[nbFrame];
        for (int i = 0; i < nbFrame; i++) {
            assertOrThrow(res, output.result_record().result(0).value().list().result(i).variable().getText().equals("frame"));
            assertOrThrow(res, output.result_record().result(0).value().list().result(i).value().tuple() != null);
            int nbVar = output.result_record().result(0).value().list().result(i).value().tuple().result().size();
            assertOrThrow(res, nbVar >= 3);

            assertOrThrow(res, output.result_record().result(0).value().list().result(i).value().tuple().result(0).variable().getText().equals("level"));
            long level = Long.parseUnsignedLong(extractValue(output.result_record().result(0).value().list().result(i).value().tuple().result(0).value().getText()));
            assertOrThrow(res, output.result_record().result(0).value().list().result(i).value().tuple().result(1).variable().getText().equals("addr"));
            long addr = Long.parseUnsignedLong(extractValue(output.result_record().result(0).value().list().result(i).value().tuple().result(1).value().getText()).substring(2), 16);
            assertOrThrow(res, output.result_record().result(0).value().list().result(i).value().tuple().result(2).variable().getText().equals("func"));
            String func = extractValue(output.result_record().result(0).value().list().result(i).value().tuple().result(2).value().getText());

            String file = null;
            if (nbVar >= 4 && output.result_record().result(0).value().list().result(i).value().tuple().result(3).variable().getText().equals("file"))
                file = extractValue(output.result_record().result(0).value().list().result(i).value().tuple().result(3).value().getText());
            String fullName = null;
            if (nbVar >= 5 && output.result_record().result(0).value().list().result(i).value().tuple().result(4).variable().getText().equals("fullname"))
                fullName = extractValue(output.result_record().result(0).value().list().result(i).value().tuple().result(4).value().getText());
            Long line = null;
            if (nbVar >= 6 && output.result_record().result(0).value().list().result(i).value().tuple().result(5).variable().getText().equals("line"))
                line = Long.parseLong(extractValue(output.result_record().result(0).value().list().result(i).value().tuple().result(5).value().getText()));

            frames[i] = new Frame(level, addr, func, file, fullName, line);
        }
        return frames;
    }
}
