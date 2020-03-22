package be.uclouvain.gdbsimulator.value;

public interface Value<T> {
    T getValue();
    void setValue(T newVal);
    Double toDouble();
}
