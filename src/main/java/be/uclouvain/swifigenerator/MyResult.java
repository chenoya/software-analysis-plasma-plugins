package be.uclouvain.swifigenerator;

import fr.inria.plasmalab.algorithm.data.SMCResult;
import fr.inria.plasmalab.workflow.data.AbstractRequirement;
import fr.inria.plasmalab.workflow.data.simulation.InterfaceIdentifier;
import fr.inria.plasmalab.workflow.exceptions.PlasmaExperimentException;
import fr.inria.plasmalab.workflow.shared.ResultIdentifier;

public class MyResult implements SMCResult {
	private static final ResultIdentifier nbSimId = new ResultIdentifier("#Simulations", false);
	private static final ResultIdentifier positiveId = new ResultIdentifier("#Validated", false);
	private static final ResultIdentifier crashedId = new ResultIdentifier("#Crashed/Could not retrieve", false);
	private static final ResultIdentifier negativeId = new ResultIdentifier("#Invalidated", false);
	//private static final ResultIdentifier probaId = new ResultIdentifier("Probability", false);

	private AbstractRequirement origin;
	//private double probability;
	private int nbSimu;
	private int positiveSimu;
	private int crashedSimu;
	private int negativeSimu;

	public MyResult(AbstractRequirement req, int nbSimu, int positiveSimu, int crashedSimu, int negativeSimu) {
		this.origin = req;
		this.nbSimu = nbSimu;
		this.positiveSimu = positiveSimu;
		this.crashedSimu = crashedSimu;
		this.negativeSimu = negativeSimu;
	}
	
	@Override
	public String getCategory() {
		return origin.getName();
	}

	@Override
	public InterfaceIdentifier[] getHeaders() {
		InterfaceIdentifier[] ret = new InterfaceIdentifier[4];
		ret[0] = nbSimId;
		ret[1] = positiveId;
		ret[2] = crashedId;
		ret[3] = negativeId;
		//ret[4] = probaId;
		return ret;
	}

	@Override
	public Object getValueOf(String header) throws PlasmaExperimentException {
		if (header.equals(nbSimId.getName()))
			return nbSimu;
		else if (header.equals(positiveId.getName()))
			return positiveSimu;
		else if (header.equals(crashedId.getName()))
			return crashedSimu;
		else if (header.equals(negativeId.getName()))
			return negativeSimu;
		else
			throw new PlasmaExperimentException("header " + header + " not found in MyResult.");
	}

	@Override
	public Object getValueOf(InterfaceIdentifier id) throws PlasmaExperimentException {
		if (id == nbSimId)
			return nbSimu;
		else if (id == positiveId)
			return positiveSimu;
		else if (id == crashedId)
			return crashedSimu;
		else if (id == negativeId)
			return negativeSimu;
		else
			throw new PlasmaExperimentException("header ID " + id.getName() + " not found in MyResult.");
	}

	@Override
	public AbstractRequirement getOriginRequirement() {
		return origin;
	}

	@Override
	public double getPr() {
		return (positiveSimu + crashedSimu) / (double) nbSimu;
	}

	@Override
	public int getTotalCount() {
		return nbSimu;
	}

}
