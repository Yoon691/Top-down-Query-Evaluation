package fr.univlyon1.mif37.dex.mapping;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by ecoquery on 20/05/2016.
 */
public class Variable implements Value {

    private String name;
    private static final ConcurrentMap<String, Variable> memo = new ConcurrentHashMap<>();


    public Variable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Variable variable = (Variable) o;

        return name != null ? name.equals(variable.name) : variable.name == null;

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return name;
    }

    public static Variable create(String name) {
        Variable c = memo.get(name);
        if (c != null) {
            return c;
        }
        // try creating it
        c = new Variable(name);
        Variable existing = memo.putIfAbsent(name, c);
        if (existing != null) {
            return existing;
        }
        return c;
    }
}
