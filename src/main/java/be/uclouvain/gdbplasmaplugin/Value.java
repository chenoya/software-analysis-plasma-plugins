package be.uclouvain.gdbplasmaplugin;

public interface Value<T> {
    T getValue();
    void setValue(T newVal);
    Double toDouble();
}
