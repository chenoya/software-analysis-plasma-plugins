package be.uclouvain.softwaresimulator;

import fr.inria.plasmalab.workflow.data.simulation.InterfaceIdentifier;

public class VariableIdentifier implements InterfaceIdentifier {

    private String name;
    private String expr;

    public VariableIdentifier(String name, String expr) {
        this.name = name;
        this.expr = expr;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isBoolean() {
        return false;
    }

    @Override
    public int compareTo(InterfaceIdentifier o) {
        return this.getName().compareTo(o.getName());
    }

    public String getExpr() {
        return expr;
    }
}
