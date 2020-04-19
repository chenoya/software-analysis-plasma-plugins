package be.uclouvain.swifigenerator;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.function.Consumer;

public class OptionIter implements Iterable<String> {

    protected final String name;
    protected final long end;

    public OptionIter(String name, long end) {
       this.name = name;
       this.end = end;
    }

    @Override
    public Iterator<String> iterator() {
        return new Iterator<String>() {
            private long current;

            @Override
            public boolean hasNext() {
                return current < end;
            }

            @Override
            public String next() {
                if (current < end) {
                    return name + " " + current++;
                } else {
                    throw new NoSuchElementException();
                }
            }
        };
    }

    @Override
    public void forEach(Consumer<? super String> action) {
        for (int i = 0; i < end; i++) {
            action.accept(name + " " + i);
        }
    }

    @Override
    public Spliterator<String> spliterator() {
        throw new RuntimeException("Not implemented");
    }

    public long nbIter() {
        return end;
    }
}
