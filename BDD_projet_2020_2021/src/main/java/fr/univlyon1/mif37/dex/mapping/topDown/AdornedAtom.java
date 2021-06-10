package fr.univlyon1.mif37.dex.mapping.topDown;
import fr.univlyon1.mif37.dex.mapping.Atom;
import fr.univlyon1.mif37.dex.mapping.Value;

import java.util.List;
/**
 * @juba BDD
 */

/**
 * An adorned atom. Each argument of any atom formed from this
 * predicate symbol will be marked as bound or free in accordance with the
 * predicate's adornment.
 *
 */


public class AdornedAtom {

    private Atom atom;
    private AdornedAtom Adornedatom;

    /**
     * The adornment of the atom. A true value means that the
     * corresponding term in the arguments of an atom
     * is considered bound; a false value implies that the argument is
     * free.
     */
    private List<Boolean> adornment;
    /**
     * Total number of bound terms in the adornment.
     */
    private int bound;
    private  Value[] args;
    /**
     * Constructs an adorned Atom from a atom and an
     * adornment.
     *  @param atom
     *            atom
     * @param adornment
     * @param args
     */
    public AdornedAtom(Atom atom, List<Boolean> adornment, Value[] args) {
        this.atom = atom;
        this.adornment = adornment;

        this.args = args;
    }

    public AdornedAtom(Atom q, List<Boolean> adornment) {
        this.atom = q;
        this.adornment = adornment;
    }

    public Atom getAtom() {
        return atom;
    }

        public AdornedAtom getAdornedAtom() {
        return Adornedatom;
    }

    public List<Boolean> getAdornment() {
        return adornment;
    }

    public int getBound() {
        return bound;
    }

    public Value[] getArgs() {
        return args;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((adornment == null) ? 0 : adornment.hashCode());
        result = prime * result + ((atom == null) ? 0 : atom.hashCode());
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
        AdornedAtom other = (AdornedAtom) obj;
        if (adornment == null) {
            if (other.adornment != null)
                return false;
        } else if (!adornment.equals(other.adornment))
            return false;
        if (atom == null) {
            if (other.atom != null)
                return false;
        } else if (!atom.equals(other.atom))
            return false;
        return true;
    }
}
