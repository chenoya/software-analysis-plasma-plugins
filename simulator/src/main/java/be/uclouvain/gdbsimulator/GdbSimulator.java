package be.uclouvain.gdbsimulator;

import be.uclouvain.gdbmiapi.GdbException;
import be.uclouvain.gdbmiapi.GdbProcess;
import be.uclouvain.gdbmiapi.commands.*;
import be.uclouvain.gdbmiapi.commands.File;
import be.uclouvain.gdbsimulator.value.*;
import fr.inria.plasmalab.workflow.data.AbstractModel;
import fr.inria.plasmalab.workflow.data.simulation.InterfaceIdentifier;
import fr.inria.plasmalab.workflow.data.simulation.InterfaceState;
import fr.inria.plasmalab.workflow.exceptions.PlasmaDataException;
import fr.inria.plasmalab.workflow.exceptions.PlasmaDeadlockException;
import fr.inria.plasmalab.workflow.exceptions.PlasmaSimulatorException;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

public class GdbSimulator extends AbstractModel {

    private static GdbExpression PC_ID = new GdbExpression("pc", "$pc", (g) -> {
        try {
            return new LongValue(Long.parseUnsignedLong(DataManipulation.data_eval_expr(g, "$pc").split(" ")[0].substring(2), 16));
        } catch (Exception e) {
            System.err.println("Can not evaluate $pc : " + e.getMessage());
            return new NoValue(e.getMessage());
        }
    });
    private static GdbExpression LINE_ID = new GdbExpression("line", "line", (g) -> {
        try {
            return new LongValue((Long) File.info_source(g).get("line"));
        } catch (Exception e) {
            System.err.println("Can not evaluate line number : " + e.getMessage());
            return new NoValue(e.getMessage());
        }
    });
    private static GdbExpression CF_ID = new GdbExpression("CF", "CF", (g) -> {
        try {
            return new BoolValue(DataManipulation.data_eval_expr(g, "$eflags").contains("CF"));
        } catch (Exception e) {
            System.err.println("Can not evaluate flags : " + e.getMessage());
            return new NoValue(e.getMessage());
        }
    });
    private static GdbExpression OF_ID = new GdbExpression("OF", "OF", (g) -> {
        try {
            return new BoolValue(DataManipulation.data_eval_expr(g, "$eflags").contains("OF"));
        } catch (Exception e) {
            System.err.println("Can not evaluate flags : " + e.getMessage());
            return new NoValue(e.getMessage());
        }
    });

    private static ArrayList<GdbExpression> exprFromSimu = new ArrayList<>();
    private static ArrayList<GdbExpression> exprFromCheck = new ArrayList<>();
    private static String method = "";

    private String filePath = null;

    private GdbProcess gdbProcess = null;

    private List<InterfaceState> trace;

    public GdbSimulator(String name, String content, String id) {
        this.name = name;
        this.content = content;
        this.errors = new ArrayList<>();
        this.origin = null;
        this.id = id;
    }

