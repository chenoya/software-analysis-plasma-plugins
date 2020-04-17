package be.uclouvain.swifigenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import be.uclouvain.softwaresimulator.SoftwareSimulator;
import fr.inria.plasmalab.algorithm.InterfaceAlgorithmScheduler;
import fr.inria.plasmalab.algorithm.InterfaceAlgorithmWorker;
import fr.inria.plasmalab.algorithm.data.SMCAlternatives;
import fr.inria.plasmalab.algorithm.data.SMCParameter;
import fr.inria.plasmalab.algorithm.factory.InterfaceAlgorithmFactory;
import fr.inria.plasmalab.workflow.data.AbstractModel;
import fr.inria.plasmalab.workflow.data.AbstractRequirement;
import fr.inria.plasmalab.workflow.exceptions.PlasmaParameterException;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
public class SwifiGeneratorFactory implements InterfaceAlgorithmFactory {

	@Override
	public String getId() {
		return "swifigenerator";
	}

	@Override
	public String getName() {
		return "SWIFI generator";
	}

	@Override
	public String getDescription() {
		return "SoftWare Implemented Fault Injection generator";
	}
	
	@Override
	public String toString(){
		return getName();
	}
	
	@Override
	public InterfaceAlgorithmScheduler createScheduler(AbstractModel model,
			List<AbstractRequirement> reqs,
			Map<String, Object> parametersMap) throws PlasmaParameterException {
		Object distributed = parametersMap.get("distributed");
		if (distributed instanceof Boolean && (Boolean)distributed) {
			throw new PlasmaParameterException("Distributed not available.");
		}
		if (!(model instanceof SoftwareSimulator))
			throw new PlasmaParameterException("This algorithm works only with a GDB simulator");
		/*int nbSims = 0;
		try {
			nbSims = Integer.parseInt(parametersMap.get("Nb Sims").toString());
		}
		catch(Exception e){
			throw new PlasmaParameterException(e);
		}
		if ( !(nbSims > 0) )
			throw new PlasmaParameterException("Nb Sims" + " must be > 0.");*/
		return new SwifiGenerator((SoftwareSimulator) model, reqs, getId());
	}

	@Override
	public InterfaceAlgorithmWorker createWorker(AbstractModel arg0, List<AbstractRequirement> arg1) {
		return null; // not distributed
	}

	@Override
	public List<SMCParameter> getParametersList() {
		List<SMCParameter> parameters = new ArrayList<SMCParameter>();
		parameters.add(new SMCParameter("Max simul", "Maximum nmber of simulations", false));
		SMCAlternatives z1b = new SMCAlternatives("Z1B", "Zero one byte", new ArrayList<>(), null);
		SMCAlternatives z1w = new SMCAlternatives("Z1W", "Zero one word", new ArrayList<>(), z1b);
		z1b.setNext(z1w);
		parameters.add(z1b); //only add head
		return parameters;
	}
	
	@Override
	public void fillParametersMap(Map<String, Object> parametersMap, String[] parameters) throws PlasmaParameterException {
		throw new PlasmaParameterException("Not implemented");
		/*try{
			if(parameters.length == 1)
				parametersMap.put("Nb Sims", Boolean.parseBoolean(parameters[0]));
			else
				throw new PlasmaParameterException("Not enough parameters for the SWIFI generator");
		} catch(NumberFormatException e){
			throw new PlasmaParameterException(e);
		}*/
	}

	@Override
	public Class<?> getResourceHandler() {
		return null; // not distributed
	}

	@Override
	public boolean isDistributed() {
		return false;
	}

}
