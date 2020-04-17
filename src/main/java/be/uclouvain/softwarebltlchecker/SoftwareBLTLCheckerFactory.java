package be.uclouvain.softwarebltlchecker;

import fr.inria.plasmalab.workflow.data.AbstractRequirement;
import fr.inria.plasmalab.workflow.data.factory.AbstractRequirementFactory;
import fr.inria.plasmalab.workflow.exceptions.PlasmaDataException;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import java.io.File;

@PluginImplementation
@SuppressWarnings("unused")
public class SoftwareBLTLCheckerFactory extends AbstractRequirementFactory {

    private final static String id = "software-bltl-checker";

    @Override
    public String getName() {
        return "BLTL with traces for software simulations";
    }

    @Override
    public String getDescription() {
        return "Bounded-Linear Temporal Logic with traces for software simulations " +
                "(works only with software-simulator-plugin)";
    }

    @Override
    public String getId() {
        return id;
    }

    /**
     * Create an empty requirement.
     * This constructor is used when creating a new requirement.
     *
     * @param name the name of the requirement
     */
    @Override
    public AbstractRequirement createAbstractRequirement(String name) {
        return new SoftwareBLTLChecker(name, id);
    }

    /**
     * Create a requirement from a file.
     * This constructor is used when importing a requirement from a file.
     *
     * @param name the name of the requirement
     * @param file the file that contains the requirement
     * @throws PlasmaDataException if an error occurs while opening the file
     */
    @Override
    public AbstractRequirement createAbstractRequirement(String name, File file) throws PlasmaDataException {
        return new SoftwareBLTLChecker(name, file, id);
    }

    /**
     * Create a requirement from a content.
     * This constructor is used when opening a saved project.
     *
     * @param name    the name of the requirement
     * @param content the content of the requirement
     */
    @Override
    public AbstractRequirement createAbstractRequirement(String name, String content) {
        return new SoftwareBLTLChecker(name, content, id);
    }

}
