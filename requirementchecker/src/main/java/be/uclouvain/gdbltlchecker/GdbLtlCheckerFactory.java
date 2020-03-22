package be.uclouvain.gdbltlchecker;

import java.io.File;

import fr.inria.plasmalab.workflow.data.AbstractRequirement;
import fr.inria.plasmalab.workflow.data.factory.AbstractRequirementFactory;
import fr.inria.plasmalab.workflow.exceptions.PlasmaDataException;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
public class GdbLtlCheckerFactory extends AbstractRequirementFactory {
    private static final String name = "BLTL with traces for GDB", description = "Bounded-Linear Temporal Logic with traces for GDB simulator only";
    private static final String id = "bltl-traces-gdb";

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public AbstractRequirement createAbstractRequirement(String name) {
        return new GdbLtlChecker(name , "", id);
    }

    @Override
    public AbstractRequirement createAbstractRequirement(String name, File file) throws PlasmaDataException {
        return new GdbLtlChecker(name, file, id);
    }

    @Override
    public AbstractRequirement createAbstractRequirement(String name, String content) {
        return new GdbLtlChecker(name, content, id);
    }

}