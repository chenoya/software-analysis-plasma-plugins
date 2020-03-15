package be.uclouvain.gdbsimulator;

import fr.inria.plasmalab.workflow.data.simulation.InterfaceIdentifier;

public class GdbExpression implements InterfaceIdentifier {

	String name;
	String expr;

	public GdbExpression(String name, String expr) {
		this.name = name;
		this.expr = expr;
	}
	
	@Override
	public int compareTo(InterfaceIdentifier id) {
		return name.compareTo(id.getName());
	}

	@Override
	public String getName() {
		return name;
	}

	public String getExpr() {
		return expr;
	}

	@Override
	public boolean isBoolean() {
		return false;
	}

}
