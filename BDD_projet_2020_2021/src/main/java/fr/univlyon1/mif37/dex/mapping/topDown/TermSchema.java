package fr.univlyon1.mif37.dex.mapping.topDown;
import fr.univlyon1.mif37.dex.mapping.Value;
import fr.univlyon1.mif37.dex.mapping.Variable;

import java.util.ArrayList;
import java.util.List;
/**
 * @juba BDD
 */
/**
 * A list of terms of fixed arity representing the attribute schema for a
 * relation of the same arity (i.e., the 2nd term in the list is the attribute
 * for the 2nd "column" in the relation).
 *
 */
public class TermSchema {
    /**
     * The attributes of this schema.
     */
    public final List<Value> attributes;

    /**
     * Constructs a schema from a list of terms.
     *
     * @param terms
     *            the terms
     */
    public TermSchema(List<Value> terms) {
        this.attributes = terms;
    }

    /**
     * Constructs a schema from another schema.
     *
     * @param other
     *            the other schema
     */
    public TermSchema(TermSchema other) {
        this.attributes = new ArrayList<>(other.attributes);
    }

    /**
     * Constructs a schema of the supplied arity. The attributes are given
     * unique but arbitrary names.
     *
     * @param arity
     *            the arity
     */
    public TermSchema(int arity) {
        attributes = new ArrayList<>();
        for (int i = 0; i < arity; ++i) {
            Value t = Variable.create("ANON" + i);
            this.attributes.add(t);
        }
    }

    /**
     * Returns the term at the given index into the schema.
     *
     * @param i
     *            the index
     * @return the term
     */
    public Value get(int i) {
        return this.attributes.get(i);
    }

    /**
     * Returns the index of the given term in the schema. If the term appears
     * multiple times in the schema, returns the first position.
     *
     * @param t
     *            the term
     * @return the index
     */
    public int get(Value t) {
        return this.attributes.indexOf(t);
    }

    /**
     * Returns the size of this schema.
     *
     * @return the size
     */
    public int size() {
        return this.attributes.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        int sz = size();
        for (int i = 0; i < sz; ++i) {
            sb.append(get(i));
            if (i < sz - 1) {
                sb.append(", ");
            }
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((attributes == null) ? 0 : attributes.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TermSchema other = (TermSchema) obj;
        if (attributes == null) {
            if (other.attributes != null)
                return false;
        } else if (!attributes.equals(other.attributes))
            return false;
        return true;
    }

}
