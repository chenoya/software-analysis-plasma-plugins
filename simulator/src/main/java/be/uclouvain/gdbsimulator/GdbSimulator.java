package be.uclouvain.gdbsimulator;

import be.uclouvain.gdbmiapi.GdbException;
import be.uclouvain.gdbmiapi.GdbProcess;
import be.uclouvain.gdbmiapi.commands.*;
import be.uclouvain.gdbmiapi.commands.File;
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

    private static GdbExpression PC_ID = new GdbExpression("pc", null);
    private static GdbExpression LINE_ID = new GdbExpression("line", null);
    private static GdbExpression CF_ID = new GdbExpression("cf", null);
    private static GdbExpression OF_ID = new GdbExpression("of", null);
    private static ArrayList<GdbExpression> VARIDS = new ArrayList<>();
    private static String METHOD = "";

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
        VARIDS.clear();

        // Verify model content
        String[] result = content.split(System.lineSeparator());
        if (result.length < 3) {
            errors.add(new PlasmaDataException("Need at least two lines : path to executable + function name + named expressions list"));
            return true;
        }

        java.io.File file = new java.io.File(result[0]);
        if (!(file.canRead() && file.isFile() && file.canExecute())) {
            errors.add(new PlasmaDataException("Impossible to read executable"));
            return true;
        }
        filePath = result[0];

        METHOD = result[1];

        for (int i = 2; i < result.length; i++) {
            String[] parts = result[i].split(" @ ");
            if (parts.length != 2) {
                errors.add(new PlasmaDataException("Watched expression must be written as : 'name' @ 'expr'"));
                return true;
            }
            VARIDS.add(new GdbExpression(parts[0], parts[1]));
        }

        return false;
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
            Breakpoint.break_(gdbProcess, "*" + METHOD);
            ProgramExecution.run(gdbProcess);
        } catch (IOException | GdbException e) {
            throw new PlasmaSimulatorException(e.getMessage());
        }

        try {
            trace = new ArrayList<>();
            trace.add(fill_state());
            return getCurrentState();
        } catch (IOException e) {
            throw new PlasmaSimulatorException(e.getMessage());
        }
    }

    @Override
    public InterfaceState newPath(List<InterfaceState> arg0) throws PlasmaSimulatorException {
        return newPath();
    }

    @Override
    public InterfaceState newPath(long arg0) throws PlasmaSimulatorException {
        return newPath();
    }

    private GdbState fill_state() throws IOException {
        Map<InterfaceIdentifier, Value> map = new HashMap<>();
        try {
            map.put(PC_ID, new LongValue(Long.parseUnsignedLong(DataManipulation.data_eval_expr(gdbProcess, "$pc").split(" ")[0].substring(2), 16)));
        } catch (Exception e) {
            map.put(PC_ID, new NoValue());
        }
        try {
            map.put(LINE_ID, new LongValue((Long) File.info_source(gdbProcess).get("line")));
        } catch (Exception e) {
            map.put(LINE_ID, new NoValue());
        }
        try {
            map.put(CF_ID, new IntValue(DataManipulation.data_eval_expr(gdbProcess, "$eflags").contains("CF") ? 1 : 0));
        } catch (Exception e) {
            map.put(CF_ID, new NoValue());
        }
        try {
            map.put(OF_ID, new IntValue(DataManipulation.data_eval_expr(gdbProcess, "$eflags").contains("OF") ? 1 : 0));
        } catch (Exception e) {
            map.put(OF_ID, new NoValue());
        }
        for (GdbExpression i : VARIDS) {
            //TODO specific type instead of ValueInt
            try {
                map.put(i, new IntValue(Integer.parseInt(DataManipulation.data_eval_expr(gdbProcess, i.getExpr()))));
            } catch (Exception e) {
                map.put(i, new NoValue());
            }
        }
        return new GdbState(map);
    }

    @Override
    public InterfaceState simulate() throws PlasmaSimulatorException {
        try {
            ProgramExecution.nexti(gdbProcess);
            Map<String, Object> backtrace = StackManipulation.backtrace(gdbProcess);
            String currentFunc = (String) backtrace.get("func");
            if (!METHOD.equals(currentFunc)) {
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
        ArrayList<InterfaceIdentifier> res = new ArrayList<>(VARIDS.size() + 2);
        res.add(PC_ID);
        res.add(LINE_ID);
        res.add(CF_ID);
        res.add(OF_ID);
        res.addAll(VARIDS);
        return res.toArray(new InterfaceIdentifier[]{});
    }

    @Override
    public Map<String, InterfaceIdentifier> getIdentifiers() {
        Map<String, InterfaceIdentifier> mymap = new HashMap<String, InterfaceIdentifier>();
        mymap.put(PC_ID.getName(), PC_ID);
        mymap.put(LINE_ID.getName(), LINE_ID);
        mymap.put(CF_ID.getName(), CF_ID);
        mymap.put(OF_ID.getName(), OF_ID);
        for (InterfaceIdentifier i : VARIDS) {
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
        // TODO Auto-generated method stub
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
