package be.uclouvain.softwaresimulator;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * An object that can simulate a program and extract information during the simulation.
 */
public abstract class Simulator {
    protected Map<String, Object> options;

    /**
     * @param options depending on the simulator
     * @throws IllegalArgumentException if the options are invalid
     */
    public Simulator(Map<String, Object> options) throws IllegalArgumentException {
        this.options = options;
    }

    /**
     * Start the simulation and wait before doing the first step in the function.
     * @param executable the name of the executable file
     * @param function   the name of the function to simulate
     * @throws IOException if something went wrong
     */
    public abstract void start(String executable, String function, boolean withCache) throws IOException;

    /**
     * Step one instruction in the function (staying inside it) and wait.
     *
     * @return false if we finished the function
     * @throws IOException if something went wrong
     */
    public abstract boolean nextInstruction() throws IOException;

    /**
     * Get the current program counter.
     *
     * @return the PC or empty if something went wrong
     */
    public abstract Optional<Long> getProgramCounter();

    /**
     * Get the current file line.
     *
     * @return the line or empty if something went wrong
     */
    public abstract Optional<Long> getFileLine();

    /**
     * Get the names of values defined by the simulator.
     *
     * @return the names (as VariableIdentifier)
     */
    public abstract List<VariableIdentifier> getSimulatorSpecificValuesNames();

    /**
     * Get the names and current values of values defined by the simulator.
     *
     * @return the names (as VariableIdentifier) and values (empty if something went wrong)
     */
    public abstract Map<VariableIdentifier, Optional<Double>> getSimulatorSpecificValues();

    /**
     * Return the boolean value of an expression.
     *
     * @param expression the expression understandable by the simulator
     * @return the value or empty if something went wrong
     */
    public abstract Optional<Boolean> evaluateBooleanExpression(String expression);

    /**
     * Return the double value of an expression.
     *
     * @param expression the expression understandable by the simulator
     * @return the value or empty if something went wrong
     */
    public abstract Optional<Double> evaluateDoubleExpression(String expression);

}
