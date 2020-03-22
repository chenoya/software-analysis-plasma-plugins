package be.uclouvain.gdbsimulator.value;

public class DoubleValue implements Value<Double> {
    private Double content;
    @Override
    public Double getValue() {
        return content;
    }

    @Override
    public void setValue(Double newVal) {
        this.content = newVal;
    }

    @Override
    public Double toDouble() {
        return content;
    }

    public DoubleValue(Double content) {
        this.content = content;
    }
}
