package be.uclouvain.gdbsimulator.value;

public class NoValue implements Value<String> {
    String reason;

    @Override
    public String getValue() {
        return reason;
    }

    @Override
    public void setValue(String newVal) {
        reason = newVal;
    }

    @Override
    public Double toDouble() {
        return Double.NaN;
    }

    public NoValue(String reason) {
        this.reason = reason;
    }
}
