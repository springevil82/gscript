package gscript.factory.random;

import gscript.Factory;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.concurrent.ThreadLocalRandom;

public final class GroovyRandomFactory {

    private final Factory factory;
    private final SecureRandom secureRandom;

    public GroovyRandomFactory(Factory factory) {
        this.factory = factory;
        this.secureRandom = new SecureRandom();
    }

    /**
     * Сгенерить случайное число в диапазоне min ... max
     */
    public int randomInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    /**
     * Сгенерить случайное число с плавающей точкий в диапазоне min ... max
     */
    public double randomDouble(int min, int max) {
        return min + Math.random() * (max - min);
    }

    /**
     * Сгенерить слуйную строку длинны length
     *
     * @param length длина строки
     */
    public String randomString(int length) {
        return new BigInteger(130, secureRandom).toString(length);
    }

}
