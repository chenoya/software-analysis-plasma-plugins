package be.uclouvain.gdbsimulator;

import fr.inria.plasmalab.workflow.data.simulation.InterfaceIdentifier;

public class GdbExpression implements InterfaceIdentifier {

	String name;

	public GdbExpression(String name) {
		this.name = name;
	}
	
	@Override
	public int compareTo(InterfaceIdentifier id) {
		return name.compareTo(id.getName());
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isBoolean() {
		return false;
	}

}
