package be.uclouvain.swifigenerator;

import fr.inria.plasmalab.algorithm.data.SMCResult;
import fr.inria.plasmalab.workflow.data.AbstractRequirement;
import fr.inria.plasmalab.workflow.data.simulation.InterfaceIdentifier;
import fr.inria.plasmalab.workflow.exceptions.PlasmaExperimentException;
import fr.inria.plasmalab.workflow.shared.ResultIdentifier;

public class MyResult implements SMCResult {
	private static final ResultIdentifier probaId = new ResultIdentifier("Probability", false);
	private static final ResultIdentifier simId = new ResultIdentifier("#Simulations", false);

	private AbstractRequirement origin;
	private double probability;
	private int nbsimulations;
	
	public MyResult(AbstractRequirement req, double proba, int nbsims) {
		this.origin = req;
		this.probability = proba;
		this.nbsimulations = nbsims;
	}
	
	@Override
	public String getCategory() {
		return origin.getName();
	}

	@Override
	public InterfaceIdentifier[] getHeaders() {
		InterfaceIdentifier[] ret = new InterfaceIdentifier[2];
		ret[0] = probaId;
		ret[1] = simId;
		return ret;
	}

	@Override
	public Object getValueOf(String header) throws PlasmaExperimentException {
		if (header == probaId.getName())
			return probability;
		else if (header == simId.getName())
			return nbsimulations;
		else
			throw new PlasmaExperimentException("header " + header + " not found in MyResult.");
	}

	@Override
	public Object getValueOf(InterfaceIdentifier id) throws PlasmaExperimentException {
		if (id == probaId)
			return probability;
		else if (id == simId)
			return nbsimulations;
		else
			throw new PlasmaExperimentException("header ID " + id.getName() + " not found in MyResult.");
	}

	@Override
	public AbstractRequirement getOriginRequirement() {
		return origin;
	}

	@Override
	public double getPr() {
		return probability;
	}

	@Override
	public int getTotalCount() {
		return nbsimulations;
	}

}
