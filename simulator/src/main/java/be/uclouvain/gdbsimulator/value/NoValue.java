package be.uclouvain.gdbsimulator.value;

public class NoValue implements Value<String> {
    @Override
    public String getValue() {
        return null;
    }

    @Override
    public void setValue(String newVal) {
    }

    @Override
    public Double toDouble() {
        return Double.NaN;
    }
}
