package be.uclouvain.swifigenerator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import be.uclouvain.softwaresimulator.SoftwareSimulator;
import fr.inria.plasmalab.algorithm.AbstractAlgorithm;
import fr.inria.plasmalab.workflow.data.AbstractRequirement;
import fr.inria.plasmalab.workflow.data.simulation.InterfaceState;
import fr.inria.plasmalab.workflow.exceptions.PlasmaCheckerException;
import fr.inria.plasmalab.workflow.exceptions.PlasmaSimulatorException;
import fr.inria.plasmalab.workflow.shared.ResultInterface;

public class SwifiGenerator extends AbstractAlgorithm {

	private final SoftwareSimulator modelSoftware;
	private final AlgorithmOptions algorithmOptions;
	private String oldExecutable;
	private long nbIterEsti;
	private long startTime;

	public SwifiGenerator(SoftwareSimulator model, List<AbstractRequirement> reqs, AlgorithmOptions algorithmOptions, String id) {
		this.model = model;
		this.modelSoftware = model;
		this.requirements = reqs;
		this.nodeURI = id;
		this.algorithmOptions = algorithmOptions;
	}

	private long nbIter = 0;
	private double[] results;


	private void combination(List<OptionIter> options, int level, List<String> selectedOption) {
		if (stopOrderReceived) {
			return; //abort
		}
		if (nbIter >= algorithmOptions.getNbMaxSimu()) {
			return; //max number of simulation reached
		}
		if (level >= options.size()) {
			simulatePath(selectedOption);
			return;
		}
		for (String option : options.get(level)) {
			ArrayList<String> newSelectedOptions = new ArrayList<>(selectedOption);
			newSelectedOptions.add(option);
			combination(options, level + 1, newSelectedOptions);
		}
	}

	private void simulatePath(List<String> selectedOption) {
		try {
			File file = File.createTempFile("exec", null);
			file.deleteOnExit();

			String modifiedExecutable = file.getAbsolutePath();
			modelSoftware.setExecutable(modifiedExecutable);

			Runtime rt = Runtime.getRuntime();
			List<String> params = new ArrayList<>();
			params.add(algorithmOptions.getCmdSwifi()); // "C:\Users\Antoine\AppData\Local\Programs\Python\Python36\python.exe" "C:\Users\Antoine\Downloads\TFE\swifi-tool\swifitool\faults_inject.py"
			params.add("-i");
			params.add(oldExecutable);
			params.add("-o");
			params.add(modifiedExecutable);
			params.addAll(Arrays.asList(algorithmOptions.getOtherParams().split(" ")));
			for (String o : selectedOption) {
				String[] parts = o.split(" ");
				params.addAll(Arrays.asList(parts));
			}
			Process pr = rt.exec(params.toArray(new String[0]));
			pr.waitFor();

			ByteArrayOutputStream buf = new ByteArrayOutputStream();
			while (pr.getErrorStream().available() > 0) {
				buf.write((byte) pr.getErrorStream().read());
			}
			String pythonErrorMsg = buf.toString(StandardCharsets.UTF_8.name());
			if (!pythonErrorMsg.isEmpty()) {
				throw new IllegalArgumentException(pythonErrorMsg);
			}

			InterfaceState path = model.newPath();
			for (int i = 0; i < requirements.size(); i++) {
				double res = requirements.get(i).check(path);
				if (res > 0) {
					results[i] += res;
				}
			}
			nbIter++;
			notifyResults();
			notifyProgress();

		} catch (IOException | InterruptedException | IllegalArgumentException e) {
			listener.notifyAlgorithmError(nodeURI, e.getMessage());
			throw new RuntimeException(e);
		} catch (PlasmaSimulatorException | PlasmaCheckerException e) {
			System.out.println(Arrays.toString(selectedOption.toArray()));
			System.out.println(e.getMessage());
			// assume res = 0 for all checkers so just update nbIter
			nbIter++;
			notifyResults();
			notifyProgress();
		}
	}

	@Override
	public void run() {
		initializeAlgorithm();
		listener.notifyAlgorithmStarted(nodeURI);

		oldExecutable = modelSoftware.getExecutable();
		modelSoftware.setWithCache(false);

		File f = new File(oldExecutable);
		long fileSize = f.length();

		List<OptionIter> options = new ArrayList<>();
		for (int i = 0; i < algorithmOptions.getNop(); i++) {
			if (algorithmOptions.getOtherParams().contains("ARM") || algorithmOptions.getOtherParams().contains("arm")) {
				options.add(new OptionIter("NOP", fileSize - 1));
			} else {
				options.add(new OptionIter("NOP", fileSize));
			}
		}
		for (int i = 0; i < algorithmOptions.getZ1b(); i++) {
			options.add(new OptionIter("Z1B", fileSize));
		}
		for (int i = 0; i < algorithmOptions.getZ1w(); i++) {
			try {
				int word = Integer.parseInt(algorithmOptions.getOtherParams().split("-w ?")[1].split(" ")[0]);
				options.add(new OptionIter("Z1W", fileSize - word + 1));
			} catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
				options.add(new OptionIter("Z1W", fileSize));
			}
		}
		for (int i = 0; i < algorithmOptions.getFlp(); i++) {
			options.add(new OptionIterFlp("FLP", fileSize));
		}
		//TODO
		/*for (int i = 0; i < algorithmOptions.getJmp(); i++) {
			options.add(new OptionIter("JMP", fileSize));
		}
		for (int i = 0; i < algorithmOptions.getJbe(); i++) {
			options.add(new OptionIter("JBE", fileSize));
		}*/

		nbIter = 0;
		results = new double[requirements.size()];
		nbIterEsti = 1;
		for (OptionIter i : options) {
			nbIterEsti *= i.nbIter();
		}
		nbIterEsti = Math.min(nbIterEsti, algorithmOptions.getNbMaxSimu());
		startTime = System.currentTimeMillis();
		combination(options, 0, new ArrayList<>());

		if(!errorOccured){
			// Notify new results
			notifyResults();
			// Notify completed
			if(stopOrderReceived)
				listener.notifyAlgorithmStopped(nodeURI);
			else
				listener.notifyAlgorithmCompleted(nodeURI);
		}
	}

	private void notifyProgress() {
		double p = (double) nbIter / (double) nbIterEsti;
		listener.notifyProgress((int) (100*p));
		listener.notifyTimeRemaining((long) ((System.currentTimeMillis()-startTime)/p-(System.currentTimeMillis()-startTime)));
	}

	private void notifyResults() {
		List<ResultInterface> resultList = new ArrayList<>(requirements.size());
		for (int i = 0; i < requirements.size(); i++) {
			resultList.add(new MyResult(requirements.get(i), results[i] / nbIter, (int) nbIter));
		}
		listener.publishResults(nodeURI, resultList);
	}
}
