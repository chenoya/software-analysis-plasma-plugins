package be.uclouvain.softwaresimulator;

import fr.inria.plasmalab.workflow.data.AbstractModel;
import fr.inria.plasmalab.workflow.data.factory.AbstractModelFactory;
import fr.inria.plasmalab.workflow.exceptions.PlasmaDataException;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import java.io.File;

@PluginImplementation
public class SoftwareSimulatorFactory extends AbstractModelFactory {

    private final static String id = "Executable_simulator";

    @Override
    public String getName() {
        return "GDB Executable simulation";
    }

    @Override
    public String getDescription() {
        return "Simulator of binary executable with GDB";
    }

    @Override
    public String getId() {
        return id;
    }


    @Override
    public AbstractModel createAbstractModel(String name) {
        return new SoftwareSimulator(name, "", id);
    }

    @Override
    public AbstractModel createAbstractModel(String name, File file) throws PlasmaDataException {
        return new SoftwareSimulator(name, file, id);
    }

    @Override
    public AbstractModel createAbstractModel(String name, String content) {
        return new SoftwareSimulator(name, content, id);
    }


}