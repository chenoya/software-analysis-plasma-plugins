package be.uclouvain.gdbsimulator.value;

public class IntValue implements Value<Integer> {
    private Integer content;

    @Override
    public Integer getValue() {
        return content;
    }

    @Override
    public void setValue(Integer newVal) {
        this.content = newVal;
    }

    @Override
    public Double toDouble() {
        return Double.valueOf(content);
    }

    public IntValue(Integer content) {
        this.content = content;
    }
}
