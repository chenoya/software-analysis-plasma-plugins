package be.uclouvain.softwaresimulator;

import be.uclouvain.Utils;
import be.uclouvain.gdbmiapi.GdbSimulator;
import be.uclouvain.softwarebltlchecker.SoftwareBLTLChecker;
import com.moandjiezana.toml.Toml;
import fr.inria.plasmalab.workflow.data.AbstractModel;
import fr.inria.plasmalab.workflow.data.simulation.InterfaceIdentifier;
import fr.inria.plasmalab.workflow.data.simulation.InterfaceState;
import fr.inria.plasmalab.workflow.exceptions.PlasmaDataException;
import fr.inria.plasmalab.workflow.exceptions.PlasmaDeadlockException;
import fr.inria.plasmalab.workflow.exceptions.PlasmaSimulatorException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SoftwareSimulator extends AbstractModel {

    private static final VariableIdentifier PC_ID = new VariableIdentifier("pc", "$pc");
    private static final VariableIdentifier LINE_ID = new VariableIdentifier("line", "line");

    private static final List<VariableIdentifier> exprFromSimu = new ArrayList<>();
    private static final ArrayList<SoftwareBLTLChecker> registeredCheckers = new ArrayList<>();

    private String executable = null;
    private String function = "";
    private boolean withCache = true;

    private Simulator simulator = null;

    private List<InterfaceState> trace;

    private boolean atDeadlock = false;

    // creating a new model
    public SoftwareSimulator(String name, String id) {
        this.name = name;
        this.errors = new ArrayList<>();
        this.origin = null;
        this.id = id;
        this.content = "executable = \"\"\n" +
                "function = \"main\"\n" +
                "\n" +
                "[simulator]\n" +
                "name = \"gdb\"\n" +
                "\n" +
                "[simulator.options]\n" +
                "CF = true\n" +
                "OF = true\n" +
                "STACK_M = true\n" +
                "gdb_path = \"/usr/bin/gdb\"\n" +
                "\n" +
                "[variables]\n";
    }

    // opening a saved model
    public SoftwareSimulator(String name, String content, String id) {
        this.name = name;
        this.errors = new ArrayList<>();
        this.origin = null;
        this.id = id;
        this.content = content;
    }

    // importing model from file
    public SoftwareSimulator(String name, java.io.File file, String id) throws PlasmaDataException {
        this.name = name;
        this.errors = new ArrayList<>();
        this.origin = file;
        this.id = id;
        try {
            this.content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PlasmaDataException("Cannot read model file", e);
        }
    }

    public SoftwareSimulator(java.io.File file, String function) {
        this.executable = file.getPath();
        this.function = function;
    }

    public void addGdbExpressions(SoftwareBLTLChecker softwareBLTLChecker) {
        registeredCheckers.add(softwareBLTLChecker);
    }

    public String getExecutable() {
        return executable;
    }

    public void setExecutable(String newExecutable) {
        this.executable = newExecutable;
    }

    public void setWithCache(boolean withCache) {
        this.withCache = withCache;
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
        for (SoftwareBLTLChecker softwareBLTLChecker : registeredCheckers) {
            for (VariableIdentifier i : softwareBLTLChecker.getVariableIdentifiers()) {
                map.put(i, simulator.evaluateBooleanExpression(i.getExpr()).map(b -> b ? 1.0 : 0.0).orElse(Double.NaN));
            }
        }

        return new SoftwareState(map);
    }

    /* ***** FROM AbstractModel ***************************************************************************************/

    @Override
    public InterfaceState newPath() throws PlasmaSimulatorException {
        atDeadlock = false;
        try {
            simulator.start(executable, function, withCache);
        } catch (IOException e) {
            throw new PlasmaSimulatorException(e.getMessage());
        }

        trace = new ArrayList<>();
        trace.add(fill_state());
        return getCurrentState();
    }

    @Override
    public InterfaceState newPath(List<InterfaceState> list) throws PlasmaSimulatorException {
        throw new PlasmaSimulatorException("Cannot instantiate model from trace.");
    }

    @Override
    public InterfaceState newPath(long seed) throws PlasmaSimulatorException {
        // no random generator so ignore the seed
        return newPath();
    }

    @Override
    public InterfaceState simulate() throws PlasmaSimulatorException {
        //when dealing with multiple checkers,
        // simulate() can be called even after previously raising PlasmaDeadlockException
        if (atDeadlock) {
            throw new PlasmaDeadlockException(getCurrentState(), getTraceLength());
        }
        try {
            boolean stillInFct = simulator.nextInstruction();
            if (!stillInFct) {
                atDeadlock = true;
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
    public void cut(int i) throws PlasmaSimulatorException {
        throw new PlasmaSimulatorException("Cannot cut trace");
    }

    @Override
    public void setValueOf(Map<InterfaceIdentifier, Double> map) throws PlasmaSimulatorException {
        throw new PlasmaSimulatorException("Set of values is not implemented");
    }

    @Override
    public void clean() throws PlasmaSimulatorException {
        super.clean();
        registeredCheckers.clear();
    }

    @Override
    public InterfaceState getCurrentState() {
        return trace.get(getTraceLength() - 1);
    }

    @Override
    public InterfaceState getStateAtPos(int i) {
        return trace.get(i);
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
    public int getDeadlockPos() {
        return getTraceLength() - 1;
    }

    @Override
    public List<InterfaceIdentifier> getStateProperties() {
        return null;
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
        for (SoftwareBLTLChecker softwareBLTLChecker : registeredCheckers) {
            for (InterfaceIdentifier i : softwareBLTLChecker.getVariableIdentifiers()) {
                map.put(i.getName(), i);
            }
        }
        return map;
    }

    @Override
    public InterfaceIdentifier getTimeId() {
        return null;
    }

    @Override
    public boolean hasTime() {
        return false;
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

    // public List<Variable> getOptimizationVariables() --> same as super

    // public List<VariableConstraint> getOptimizationConstraints() --> same as super

    /* ***** FROM AbstractData ****************************************************************************************/

    // public String getName() --> same as super

    // public void rename(String newName) --> same as super

    // public String getId() --> same as super

    // public String getContent() --> same as super

    // public boolean isEmpty() --> same as super

    // public File getOrigin() --> same as super

    // public String toString() --> same as super

    // public List<PlasmaDataException> getErrors() --> same as super

    // public void updateContent(String newContent) --> same as super

    // public void exportTo(File file) throws IOException --> same as super

    // public List<AbstractFunction> getFunctions() --> same as super

    @Override
    public boolean checkForErrors() {
        errors.clear();
        exprFromSimu.clear();

        Toml toml;
        try {
            toml = new Toml().read(content);
        } catch (IllegalStateException e) {
            errors.add(new PlasmaDataException(e.getMessage()));
            return true;
        }

        executable = Utils.getFromTOML(toml::getString, "executable");
        if (executable == null || executable.isEmpty()) {
            errors.add(new PlasmaDataException("The 'executable' option must be filled."));
            return true;
        }
        java.io.File file = new java.io.File(executable);
        if (!(file.canRead() && file.isFile())) {
            errors.add(new PlasmaDataException("Impossible to read 'executable'."));
            return true;
        }

        function = Utils.getFromTOML(toml::getString, "function");
        if (function == null || function.isEmpty()) {
            errors.add(new PlasmaDataException("The 'function' option must be filled."));
            return true;
        }

        Toml vars;
        try {
            vars = Utils.getFromTOML(toml::getTable, "variables");
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

        String s = Utils.getFromTOML(toml::getString, "simulator.name");
        if (s == null || s.isEmpty()) {
            errors.add(new PlasmaDataException("The 'simulator.name' option must be filled."));
            return true;
        }
        try {
            if (s.equals("gdb") || s.equals("GDB")) {
                simulator = new GdbSimulator(options);
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

}
