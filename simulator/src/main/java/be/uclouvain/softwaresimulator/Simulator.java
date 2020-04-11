package be.uclouvain.softwaresimulator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class Simulator {
    protected Path sourceFile;
    protected String functionName;

    public Simulator(Path sourceFile, String functionName) throws IOException {
        this.sourceFile = sourceFile;
        this.functionName = functionName;
    }

    public abstract void start() throws IOException;

    public abstract boolean nextInstruction() throws IOException;

    public abstract Optional<Long> getProgramCounter();

    public abstract Optional<Long> getFileLine();

    public abstract List<VariableIdentifier> getSimulatorSpecificValuesNames();

    public abstract Map<VariableIdentifier, Optional<Double>> getSimulatorSpecificValues();

    public abstract Optional<Boolean> evaluateBooleanExpression(String expression);

    public abstract Optional<Double> evaluateDoubleExpression(String expression);
}
