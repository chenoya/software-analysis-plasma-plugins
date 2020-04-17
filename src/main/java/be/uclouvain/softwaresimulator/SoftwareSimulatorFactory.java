package be.uclouvain.softwaresimulator;

import fr.inria.plasmalab.workflow.data.AbstractModel;
import fr.inria.plasmalab.workflow.data.factory.AbstractModelFactory;
import fr.inria.plasmalab.workflow.exceptions.PlasmaDataException;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import java.io.File;

@PluginImplementation
@SuppressWarnings("unused")
public class SoftwareSimulatorFactory extends AbstractModelFactory {

    private final static String id = "software-simulator";

    @Override
    public String getName() {
        return "Software simulator";
    }

    @Override
    public String getDescription() {
        return "Simulator of programs using debuggers";
    }

    @Override
    public String getId() {
        return id;
    }

    /**
     * Create an empty model.
     * This constructor is used when creating a new model.
     *
     * @param name the name of the model
     */
    @Override
    public AbstractModel createAbstractModel(String name) {
        return new SoftwareSimulator(name, id);
    }

    /**
     * Create a model from a file.
     * This constructor is used when importing a model from a file.
     *
     * @param name the name of the model
     * @param file the file that contains the model
     * @throws PlasmaDataException if an error occurs while opening the file
     */
    @Override
    public AbstractModel createAbstractModel(String name, File file) throws PlasmaDataException {
        return new SoftwareSimulator(name, file, id);
    }

    /**
     * Create a model from a content.
     * This constructor is used when opening a saved project.
     *
     * @param name    the name of the model
     * @param content the content of the model
     */
    @Override
    public AbstractModel createAbstractModel(String name, String content) {
        return new SoftwareSimulator(name, content, id);
    }

}