    public GdbSimulator(String name, java.io.File file, String id) throws PlasmaDataException {
        this.name = name;
        this.content = "";
        this.errors = new ArrayList<>();
        this.origin = file;
        this.id = id;
        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            while (br.ready())
                content = content + br.readLine() + "\n";
            br.close();
        } catch (IOException e) {
            throw new PlasmaDataException("Cannot read model file", e);
        }
    }


    @Override
    public boolean checkForErrors() {
        // Empty from previous errors
        errors.clear();
        exprFromSimu.clear();

        // Verify model content
        String[] result = content.split(System.lineSeparator());
        if (result.length < 2) {
            errors.add(new PlasmaDataException("Need at least two lines : path to executable + function name + named expressions list (optional)"));
            return true;
        }

        java.io.File file = new java.io.File(result[0]);
        if (!(file.canRead() && file.isFile() && file.canExecute())) {
            errors.add(new PlasmaDataException("Impossible to read executable"));
            return true;
        }
        filePath = result[0];

        method = result[1];

        for (int i = 2; i < result.length; i++) {
            String[] parts = result[i].split(" @ ");
            if (parts.length != 2) {
                errors.add(new PlasmaDataException("Watched expression must be written as : 'name' @ 'expr'"));
                return true;
            }
            exprFromSimu.add(new GdbExpression(parts[0], parts[1], GdbExpression.makeDoubleParser(parts[1])));
        }

        return false;
    }

    @SuppressWarnings("unused")
    //Used through reflexion from the Checker.
    public void addGdbExpressions(Map<String, String> pairs) {
        exprFromCheck.clear();
        pairs.forEach((name, expr) -> exprFromCheck.add(new GdbExpression(name, expr, GdbExpression.makeBoolParser(expr))));
    }

    @Override
    public void setValueOf(Map<InterfaceIdentifier, Double> update) throws PlasmaSimulatorException {
        throw new PlasmaSimulatorException("No impl of setValueOf");
    }

    @Override
    public InterfaceState newPath() throws PlasmaSimulatorException {
        try {
            //gdbMI = new GdbMI(System.out);
            gdbProcess = new GdbProcess(new PrintStream(OutputStream.nullOutputStream()));
            File.file(gdbProcess, Paths.get(filePath));
            Breakpoint.break_(gdbProcess, "*" + method);
            ProgramExecution.run(gdbProcess);
        } catch (IOException | GdbException e) {
            throw new PlasmaSimulatorException(e.getMessage());
        }

        trace = new ArrayList<>();
        trace.add(fill_state());
        return getCurrentState();
    }

    @Override
    public InterfaceState newPath(List<InterfaceState> arg0) throws PlasmaSimulatorException {
        return newPath();
    }

    @Override
    public InterfaceState newPath(long arg0) throws PlasmaSimulatorException {
        return newPath();
    }

    private GdbState fill_state() {
        Map<InterfaceIdentifier, Value> map = new HashMap<>();
        map.put(PC_ID, PC_ID.getParser().apply(gdbProcess));
        map.put(LINE_ID, LINE_ID.getParser().apply(gdbProcess));
        map.put(CF_ID, CF_ID.getParser().apply(gdbProcess));
        map.put(OF_ID, OF_ID.getParser().apply(gdbProcess));
        for (GdbExpression i : exprFromSimu) {
            map.put(i, i.getParser().apply(gdbProcess));
        }
        for (GdbExpression i : exprFromCheck) {
            map.put(i, i.getParser().apply(gdbProcess));
        }
        return new GdbState(map);
    }

    @Override
    public InterfaceState simulate() throws PlasmaSimulatorException {
        try {
            ProgramExecution.nexti(gdbProcess);
            Map<String, Object> backtrace = StackManipulation.backtrace(gdbProcess);
            String currentFunc = (String) backtrace.get("func");
            if (!method.equals(currentFunc)) {
                throw new PlasmaDeadlockException(getCurrentState(), getTraceLength());
            }
            trace.add(fill_state());
            return getCurrentState();
        } catch (IOException | GdbException e) {
            throw new PlasmaSimulatorException(e.getMessage());
        }
    }

    @Override
    public void backtrack() throws PlasmaSimulatorException {
        throw new PlasmaSimulatorException("Cannot backtrack");
    }

    @Override
    public void cut(int arg0) throws PlasmaSimulatorException {
        throw new PlasmaSimulatorException("Not implemented");
    }

    @Override
    public InterfaceState getCurrentState() {
        return trace.get(getTraceLength() - 1);
    }

    @Override
    public int getDeadlockPos() {
        return getTraceLength() - 1;
    }

    @Override
    public InterfaceIdentifier[] getHeaders() {
        ArrayList<InterfaceIdentifier> res = new ArrayList<>(exprFromSimu.size() + 4);
        res.add(PC_ID);
        res.add(LINE_ID);
        res.add(CF_ID);
        res.add(OF_ID);
        res.addAll(exprFromSimu);
        return res.toArray(new InterfaceIdentifier[]{});
    }

    @Override
    public Map<String, InterfaceIdentifier> getIdentifiers() {
        Map<String, InterfaceIdentifier> mymap = new HashMap<String, InterfaceIdentifier>();
        mymap.put(PC_ID.getName(), PC_ID);
        mymap.put(LINE_ID.getName(), LINE_ID);
        mymap.put(CF_ID.getName(), CF_ID);
        mymap.put(OF_ID.getName(), OF_ID);
        for (InterfaceIdentifier i : exprFromSimu) {
            mymap.put(i.getName(), i);
        }
        for (InterfaceIdentifier i : exprFromCheck) {
            mymap.put(i.getName(), i);
        }
        return mymap;
    }

    @Override
    public InterfaceState getStateAtPos(int index) {
        return trace.get(index);
    }

    @Override
    public List<InterfaceIdentifier> getStateProperties() {
        return null;
    }

    @Override
    public InterfaceIdentifier getTimeId() {
        return null;
    }

    @Override
    public List<InterfaceState> getTrace() {
        return trace;
    }

    @Override
    public boolean hasTime() {
        return false;
    }


}
