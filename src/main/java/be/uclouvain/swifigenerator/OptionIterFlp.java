package be.uclouvain.swifigenerator;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.function.Consumer;

public class OptionIterFlp extends OptionIter {

    public OptionIterFlp(String name, long end) {
        super(name, end);
    }

    @Override
    public Iterator<String> iterator() {
        return new Iterator<String>() {
            private long current;
            private int offset;

            @Override
            public boolean hasNext() {
                return !(current == end);
            }

            @Override
            public String next() {
                if (!(current == end)) {
                    String res = name + " " + current + " " + offset;
                    if (offset == 7)
                        current++;
                    offset = (offset + 1) % 8;
                    return res;
                } else {
                    throw new NoSuchElementException();
                }
            }
        };
    }

    @Override
    public void forEach(Consumer<? super String> action) {
        for (int i = 0; i < end; i++) {
            for (int j = 0; j < 8; j++) {
                action.accept(name + " " + i + " " + j);
            }
        }
    }

    @Override
    public Spliterator<String> spliterator() {
        throw new RuntimeException("Not implemented");
    }

    public long nbIter() {
        return 8 * end;
    }
}
