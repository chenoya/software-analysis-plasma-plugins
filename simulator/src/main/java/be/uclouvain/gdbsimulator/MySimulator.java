package be.uclouvain.gdbsimulator;

import be.uclouvain.gdbmiapi.GDBException;
import be.uclouvain.gdbmiapi.GdbProcess;
import be.uclouvain.gdbmiapi.commands.*;
import fr.inria.plasmalab.workflow.data.AbstractModel;
import fr.inria.plasmalab.workflow.data.simulation.InterfaceIdentifier;
import fr.inria.plasmalab.workflow.data.simulation.InterfaceState;
import fr.inria.plasmalab.workflow.exceptions.PlasmaDataException;
import fr.inria.plasmalab.workflow.exceptions.PlasmaDeadlockException;
import fr.inria.plasmalab.workflow.exceptions.PlasmaSimulatorException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MySimulator extends AbstractModel {

    private static IdVariableName PC_ID = new IdVariableName("pc");
    private static IdVariableName LINE_ID = new IdVariableName("line");
    private static IdVariableName CF_ID = new IdVariableName("cf");
    private static IdVariableName OF_ID = new IdVariableName("of");
    private static ArrayList<IdVariableName> VARIDS = new ArrayList<>();

    private String filePath = null;

    private GdbProcess gdbProcess = null;

    private List<InterfaceState> trace;

    public MySimulator(String name, String content, String id) {
        this.name = name;
        this.content = content;
        this.errors = new ArrayList<>();
        this.origin = null;
        this.id = id;
    }

    public MySimulator(String name, java.io.File file, String id) throws PlasmaDataException {
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
        if(result.length != 2) {
            errors.add(new PlasmaDataException("Need two lines : variable list + path to executable"));
            return true;
        }

        String[] variableNames = result[0].split(" ");
        for (String v : variableNames) {
            VARIDS.add(new IdVariableName(v));
        }

        java.io.File file = new java.io.File(result[1]);

        if (!(file.canRead() && file.isFile() && file.canExecute())) {
            errors.add(new PlasmaDataException("Impossible to read executable"));
            return true;
        }

        filePath = result[1];

        //initialState = new MyState(1,1);

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
            ProgramExecution.start(gdbProcess);
        } catch (IOException | GDBException e) {
            e.printStackTrace();
        }

        try {
            trace = new ArrayList<>();
            trace.add(simulate_step());
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

    private MyState simulate_step() throws IOException {
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
        for (InterfaceIdentifier i : VARIDS) {
            //TODO specific type instead of ValueInt
            try {
                map.put(i, new IntValue(Integer.parseInt(DataManipulation.data_eval_expr(gdbProcess, i.getName()))));
            } catch (Exception e) {
                map.put(i, new NoValue());
            }
        }
        return new MyState(map);
    }

    @Override
    public InterfaceState simulate() throws PlasmaSimulatorException {
        try {
            ProgramExecution.nexti(gdbProcess);
            if (File.info_source(gdbProcess).get("file").equals("../csu/libc-start.c")) {
                throw new PlasmaDeadlockException(getCurrentState(), getTraceLength());
            } else {
                trace.add(simulate_step());
                return getCurrentState();
            }

        } catch (IOException | GDBException e) {
            throw new PlasmaSimulatorException(e.getMessage());
        }
    }

    @Override
    public void backtrack() throws PlasmaSimulatorException {
        //if (trace.size() == 1)
            throw new PlasmaSimulatorException("Trace is already at initial state: cannot backtrack");
        /*else {
            int length = trace.size() - 1;
            newPath();
            while (trace.size() < length) {
                simulate();
            }
        }*/
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
        return res.toArray(new InterfaceIdentifier[] {});
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
