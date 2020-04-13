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

    private Map<String, String> gdbExpr = new HashMap<>();
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

    private void addExpressions(AbstractModel model) {
        try {
            Method method = model.getClass().getMethod("addGdbExpressions", Map.class);
            method.invoke(model, gdbExpr);
        } catch (SecurityException | NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("The \"BLTL with traces for GDB\" checker works only with the \"GDB Executable simulation\" simulator");
        }
    }

    @Override
    public void setModel(AbstractModel abstractModel) {
        addExpressions(abstractModel);
        this.model = abstractModel;
        bltlRequirement.setModel(abstractModel);
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
            gdbExpr.clear();
            int nbId = 0;
            String content2 = " " + this.content + " ";
            String[] parts = content2.split("\\$");
            if ((parts.length - 1) % 2 != 0) {
                errors.add(new PlasmaDataException("Unbalanced GdbExpression bounds"));
                return true;
            }
            StringBuilder newContent = new StringBuilder();
            for (int i = 0; i < parts.length; i++) {
                if (i % 2 == 0) {
                    newContent.append(parts[i]);
                } else {
                    gdbExpr.put("id_" + nbId, parts[i]);
                    newContent.append("id_").append(nbId);
                    nbId++;
                }
            }
            bltlRequirement.updateContent(newContent.substring(1, newContent.length()-1));
            if (this.model != null) {
                addExpressions(this.model);
            }
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
