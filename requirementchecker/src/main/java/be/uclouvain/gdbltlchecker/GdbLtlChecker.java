package be.uclouvain.gdbltlchecker;

import fr.inria.plasmalab.bltl.BLTLRequirement;
import fr.inria.plasmalab.workflow.data.AbstractModel;
import fr.inria.plasmalab.workflow.data.simulation.InterfaceState;
import fr.inria.plasmalab.workflow.exceptions.PlasmaCheckerException;
import fr.inria.plasmalab.workflow.exceptions.PlasmaDataException;
import fr.inria.plasmalab.workflow.exceptions.PlasmaSimulatorException;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class GdbLtlChecker extends BLTLRequirement {

    private Map<String, String> gdbExpr = new HashMap<>();
    private boolean parsed = false;

    public GdbLtlChecker(String name, File file, String id) throws PlasmaDataException {
        super(name, file, id);
    }

    public GdbLtlChecker(String name, String content, String id) {
        super(name, content, id);
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
        super.setModel(abstractModel);
    }

    @Override
    public Double check(InterfaceState path) throws PlasmaCheckerException {
        Double res = super.check(path);
        if (res != 1.0) {
            // print trace
            for (InterfaceState interfaceState : model.getTrace()) {
                for (String interfaceIdentifier : model.getIdentifiers().keySet()) {
                    try {
                        System.out.println(interfaceIdentifier + " = " + interfaceState.getValueOf(interfaceIdentifier));
                    } catch (PlasmaSimulatorException e) {
                        System.out.println(interfaceIdentifier + " = " + e.getMessage());
                    }
                }
                System.out.println("----------");
            }
        }
        return res;
    }

    /*@Override
    public Double check(int i, InterfaceState interfaceState) throws PlasmaCheckerException {
        return super.check(i, interfaceState);
    }

    @Override
    public Double check(String s, double v, InterfaceState interfaceState) throws PlasmaCheckerException {
        return super.check(s, v, interfaceState);
    }

    @Override
    public List<InterfaceState> getLastTrace() {
        return super.getLastTrace();
    }*/

    @Override
    public boolean checkForErrors() {
        if (!parsed) {
            errors.clear();
            gdbExpr.clear();
            int nbId = 0;
            content = " " + content + " ";
            String[] parts = content.split("\\$");
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
            this.content = newContent.toString();
            this.content = this.content.substring(1, this.content.length()-1);
            this.parsed = true;
            if (this.model != null) {
                addExpressions(this.model);
            }

        }
        return super.checkForErrors();
    }

    @Override
    public void updateContent(String newContent) {
        this.parsed = false;
        super.updateContent(newContent);
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
