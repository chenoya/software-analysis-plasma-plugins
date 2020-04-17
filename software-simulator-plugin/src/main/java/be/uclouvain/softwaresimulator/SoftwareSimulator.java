package be.uclouvain.softwaresimulator;

import be.uclouvain.gdbmiapi.GdbSimulator;
import be.uclouvain.gdbmiapi.Utils;
import com.moandjiezana.toml.Toml;
import fr.inria.plasmalab.workflow.data.AbstractModel;
import fr.inria.plasmalab.workflow.data.simulation.InterfaceIdentifier;
import fr.inria.plasmalab.workflow.data.simulation.InterfaceState;
import fr.inria.plasmalab.workflow.exceptions.PlasmaDataException;
import fr.inria.plasmalab.workflow.exceptions.PlasmaDeadlockException;
import fr.inria.plasmalab.workflow.exceptions.PlasmaSimulatorException;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SoftwareSimulator extends AbstractModel {

    private static VariableIdentifier PC_ID = new VariableIdentifier("pc", "$pc");
    private static VariableIdentifier LINE_ID = new VariableIdentifier("line", "line");

    private static List<VariableIdentifier> exprFromSimu = new ArrayList<>();
    private static List<VariableIdentifier> exprFromCheck = new ArrayList<>();

    private String executable = null;
    private String function = "";

    private Simulator simulator = null;

    private List<InterfaceState> trace;

    public SoftwareSimulator(String name, String content, String id) {
        this.name = name;
        this.content = content;
        this.errors = new ArrayList<>();
        this.origin = null;
        this.id = id;
    }

    public SoftwareSimulator(String name, java.io.File file, String id) throws PlasmaDataException {
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

    public SoftwareSimulator(java.io.File file, String function) {
        this.executable = file.getPath();
        this.function = function;
    }

    private <T> T getFromTOML(Function<String, T> f, String name) {
        try {
            return f.apply(name);
        } catch (ClassCastException e) {
            return null;
        }
    }

    @Override
    public boolean checkForErrors() {
        // Empty from previous errors
        errors.clear();
        exprFromSimu.clear();

        Toml toml;
        try {
            toml = new Toml().read(content);
        } catch (IllegalStateException e) {
            errors.add(new PlasmaDataException(e.getMessage()));
            return true;
        }

        executable = getFromTOML(toml::getString, "executable");
        if (executable == null || executable.isEmpty()) {
            errors.add(new PlasmaDataException("The 'executable' option must be filled."));
            return true;
        }
        java.io.File file = new java.io.File(executable);
        if (!(file.canRead() && file.isFile())) {
            errors.add(new PlasmaDataException("Impossible to read 'executable'."));
            return true;
        }

        function = getFromTOML(toml::getString, "function");
        if (function == null || function.isEmpty()) {
            errors.add(new PlasmaDataException("The 'function' option must be filled."));
            return true;
        }

        Toml vars;
        try {
            vars = getFromTOML(toml::getTable, "variables");
        } catch (ClassCastException e) {
            errors.add(new PlasmaDataException("The 'variables' option must be a table."));
            return true;
        }
        if (vars != null) {
            for (Map.Entry<String, Object> entry : vars.toMap().entrySet()) {
                String k = entry.getKey();
                Object v = entry.getValue();
                if (v instanceof String && !((String) v).isEmpty()) {
                    exprFromSimu.add(new VariableIdentifier(k, (String) v));
                } else {
                    errors.add(new PlasmaDataException("The content of the variable must be non-empty strings."));
                    return true;
                }
            }
        }

        Map<String, Object> options;
        try {
            Toml o = toml.getTable("simulator.options");
            options = (o == null) ? null : o.toMap();
        } catch (ClassCastException e) {
            errors.add(new PlasmaDataException("The 'simulator.options' option must be a table."));
            return true;
        }

        String s = getFromTOML(toml::getString, "simulator.name");
        if (s == null || s.isEmpty()) {
            errors.add(new PlasmaDataException("The 'simulator.name' option must be filled."));
            return true;
        }
        try {
            if (s.equals("gdb") || s.equals("GDB")) {
                simulator = new GdbSimulator(executable, function, options);
            } else {
                errors.add(new PlasmaDataException("Unknown 'simulator.name' (available options are 'gdb')."));
                return true;
            }
        } catch (IllegalArgumentException e) {
            errors.add(new PlasmaDataException(e.getMessage()));
            return true;
        }

        return false;
    }

    @SuppressWarnings("unused")
    //Used through reflexion from the Checker.
    public void addGdbExpressions(Map<String, String> pairs) {
        exprFromCheck.clear();
        pairs.forEach((name, expr) -> exprFromCheck.add(new VariableIdentifier(name, expr)));
    }

    @Override
    public void setValueOf(Map<InterfaceIdentifier, Double> update) throws PlasmaSimulatorException {
        throw new PlasmaSimulatorException("No impl of setValueOf");
    }

    @Override
    public InterfaceState newPath() throws PlasmaSimulatorException {
        try {
            simulator.start();
        } catch (IOException e) {
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

    private SoftwareState fill_state() {
        Map<VariableIdentifier, Double> map = new HashMap<>();
        map.put(PC_ID, simulator.getProgramCounter().map(Utils::longToDoubleOrNaN).orElse(Double.NaN));
        map.put(LINE_ID, simulator.getFileLine().map(Utils::longToDoubleOrNaN).orElse(Double.NaN));
        map.putAll(simulator.getSimulatorSpecificValues().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().orElse(Double.NaN))));
        for (VariableIdentifier i : exprFromSimu) {
            map.put(i, simulator.evaluateDoubleExpression(i.getExpr()).orElse(Double.NaN));
        }
        for (VariableIdentifier i : exprFromCheck) {
            map.put(i, simulator.evaluateBooleanExpression(i.getExpr()).map(b -> b ? 1.0 : 0.0).orElse(Double.NaN));
        }
        return new SoftwareState(map);
    }

    @Override
    public InterfaceState simulate() throws PlasmaSimulatorException {
        try {
            boolean stillInFct = simulator.nextInstruction();
            if (!stillInFct) {
                throw new PlasmaDeadlockException(getCurrentState(), getTraceLength());
            }
            trace.add(fill_state());
            return getCurrentState();
        } catch (IOException e) {
            throw new PlasmaSimulatorException(e.getMessage());
        } catch (NullPointerException e) {
            throw new PlasmaSimulatorException("You need to restart the path after changing the configuration.");
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
        res.addAll(simulator.getSimulatorSpecificValuesNames());
        res.addAll(exprFromSimu);
        return res.toArray(new InterfaceIdentifier[]{});
    }

    @Override
    public Map<String, InterfaceIdentifier> getIdentifiers() {
        Map<String, InterfaceIdentifier> map = new HashMap<>();
        map.put(PC_ID.getName(), PC_ID);
        map.put(LINE_ID.getName(), LINE_ID);
        if (simulator != null) {
            for (InterfaceIdentifier i : simulator.getSimulatorSpecificValuesNames()) {
                map.put(i.getName(), i);
            }
        }
        for (InterfaceIdentifier i : exprFromSimu) {
            map.put(i.getName(), i);
        }
        for (InterfaceIdentifier i : exprFromCheck) {
            map.put(i.getName(), i);
        }
        return map;
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
    public int getTraceLength() {
        return trace == null ? 0 : trace.size();
    }

    @Override
    public boolean hasTime() {
        return false;
    }

    public String getExecutable() {
        return executable;
    }

    public String getFunction() {
        return function;
    }
}
