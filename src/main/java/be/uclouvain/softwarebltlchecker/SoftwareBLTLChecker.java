package be.uclouvain.softwarebltlchecker;

import fr.inria.plasmalab.bltl.BLTLRequirement;
import fr.inria.plasmalab.workflow.data.AbstractModel;
import fr.inria.plasmalab.workflow.data.AbstractRequirement;
import fr.inria.plasmalab.workflow.data.simulation.InterfaceState;
import fr.inria.plasmalab.workflow.exceptions.PlasmaCheckerException;
import fr.inria.plasmalab.workflow.exceptions.PlasmaDataException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SoftwareBLTLChecker extends AbstractRequirement {

    private Map<String, String> variables = new HashMap<>();
    private boolean parsed = false;
    private BLTLRequirement bltlRequirement;
    private AbstractModel model;

    public SoftwareBLTLChecker(String name, File file, String id) throws PlasmaDataException {
        this.name = name;
        this.id = id;
        try {
            this.content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PlasmaDataException(e);
        }
        this.origin = file;
        this.errors = new ArrayList<>(0);

        bltlRequirement = new BLTLRequirement(name, "", id);
    }

    public SoftwareBLTLChecker(String name, String content, String id) {
        this.name = name;
        this.id = id;
        this.content = content;
        this.origin = null;
        this.errors = new ArrayList<>(0);

        bltlRequirement = new BLTLRequirement(name, "", id);
    }

    private void addExpressionsToModel() {
        try {
            Method method = this.model.getClass().getMethod("addGdbExpressions", Map.class);
            method.invoke(model, variables);
        } catch (SecurityException | NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("The \"BLTL with traces for software simulations\" checker works only with the \"Software simulator\"");
        }
    }

    @Override
    public void setModel(AbstractModel abstractModel) {
        this.model = abstractModel;
        addExpressionsToModel();
        bltlRequirement.setModel(abstractModel);
        if (variables.size() != 0 && abstractModel.getTraceLength() > 0) {
            System.err.println("The BLTL properties containing $expressions$ cannot be used in the simulation panel (you can use variables in the simulator's configuration instead).");
            // because the model is started before we can add any checker defined expression to it
        }
    }

    @Override
    public Double check(InterfaceState path) throws PlasmaCheckerException {
        Double res = bltlRequirement.check(path);
        if (res != 1.0) {
            // print trace
            for (InterfaceState interfaceState : model.getTrace()) {
                System.out.println(interfaceState);
                System.out.println("----------");
            }
        }
        return res;
    }

    @Override
    public Double check(int i, InterfaceState interfaceState) throws PlasmaCheckerException {
        return bltlRequirement.check(i, interfaceState);
    }

    @Override
    public Double check(String s, double v, InterfaceState interfaceState) throws PlasmaCheckerException {
        return bltlRequirement.check(s, v, interfaceState);
    }

    @Override
    public List<InterfaceState> getLastTrace() {
        return bltlRequirement.getLastTrace();
    }

    @Override
    public boolean checkForErrors() {
        if (!parsed) {
            errors.clear();
            variables.clear();
            int nbId = 0;
            /*String[] parts = this.content.split("\n", 2);
            if (parts.length != 2) {
                this.errors.add(new PlasmaDataException("Must have a line to configure the trace saving then the BLTL"));
                return true;
            }
            if (parts[1].)

            String content2 = " " + parts[1] + " ";*/

            String content2 = " " + content + " ";

            StringBuilder newContent = new StringBuilder();
            StringBuffer currentExpr = new StringBuffer();
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
                            this.errors.add(new PlasmaDataException("Invalid syntax at $$ : expression must be non-empty."));
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
                            String varName = "__id_" + nbId;
                            nbId++;
                            variables.put(varName, currentExpr.toString());
                            newContent.append(" ").append(varName).append(" ");
                            newContent.append(c);
                        }
                        break;
                }
            }

            if (state != 0) {
                this.errors.add(new PlasmaDataException("Invalid syntax : non balanced $."));
                return true;
            }

            bltlRequirement.updateContent(newContent.substring(1, newContent.length()-1));

            this.parsed = true;
        }
        boolean res = bltlRequirement.checkForErrors();
        this.errors.addAll(bltlRequirement.getErrors());
        return res;
    }

    @Override
    public void updateContent(String newContent) {
        this.parsed = false;
        this.content = newContent;
        bltlRequirement.updateContent("");
    }

    /*@Override
    public List<Variable> getOptimizationVariables() {
        return super.getOptimizationVariables();
    }

    @Override
    public List<AbstractRequirement> generateInstantiatedRequirement() throws PlasmaDataException {
        return super.generateInstantiatedRequirement();
    }

    @Override
    public List<AbstractFunction> getFunctions() {
        return super.getFunctions();
    }*/

}
