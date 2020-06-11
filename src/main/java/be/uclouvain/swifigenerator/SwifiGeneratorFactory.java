package be.uclouvain.swifigenerator;

import be.uclouvain.softwaresimulator.SoftwareSimulator;
import fr.inria.plasmalab.algorithm.InterfaceAlgorithmScheduler;
import fr.inria.plasmalab.algorithm.InterfaceAlgorithmWorker;
import fr.inria.plasmalab.algorithm.data.SMCParameter;
import fr.inria.plasmalab.algorithm.factory.InterfaceAlgorithmFactory;
import fr.inria.plasmalab.workflow.data.AbstractModel;
import fr.inria.plasmalab.workflow.data.AbstractRequirement;
import fr.inria.plasmalab.workflow.exceptions.PlasmaParameterException;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

@PluginImplementation
@SuppressWarnings("unused")
public class SwifiGeneratorFactory implements InterfaceAlgorithmFactory {

	private final static String id = "swifi-generator";
	private static ArrayList<SMCParameter> parameters;

	@Override
	public String toString(){
		return getName();
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
	public String getId() {
		return id;
	}

	@Override
	public boolean isDistributed() {
		//TODO
		return false;
	}

	@Override
	public List<SMCParameter> getParametersList() {
		if (parameters == null) {
			parameters = new ArrayList<>();
			parameters.add(new SMCParameter("Python path", "Path to Python 3", false));
			parameters.add(new SMCParameter("SWIFI path", "Path to the SWIFI tool python file", false));
			parameters.add(new SMCParameter("Max simul", "Maximum nmber of simulations", false));
			parameters.add(new SMCParameter("NOP", "Number of NOP used", false));
			parameters.add(new SMCParameter("Z1B", "Number of Z1B used", false));
			parameters.add(new SMCParameter("Z1W", "Number of Z1W used", false));
			parameters.add(new SMCParameter("FLP", "Number of FLP used", false));
			//parameters.add(new SMCParameter("JMP", "Number of JMP used", false));
			//parameters.add(new SMCParameter("JBE", "Number of JBE used", false));
			parameters.add(new SMCParameter("Other parameters", "Other options given to SWIFI", false));
		}
		return parameters;
	}

	@Override
	public void fillParametersMap(Map<String, Object> parametersMap, String[] parameters) throws PlasmaParameterException {
		try{
			if(parameters.length == 8) {
				parametersMap.put("Python path", parameters[0]);
				parametersMap.put("SWIFI path", parameters[1]);
				parametersMap.put("Max simul", parameters[2].length() == 0 ? Long.MAX_VALUE : Long.parseLong(parameters[2]));
				parametersMap.put("NOP", parameters[3].length() == 0 ? 0 : Integer.parseInt(parameters[3]));
				parametersMap.put("Z1B", parameters[4].length() == 0 ? 0 : Integer.parseInt(parameters[4]));
				parametersMap.put("Z1W", parameters[5].length() == 0 ? 0 : Integer.parseInt(parameters[5]));
				parametersMap.put("FLP", parameters[6].length() == 0 ? 0 : Integer.parseInt(parameters[6]));
				//parametersMap.put("JMP", parameters[6].length() == 0 ? 0 : Integer.parseInt(parameters[6]));
				//parametersMap.put("JBE", parameters[7].length() == 0 ? 0 : Integer.parseInt(parameters[7]));
				parametersMap.put("Other parameters", parameters[7]);
			} else {
				throw new PlasmaParameterException("Wrong number of parameters for the " + getName() + " algorithm.");
			}
		} catch (NumberFormatException e){
			throw new PlasmaParameterException("Cannot parse parameters: " +  e.getMessage());
		}
	}

	@Override
	public InterfaceAlgorithmWorker createWorker(AbstractModel arg0, List<AbstractRequirement> arg1) {
		//TODO
		return null;
	}

	private <T> T parseParameter(Map<String, Object> map, String name, Function<String, T> parse, Predicate<T> cond) throws PlasmaParameterException {
		Object o = map.get(name);
		if (o instanceof String) {
			try {
				T res = parse.apply((String) o);
				if (res == null || !cond.test(res))
					throw new PlasmaParameterException("Invalid option format : " + name);
				return res;
			} catch (Exception e) {
				throw new PlasmaParameterException("Invalid option format : " + name);
			}
		} else {
			throw new PlasmaParameterException("Option not found or incorrect type : " + name);
		}
	}

	@Override
	public InterfaceAlgorithmScheduler createScheduler(AbstractModel model, List<AbstractRequirement> reqs, Map<String, Object> parametersMap) throws PlasmaParameterException {

		boolean distributed;
		Object o = parametersMap.get("distributed");
		if (o instanceof Boolean)
			distributed = (boolean) o;
		else
			throw new PlasmaParameterException("Invalid option format : distributed");

		AlgorithmOptions algorithmOptions = new AlgorithmOptions(
				parseParameter(parametersMap, "Python path", (s) -> s, (s) -> true),
				parseParameter(parametersMap, "SWIFI path", (s) -> s, (s) -> true),
				parseParameter(parametersMap, "Max simul", (s) -> s.isEmpty() ? Long.MAX_VALUE : Long.parseLong(s), (i) -> i >= 0),
				parseParameter(parametersMap, "NOP", (s) -> s.isEmpty() ? 0 : Integer.parseInt(s), (i) -> i >= 0),
				parseParameter(parametersMap, "Z1B", (s) -> s.isEmpty() ? 0 : Integer.parseInt(s), (i) -> i >= 0),
				parseParameter(parametersMap, "Z1W", (s) -> s.isEmpty() ? 0 : Integer.parseInt(s), (i) -> i >= 0),
				parseParameter(parametersMap, "FLP", (s) -> s.isEmpty() ? 0 : Integer.parseInt(s), (i) -> i >= 0),
				//parseParameter(parametersMap, "JMP", (s) -> s.isEmpty() ? 0 : Integer.parseInt(s), (i) -> i >= 0),
				//parseParameter(parametersMap, "JBE", (s) -> s.isEmpty() ? 0 : Integer.parseInt(s), (i) -> i >= 0),
				parseParameter(parametersMap, "Other parameters", (s) -> s, (s) -> true)
		);

		if (!(model instanceof SoftwareSimulator))
			throw new PlasmaParameterException("This algorithm works only with a 'software-simulator' model.");

		if (distributed)
			//TODO
			return null;
		else
			return new SwifiGenerator((SoftwareSimulator) model, reqs, algorithmOptions, getId());
	}

	@Override
	public Class<?> getResourceHandler() {
		//TODO
		return null;
	}

}
