package be.uclouvain.gdbplasmaplugin;

public class LongValue implements Value<Long> {
    private Long content;
    @Override
    public Long getValue() {
        return content;
    }

    @Override
    public void setValue(Long newVal) {
        this.content = newVal;
    }

    @Override
    public Double toDouble() {
        Double res = content.doubleValue();
        Long res2 = res.longValue();
        if (!res2.equals(content)) {
            throw new ArithmeticException("Loss of precision");
        }
        return res;
    }

    public LongValue(Long content) {
        this.content = content;
    }
}
