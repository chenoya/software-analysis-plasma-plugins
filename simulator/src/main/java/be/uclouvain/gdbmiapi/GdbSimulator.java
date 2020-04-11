package be.uclouvain.gdbmiapi;

import be.uclouvain.gdbmiapi.commands.*;
import be.uclouvain.softwaresimulator.Simulator;
import be.uclouvain.softwaresimulator.VariableIdentifier;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class GdbSimulator extends Simulator {

    private GdbProcess gdbProcess;

    private final static VariableIdentifier CF_ID = new VariableIdentifier("CF", "CF");
    private final static VariableIdentifier OF_ID = new VariableIdentifier("OF", "OF");
    private final static VariableIdentifier STACK_MODIFIED = new VariableIdentifier("STACK_M", "STACK_M");

    private static boolean stackWatchAvailable;
    private static byte[] stackContent;
    private static long stackStart;
    private static long stackEnd;


    public GdbSimulator(Path sourceFile, String functionName) throws IOException {
        super(sourceFile, functionName);
        gdbProcess = new GdbProcess(new PrintStream(OutputStream.nullOutputStream()));
        File.file(gdbProcess, sourceFile);
        Breakpoint.break_(gdbProcess, "*" + functionName);
    }

    @Override
    public void start() throws IOException {
        ProgramExecution.run(gdbProcess);

        String pid;
        try {
            pid = DataManipulation.data_eval_expr(gdbProcess, "(int) getpid()");
        } catch (IOException e) {
            stackWatchAvailable = false;
            System.err.println("Can not get PID of process : " + e.getMessage());
            return;
        }
        final Long[] start = {null};
        final Long[] end = {null};
        try {
            Files.lines(Paths.get("/proc", pid, "maps")).forEach((line) -> {
                if (line.contains("[stack]")) {
                    String[] s = line.split("[ -]");
                    try {
                        start[0] = Long.parseUnsignedLong(s[0], 16);
                        end[0] = Long.parseUnsignedLong(s[1], 16);
                    } catch (IndexOutOfBoundsException | NumberFormatException ignored) {
                    }
                }
            });
        } catch (IOException e) {
            stackWatchAvailable = false;
            System.err.println("Can not read process infos : " + e.getMessage());
            return;
        }
        long current;
        try {
            current = Long.parseUnsignedLong(DataManipulation.data_eval_expr(gdbProcess, "$sp").substring(2), 16);
        } catch (IOException e) {
            stackWatchAvailable = false;
            System.err.println("Can not evaluate $sp : " + e.getMessage());
            return;
        }
        if (start[0] == null || end[0] == null) {
            stackWatchAvailable = false;
            System.err.println("Can not get stack bounds from system");
            return;
        }
        if (current < start[0] || current >= end[0]) {
            stackWatchAvailable = false;
            System.err.println("Stack pointer out of stack bounds");
            return;
        }
        stackContent = null;
        stackStart = current;
        stackEnd = end[0];
    }

    @Override
    public boolean nextInstruction() throws IOException {
        ProgramExecution.nexti(gdbProcess);
        StackManipulation.Frame[] backtrace = StackManipulation.backtrace(gdbProcess);
        String currentFunc = backtrace[0].getFunc();
        return this.functionName.equals(currentFunc);
    }

    @Override
    public Optional<Long> getProgramCounter() {
        try {
            return Optional.of(Long.parseUnsignedLong(DataManipulation.data_eval_expr(gdbProcess, "$pc")
                    .split(" ")[0].substring(2), 16));
        } catch (IOException | NumberFormatException e) {
            System.err.println("Can not evaluate $pc : " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<Long> getFileLine() {
        try {
            return Optional.of(File.info_source(gdbProcess).getLine());
        } catch (IOException | NumberFormatException e) {
            System.err.println("Can not evaluate line number : " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public List<VariableIdentifier> getSimulatorSpecificValuesNames() {
        return Arrays.asList(CF_ID, OF_ID);
    }

    @Override
    public Map<VariableIdentifier, Optional<Double>> getSimulatorSpecificValues() {
        Map<VariableIdentifier, Optional<Double>> map = new HashMap<>();

        try {
            map.put(CF_ID, Optional.of(DataManipulation.data_eval_expr(gdbProcess, "$eflags").contains("CF") ? 1.0 : 0.0));
        } catch (Exception e) {
            System.err.println("Can not evaluate flags : " + e.getMessage());
            map.put(CF_ID, Optional.empty());
        }

        try {
            map.put(OF_ID, Optional.of(DataManipulation.data_eval_expr(gdbProcess, "$eflags").contains("OF") ? 1.0 : 0.0));
        } catch (Exception e) {
            System.err.println("Can not evaluate flags : " + e.getMessage());
            map.put(OF_ID, Optional.empty());
        }

        try {
            if (stackWatchAvailable) {
                byte[] newStack = DataManipulation.data_read_memory(gdbProcess, stackStart, stackEnd-stackStart);
                boolean changed = stackContent != null && Arrays.compare(stackContent, newStack) != 0;
                stackContent = newStack;
                map.put(STACK_MODIFIED, Optional.of(changed ? 1.0 : 0.0));
            } else {
                map.put(STACK_MODIFIED, Optional.empty());
            }
        } catch (Exception e) {
            System.err.println("Can not evaluate stack content : " + e.getMessage());
            map.put(STACK_MODIFIED, Optional.empty());
        }

        return map;
    }

    @Override
    public Optional<Boolean> evaluateBooleanExpression(String expression) {
        try {
            String s = DataManipulation.data_eval_expr(gdbProcess, expression);
            if (s.equals("1"))
                return Optional.of(true);
            else if (s.equals("0"))
                return Optional.of(false);
            else {
                System.err.println("Not a boolean expression : \"" + expression + "\"");
                return Optional.empty();
            }
        } catch (IOException e) {
            System.err.println("Can not evaluate : \"" + expression + "\" : " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<Double> evaluateDoubleExpression(String expression) {
        try {
            return Optional.of(Double.parseDouble(DataManipulation.data_eval_expr(gdbProcess, expression)));
        } catch (IOException | NumberFormatException e) {
            System.err.println("Can not evaluate expression : \"" + expression + "\" : " + e.getMessage());
            return Optional.empty();
        }
    }
}
