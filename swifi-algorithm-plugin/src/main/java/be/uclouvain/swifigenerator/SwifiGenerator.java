package be.uclouvain.swifigenerator;

import java.util.ArrayList;
import java.util.List;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import be.uclouvain.softwaresimulator.SoftwareSimulator;
import fr.inria.plasmalab.algorithm.AbstractAlgorithm;
import fr.inria.plasmalab.workflow.data.AbstractRequirement;
import fr.inria.plasmalab.workflow.data.simulation.InterfaceState;
import fr.inria.plasmalab.workflow.exceptions.PlasmaExperimentException;
import fr.inria.plasmalab.workflow.shared.ResultInterface;

public class SwifiGenerator extends AbstractAlgorithm {
	//final static Logger logger = LoggerFactory.getLogger(MyAlgorithm.class);

	private int nbSims;
	private SoftwareSimulator modelGdb;
	
	public SwifiGenerator(SoftwareSimulator model, List<AbstractRequirement> reqs, String id) {
		this.model = model;
		this.modelGdb = model;
		this.requirements = reqs;
		//this.nbSims = nbSims;
		this.nodeURI = id;
	}
	
	@Override
	public void run() {
		initializeAlgorithm();
		listener.notifyAlgorithmStarted(nodeURI);
		//logger.info("Starting " + nodeURI  + " with " + nbSims + " simulations.");

		String f = this.modelGdb.getExecutable();
		String func = this.modelGdb.getFunction();

		//GdbSimulator g = new GdbSimulator("", func);

		List<ResultInterface> results = new ArrayList<ResultInterface>(1);
		double result = 0.0;
		try {
			for (int i=1; i<= nbSims && !stopOrderReceived; i++) {
				InterfaceState path = model.newPath();
				double res = requirements.get(0).check(path);
				if (res > 0) {
					result += res; 
				}
			}
		}
		catch (PlasmaExperimentException e) {
			//logger.error(e.getMessage(),e);
			listener.notifyAlgorithmError(nodeURI, e.toString());
			errorOccured = true;
		}
		result /= nbSims;
		results.add(new MyResult(requirements.get(0), result, nbSims));
		
		if(!errorOccured){
			// Notify new results
			listener.publishResults(nodeURI, results);
			// Notify completed
			if(stopOrderReceived)
				listener.notifyAlgorithmStopped(nodeURI);
			else
				listener.notifyAlgorithmCompleted(nodeURI);
		}
	}
}
