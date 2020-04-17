package be.uclouvain.softwarebltlchecker;

import java.io.File;

import fr.inria.plasmalab.workflow.data.AbstractRequirement;
import fr.inria.plasmalab.workflow.data.factory.AbstractRequirementFactory;
import fr.inria.plasmalab.workflow.exceptions.PlasmaDataException;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
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

    @Override
    public AbstractRequirement createAbstractRequirement(String name) {
        return new SoftwareBLTLChecker(name , "", id);
    }

    @Override
    public AbstractRequirement createAbstractRequirement(String name, File file) throws PlasmaDataException {
        return new SoftwareBLTLChecker(name, file, id);
    }

    @Override
    public AbstractRequirement createAbstractRequirement(String name, String content) {
        return new SoftwareBLTLChecker(name, content, id);
    }

}