package be.uclouvain.gdbsimulator;

import be.uclouvain.gdbmiapi.GdbException;
import be.uclouvain.gdbmiapi.GdbProcess;
import be.uclouvain.gdbmiapi.commands.DataManipulation;
import be.uclouvain.gdbsimulator.value.BoolValue;
import be.uclouvain.gdbsimulator.value.DoubleValue;
import be.uclouvain.gdbsimulator.value.NoValue;
import be.uclouvain.gdbsimulator.value.Value;
import fr.inria.plasmalab.workflow.data.simulation.InterfaceIdentifier;

import java.io.IOException;
import java.util.function.Function;

public class GdbExpression implements InterfaceIdentifier {

    private String name;
    private String expr;
    private Function<GdbProcess, Value<?>> parser;

    public GdbExpression(String name, String expr, Function<GdbProcess, Value<?>> parser) {
        this.name = name;
        this.expr = expr;
        this.parser = parser;
    }

    public static Function<GdbProcess, Value<?>> makeBoolParser(String expr) {
        return (g) -> {
            try {
                String s = DataManipulation.data_eval_expr(g, expr);
                if (s.equals("1"))
                    return new BoolValue(true);
                else if (s.equals("0"))
                    return new BoolValue(false);
                else
                    return new NoValue("Not a boolean expression");
            } catch (GdbException | IOException e) {
                System.err.println("Can not evaluate \"" + expr + "\" : " + e.getMessage());
                return new NoValue(e.getMessage());
            }
        };
    }

    public static Function<GdbProcess, Value<?>> makeDoubleParser(String expr) {
        return (g) -> {
            try {
                return new DoubleValue(Double.parseDouble(DataManipulation.data_eval_expr(g, expr)));
            } catch (NumberFormatException | GdbException | IOException e) {
                System.err.println("Can not evaluate \"" + expr + "\" : " + e.getMessage());
                return new NoValue(e.getMessage());
            }
        };
    }

    @Override
    public int compareTo(InterfaceIdentifier id) {
        return name.compareTo(id.getName());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isBoolean() {
        return false;
    }

    public Function<GdbProcess, Value<?>> getParser() {
        return parser;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    public String getExpr() {
        return expr;
    }
}
