package be.uclouvain.gdbsimulator;

public interface Value<T> {
    T getValue();
    void setValue(T newVal);
    Double toDouble();
}
