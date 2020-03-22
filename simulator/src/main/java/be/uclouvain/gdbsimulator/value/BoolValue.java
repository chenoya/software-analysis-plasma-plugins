package be.uclouvain.gdbsimulator.value;

public class BoolValue implements Value<Boolean> {
    private Boolean content;

    @Override
    public Boolean getValue() {
        return content;
    }

    @Override
    public void setValue(Boolean newVal) {
        this.content = newVal;
    }

    @Override
    public Double toDouble() {
        return content ? 1.0 : 0.0;
    }

    public BoolValue(Boolean content) {
        this.content = content;
    }
}
