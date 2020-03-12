package be.uclouvain.gdbsimulator;

import fr.inria.plasmalab.workflow.data.simulation.InterfaceIdentifier;
import fr.inria.plasmalab.workflow.data.simulation.InterfaceState;
import fr.inria.plasmalab.workflow.data.simulation.InterfaceTransition;
import fr.inria.plasmalab.workflow.exceptions.PlasmaSimulatorException;

import java.util.HashMap;
import java.util.Map;

public class MyState implements InterfaceState {

	private Map<InterfaceIdentifier, Value> varContentII;
	private Map<String, Value> varContentStr;
	
	public MyState(Map<InterfaceIdentifier, Value> variablesContent) {
		this.varContentII = variablesContent;
		varContentStr = new HashMap<>();
		variablesContent.forEach((i, v) -> {
			varContentStr.put(i.getName(), v);
		});

		/*this.pc = pc;
		this.line = line;
		this.variablesContent = new HashMap<>(variablesContent);*/
	}
	  
	@Override
	public String getCategory() {
		return "MyState";
	}

	@Override
	public InterfaceIdentifier[] getHeaders() {
		return varContentII.keySet().toArray(new InterfaceIdentifier[0]);
	}

	@Override
	public InterfaceState cloneState() {
		return null;
		//TODO deep copy ?
		//return new MyState(varContentII);
	}

	@Override
	public InterfaceTransition getIncomingTransition() {
		return null;
	}

	@Override
    public Double getValueOf(InterfaceIdentifier id) throws PlasmaSimulatorException {
		if (varContentII.containsKey(id))
			return varContentII.get(id).toDouble();
		else
			throw new PlasmaSimulatorException("Unknown identifier: " + id.getName());
    }
     
    @Override
    public Double getValueOf(String id) throws PlasmaSimulatorException {
		if (varContentStr.containsKey(id))
			return varContentStr.get(id).toDouble();
		else
			throw new PlasmaSimulatorException("Unknown identifier: " + id);
    }

	@Override
    public void setValueOf(InterfaceIdentifier id, double value) throws PlasmaSimulatorException {
        throw new PlasmaSimulatorException("setValueOf not implemented");
    }

	@Override
	public String[] toStringArray() {
		return null;
	}

	@Override
	public Map<InterfaceIdentifier, Double> getValues() {
		Map<InterfaceIdentifier, Double> res = new HashMap<>(varContentII.size());
		varContentII.forEach((k,v) -> res.put(k, v.toDouble()));
		return res;
	}

}
