package fr.univlyon1.mif37.dex.mapping.topDown;

import fr.univlyon1.mif37.dex.mapping.Constant;
import fr.univlyon1.mif37.dex.mapping.Value;
import fr.univlyon1.mif37.dex.mapping.Variable;

import java.util.Arrays;
import java.util.List;
/**
 * @juba BDD
 */

/**
 * A tuple of terms, i.e., an ordered list of fixed arity.
 *
 */
public class Tuple {
    /**
     * The value in this tuple.
     */
    public Value[] elts;
    public String name;
    private Boolean isGround;

    /**
     * Constructs a tuple from a list of values.
     *
     * @param elts the list of values
     */
    public Tuple(List<Value> elts) {
        Value[] tmp = new Value[elts.size()];
        this.elts = elts.toArray(tmp);

    }

    public Tuple(Value[] elts) {
        this.elts = elts;
    }

    /**
     * Returns the term at the ith position in this tuple (0-indexed).
     *
     * @param i the position
     * @return the term
     */
    public Value get(int i) {
        return this.elts[i];
    }

    /**
     * Returns the arity of this tuple.
     *
     * @return the arity
     */
    public int size() {
        return this.elts.length;
    }

//    public Tuple unify(Tuple other) {
//        Variable subst = Variable.create(this.elts, other.elts);
//        if (subst != null) {
//            return new Tuple(subst.apply(this.elts));
//        }
//        return null;
//    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<");
        for (int i = 0; i < this.elts.length; ++i) {
            sb.append(elts[i]);
            if (i < this.elts.length - 1) {
                sb.append(", ");
            }
        }
        sb.append(">");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(elts);
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
        Tuple other = (Tuple) obj;
        if (!Arrays.equals(elts, other.elts))
            return false;
        return true;
    }


    public boolean isGround() {
        Boolean isGround;
        if ((isGround = this.isGround) == null) {
            // This might do redundant work since we do not synchronize, but
            // it's still sound, and it's probably cheap enough that
            // synchronizing might be more expensive.
            boolean b = true;
            Value[] args = new Value[0];
            for (Value t : args) {
                b &= t instanceof Constant;
            }
            this.isGround = isGround = Boolean.valueOf(b);
        }
        return isGround;
    }
}
