package be.uclouvain.softwaresimulator;

import fr.inria.plasmalab.workflow.data.simulation.InterfaceIdentifier;
import fr.inria.plasmalab.workflow.data.simulation.InterfaceState;
import fr.inria.plasmalab.workflow.data.simulation.InterfaceTransition;
import fr.inria.plasmalab.workflow.exceptions.PlasmaSimulatorException;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;

public class SoftwareState implements InterfaceState {

    private final Map<VariableIdentifier, Double> varContentII;
    private final Map<String, Double> varContentStr;

    public SoftwareState(Map<VariableIdentifier, Double> variablesContent) {
        this.varContentII = variablesContent;
        varContentStr = new HashMap<>();
        variablesContent.forEach((i, v) -> varContentStr.put(i.getName(), v));
    }

    /* ***** FROM InterfaceState **************************************************************************************/

    @Override
    @Deprecated
    public String[] toStringArray() {
        return null;
    }

    @Override
    public InterfaceTransition getIncomingTransition() {
        return null;
    }

    @Override
    public Double getValueOf(InterfaceIdentifier id) throws PlasmaSimulatorException {
        if (id instanceof VariableIdentifier && varContentII.containsKey(id))
            return varContentII.get(id);
        else if (id == null)
            throw new PlasmaSimulatorException("Unknown identifier: null");
        else
            throw new PlasmaSimulatorException("Unknown identifier: " + id.getName());
    }

    @Override
    public Double getValueOf(String id) throws PlasmaSimulatorException {
        if (varContentStr.containsKey(id))
            return varContentStr.get(id);
        else if (id == null)
            throw new PlasmaSimulatorException("Unknown identifier: null");
        else
            throw new PlasmaSimulatorException("Unknown identifier: " + id);
    }

    @Override
    public Map<InterfaceIdentifier, Double> getValues() {
        Map<InterfaceIdentifier, Double> res = new HashMap<>(varContentII.size());
        res.putAll(varContentII);
        return res;
    }

    @Override
    public void setValueOf(InterfaceIdentifier id, double value) throws PlasmaSimulatorException {
        throw new PlasmaSimulatorException("Set of values is not implemented");
    }

    @Override
    public InterfaceState cloneState() {
        return null;
    }

    /* ***** FROM ResultInterface *************************************************************************************/

    @Override
    public InterfaceIdentifier[] getHeaders() {
        return varContentII.keySet().toArray(new InterfaceIdentifier[0]);
    }

    @Override
    public String getCategory() {
        return "SoftwareState";
    }

    /* ***** FROM Object **********************************************************************************************/

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("<state>\n");
        varContentII.forEach((i, v) -> s.append("    <expr name=\"")
                .append(StringEscapeUtils.escapeXml10(i.getName()))
                .append("\" expr=\"")
                .append(StringEscapeUtils.escapeXml10(i.getExpr()))
                .append("\">")
                .append(v)
                .append("</expr>\n"));
        s.append("</state>\n");
        return s.toString();
    }

}
