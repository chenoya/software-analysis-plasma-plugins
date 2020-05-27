package be.uclouvain.softwarebltlchecker;

import be.uclouvain.Utils;
import be.uclouvain.softwaresimulator.SoftwareSimulator;
import be.uclouvain.softwaresimulator.VariableIdentifier;
import com.moandjiezana.toml.Toml;
import fr.inria.plasmalab.bltl.BLTLRequirement;
import fr.inria.plasmalab.workflow.concrete.Variable;
import fr.inria.plasmalab.workflow.data.AbstractFunction;
import fr.inria.plasmalab.workflow.data.AbstractModel;
import fr.inria.plasmalab.workflow.data.AbstractRequirement;
import fr.inria.plasmalab.workflow.data.simulation.InterfaceState;
import fr.inria.plasmalab.workflow.exceptions.PlasmaCheckerException;
import fr.inria.plasmalab.workflow.exceptions.PlasmaDataException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class SoftwareBLTLChecker extends AbstractRequirement {

    // list of $expressions$ used in the LTL
    private final List<VariableIdentifier> variables = new ArrayList<>();
    // BLTL checker
    private final BLTLRequirement bltlRequirement;
    // the model to check
    private SoftwareSimulator model;
    // trace printing mode
    private int traceMode;
    // output directory
    private File outDir;
    // prefix for trace files;
    private String prefix;
    // prefix for trace files;
    private int suffix = 0;

    // creating a new model
    public SoftwareBLTLChecker(String name, String id) {
        this.name = name;
        this.errors = new ArrayList<>();
        this.origin = null;
        this.id = id;
        this.content = "[traces]\ntype=\"one per file\"\nfolder = \"\"\nprefix = \"\"\n\n[bltl]\ntrue";
        bltlRequirement = new BLTLRequirement(name, "", id);
    }

    // opening a saved model
    public SoftwareBLTLChecker(String name, File file, String id) throws PlasmaDataException {
        this.name = name;
        this.errors = new ArrayList<>();
        this.origin = file;
        this.id = id;
        try {
            this.content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PlasmaDataException("Cannot read model file", e);
        }
        bltlRequirement = new BLTLRequirement(name, "", id);
    }

    // importing model from file
    public SoftwareBLTLChecker(String name, String content, String id) {
        this.name = name;
        this.errors = new ArrayList<>();
        this.origin = null;
        this.id = id;
        this.content = content;
        bltlRequirement = new BLTLRequirement(name, "", id);
    }

    public List<VariableIdentifier> getVariableIdentifiers() {
        return variables;
    }

    /* ***** FROM AbstractRequirement *********************************************************************************/

    @Override
    public void setModel(AbstractModel abstractModel) {
        if (abstractModel instanceof SoftwareSimulator) {
            this.model = (SoftwareSimulator) abstractModel;
        } else {
            throw new RuntimeException("The \"BLTL with traces for software simulations\" checker works only with " +
                    "the \"Software simulator\"");
        }
        this.model.addGdbExpressions(this);
        bltlRequirement.setModel(abstractModel);
    }

    @Override
    public Double check(InterfaceState path) throws PlasmaCheckerException {
        Double res = bltlRequirement.check(path);
        if (res != 1.0 && traceMode > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("<trace>\n");
            for (InterfaceState interfaceState : model.getTrace()) {
                sb.append(interfaceState.toString());
            }
            sb.append("</trace>\n\n");
            switch (traceMode) {
                case 1:
                    System.out.print(sb.toString());
                    break;
                case 2:
                    try {
                        Files.write(Paths.get(outDir.getPath(), prefix + "_" + suffix),
                                ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + sb).getBytes(StandardCharsets.UTF_8));
                    } catch (IOException e) {
                        System.err.println("Cannot write trace : " + e.getMessage());
                    }
                    break;
                case 3:
                    try {
                        if (suffix == 0) {
                            Files.write(Paths.get(outDir.getPath(), prefix),
                                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<experiment>\n"
                                            .getBytes(StandardCharsets.UTF_8));
                        }
                        Files.write(Paths.get(outDir.getPath(), prefix), sb.toString().getBytes(StandardCharsets.UTF_8),
                                StandardOpenOption.APPEND, StandardOpenOption.CREATE);
                    } catch (IOException e) {
                        System.err.println("Cannot write trace : " + e.getMessage());
                    }
                    break;
            }
            suffix++;
        }
        return res;
    }

    @Override
    public Double check(int untilStep, InterfaceState path) throws PlasmaCheckerException {
        // trace saving not needed in this case (because used in Simulation panel)
        return bltlRequirement.check(untilStep, path);
    }

    @Override
    public Double check(String id, double untilValue, InterfaceState path) throws PlasmaCheckerException {
        // will throw an exception because BTLT doesn't support it currently
        return bltlRequirement.check(id, untilValue, path);
    }

    @Override
    public void clean() {
        super.clean();
        if (suffix > 0 && traceMode == 3) {
            try {
                Files.write(Paths.get(outDir.getPath(), prefix), "</experiment>\n".getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.APPEND);
            } catch (IOException e) {
                System.err.println("Cannot write trace : " + e.getMessage());
            }
        }
        suffix = 0;
    }

    @Override
    public List<InterfaceState> getLastTrace() {
        return this.model.getTrace();
    }

    @Override
    public List<Variable> getOptimizationVariables() {
        return bltlRequirement.getOptimizationVariables();
    }

    @Override
    public List<AbstractRequirement> generateInstantiatedRequirement() throws PlasmaDataException {
        return bltlRequirement.generateInstantiatedRequirement();
    }

    /* ***** FROM AbstractData ****************************************************************************************/

    // public String getName() --> same as super

    @Override
    public void rename(String newName) {
        super.rename(newName);
        bltlRequirement.rename(newName);
    }

    // public String getId() --> same as super

    // public String getContent() --> same as super

    // public boolean isEmpty() --> same as super

    // public File getOrigin() --> same as super

    // public String toString() --> same as super

    // public List<PlasmaDataException> getErrors() --> same as super

    // public void updateContent(String newContent) --> same as super

    // public void exportTo(File file) --> same as super

    @Override
    public List<AbstractFunction> getFunctions() {
        return bltlRequirement.getFunctions();
    }

    @Override
    public boolean checkForErrors() {
        errors.clear();
        variables.clear();

        String[] parts = content.split("\\r?\\n((\\[BLTL\\])|(\\[bltl\\]))\\r?\\n");
        if (parts.length < 2) {
            this.errors.add(new PlasmaDataException("Missing the '[BLTL]' option."));
            return true;
        }

        Toml toml;
        try {
            toml = new Toml().read(parts[0]);
        } catch (IllegalStateException e) {
            errors.add(new PlasmaDataException(e.getMessage()));
            return true;
        }

        String mode = Utils.getFromTOML(toml::getString, "traces.type");
        if (mode == null) {
            errors.add(new PlasmaDataException("The 'traces.type' option must be filled."));
            return true;
        }
        switch (mode) {
            case "none":
            case "NONE":
                traceMode = 0;
                break;
            case "print":
            case "PRINT":
                traceMode = 1;
                break;
            case "one per file":
            case "ONE PER FILE":
                traceMode = 2;
                break;
            case "all in one file":
            case "ALL IN ONE FILE":
                traceMode = 3;
                break;
            default:
                errors.add(new PlasmaDataException("Unknown 'traces.type' " +
                        "(available options are 'none', 'print', 'one per file', 'all in one file')."));
                return true;
        }

        if (traceMode == 2 || traceMode == 3) {
            String outFolder = Utils.getFromTOML(toml::getString, "traces.folder");
            if (outFolder == null) {
                errors.add(new PlasmaDataException("The 'traces.folder' option must be filled."));
                return true;
            }
            outDir = new File(outFolder);
            if (!outDir.isDirectory()) {
                errors.add(new PlasmaDataException("The 'traces.folder' option must be a directory."));
                return true;
            }

            prefix = Utils.getFromTOML(toml::getString, "traces.prefix");
            if (prefix == null || prefix.isEmpty()) {
                errors.add(new PlasmaDataException("The 'traces.prefix' option must be filled."));
                return true;
            }
        }

        String content2 = " " + parts[1] + " ";

        StringBuilder newContent = new StringBuilder();
        StringBuffer currentExpr = new StringBuffer();
        int nbId = 0;
        int state = 0;
        for (int i = 0; i < content2.length(); i++) {
            char c = content2.charAt(i);
            switch (state) {
                case 0:
                    if (c == '$') {
                        state = 1;
                    } else {
                        state = 0;
                        newContent.append(c);
                    }
                    break;
                case 1:
                    if (c == '$') {
                        this.errors.add(new PlasmaDataException("Invalid syntax at $$: expression must be non-empty."));
                        return true;
                    } else {
                        state = 2;
                        currentExpr = new StringBuffer();
                        currentExpr.append(c);
                    }
                    break;
                case 2:
                    if (c == '$') {
                        state = 3;
                    } else {
                        state = 2;
                        currentExpr.append(c);
                    }
                    break;
                case 3:
                    if (c == '$') {
                        state = 2;
                        currentExpr.append(c);
                    } else {
                        state = 0;
                        String varName = "__" + this.hashCode() + "_id_" + nbId;
                        nbId++;
                        variables.add(new VariableIdentifier(varName, currentExpr.toString()));
                        newContent.append(" ").append(varName).append(" ");
                        newContent.append(c);
                    }
                    break;
            }
        }

        if (state != 0) {
            this.errors.add(new PlasmaDataException("Invalid syntax: non balanced $."));
            return true;
        }

        bltlRequirement.updateContent(newContent.substring(1, newContent.length() - 1));

        boolean res = bltlRequirement.checkForErrors();
        this.errors.addAll(bltlRequirement.getErrors());
        return res;
    }

}
