package gscript.factory.number;

import gscript.Factory;

import java.math.BigDecimal;

public final class GroovyNumberFactory {

    private final Factory factory;

    public GroovyNumberFactory(Factory factory) {
        this.factory = factory;
    }

    public BigDecimal parseDecimal(String str) {
        return new BigDecimal(str);
    }

    public BigDecimal parseDecimal(String str, int scale) {
        return new BigDecimal(str).setScale(scale, BigDecimal.ROUND_HALF_UP);
    }


}
