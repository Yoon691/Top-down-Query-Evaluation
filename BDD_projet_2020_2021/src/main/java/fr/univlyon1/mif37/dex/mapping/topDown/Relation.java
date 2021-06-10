package fr.univlyon1.mif37.dex.mapping.topDown;

import fr.univlyon1.mif37.dex.mapping.Value;

import java.util.*;
import java.util.function.Function;
/**
 * @juba BDD
 */
/**
 * A relation, i.e., a set of tuples  with an associated
 * attribute schema of the same arity.
 *
 */
public class Relation implements Iterable<Tuple>  {
    /**
     * The tuples of this relation.
     */
    protected Set<Tuple> tuples;
    /**
     * The attribute schema of this relation.
     */
    protected TermSchema attributes;

    public final int bound;


    public Relation(Relation other) {
        this.tuples = new HashSet<>(other.tuples);
        this.attributes = new TermSchema(other.attributes);
        this.bound = other.bound;
    }


    public Relation(TermSchema attributes) {
        this.tuples = new HashSet<>();
        this.attributes = attributes;
        this.bound = attributes.size();
    }


    public Relation(int bound) {
        this.tuples = new HashSet<>();
        this.bound = bound;
        this.attributes = new TermSchema(bound);
    }

    /**
     * Add a tuple of the proper arity to this relation.
     *
     * @param x
     *            the tuple
     * @return whether this relation has changed
     */
    public boolean add(Tuple x) {
        if (x.size() != this.bound) {
            throw new IllegalArgumentException("Relation has arity "
                    + this.bound + " but tuple has size " + x.size() + ".");
        }
        return this.tuples.add(x);
    }

    /**
     * Add all the tuples of another relation to this relation. The two
     * relations must have the same arity.
     *
     * @param other
     *            the other relation
     * @return whether this relation has changed
     */
    public boolean addAll(Relation other) {
        if (other.bound != this.bound) {
            throw new IllegalArgumentException(
                    "Cannot add a relation of arity " + other.bound
                            + " to a relation of arity " + this.bound + ".");
        }
        return this.tuples.addAll(other.tuples);
    }

    /**
     * Remove all the tuples in other relation from this relation. The two
     * relations must have the same arity.
     *
     * @param other
     *            the other relation
     * @return whether this relation has changed
     */
    public boolean removeAll(Relation other) {
        if (other.bound != this.bound) {
            System.out.println(this);
            System.out.println(other);
            throw new IllegalArgumentException(
                    "Cannot remove a relation of arity " + other.bound
                            + " from a relation of arity " + this.bound + ".");
        }
        return this.tuples.removeAll(other.tuples);
    }

    /**
     * Returns a new relation consisting of those tuples that meet the supplied
     * predicate.
     *
     * @param f
     *            the predicate
     * @return the new relation
     */
    public Relation filter(Function<Tuple, Boolean> f) {
        Relation r = new Relation(this.attributes);
        for (Tuple t : this.tuples) {
            if (f.apply(t)) {
                r.tuples.add(t);
            }
        }
        return r;
    }

    /**
     * Returns the number of tuples in this relation.
     *
     * @return the number of tuples
     */
    public int size() {
        return this.tuples.size();
    }

    /**
     * Returns whether the relation has any tuples.
     *
     * @return whether the relation is empty
     */
    public boolean isEmpty() {
        return this.tuples.isEmpty();
    }

    /**
     * Creates a new relation by joining this relation with the other relation
     * and projecting onto the supplied attribute schema. If the schema has
     * attributes not in either relation, those terms are null.
     *
     * @param other
     *            the other relation
     * @param schema
     *            the attribute schema
     * @return the new relation
     */
    public Relation joinAndProject(Relation other, TermSchema schema) {
        // The join is implemented via hash-join.

        // Find common terms in this relation and the other one.
        Set<Value> thisTerms = new HashSet<>();
        for (int i = 0; i < this.attributes.size(); ++i) {
            thisTerms.add(this.attributes.get(i));
        }
        Set<Value> otherTerms = new HashSet<>();
        for (int i = 0; i < other.attributes.size(); ++i) {
            otherTerms.add(other.attributes.get(i));
        }
        thisTerms.retainAll(otherTerms);

        // Identify the smaller of the two relations.
        Relation smaller;
        Relation larger;
        if (size() < other.size()) {
            smaller = this;
            larger = other;
        } else {
            smaller = other;
            larger = this;
        }

        // Create a map from term to an integer index into the schema used when
        // hashing tuples.
        Map<Value, Integer> indexMap = new HashMap<>();
        int idx = 0;
        for (Value term : thisTerms) {
            indexMap.put(term, idx++);
        }

        // Create a map from term to an integer index into the schema for output
        // tuples.
        Map<Value, Integer> outputMap = new HashMap<>();
        for (int i = 0; i < schema.size(); ++i) {
            outputMap.put(schema.get(i), i);
        }

        // Determine how the schema for the smaller relation relates to the
        // schemas used for hashing the tuples and for the output.
        Integer[] hashTupIdxForSmaller = new Integer[smaller.bound];
        Integer[] outputTupIdxForSmaller = new Integer[smaller.bound];
        for (int i = 0; i < smaller.attributes.size(); ++i) {
            Value attribute = smaller.attributes.get(i);
            hashTupIdxForSmaller[i] = indexMap.get(attribute);
            outputTupIdxForSmaller[i] = outputMap.get(attribute);
        }

        // Add tuples from smaller relation to the hash table.
        HashMap<Tuple, Set<Tuple>> table = new HashMap<>();
        int hashTupSize = thisTerms.size();
        for (Tuple t : smaller) {
            Tuple hashTup = toHashTuple(t, hashTupIdxForSmaller, hashTupSize);
            Set<Tuple> tuples = table.get(hashTup);
            if (tuples == null) {
                tuples = new HashSet<>();
                table.put(hashTup, tuples);
            }
            tuples.add(t);
        }

        // Determine how the schema for the larger relation relates to the
        // schemas used for hashing the tuples and for the output.
        Integer[] hashTupIdxForLarger = new Integer[larger.bound];
        Integer[] outputTupIdxForLarger = new Integer[larger.bound];
        for (int i = 0; i < larger.bound; ++i) {
            Value attribute = larger.attributes.get(i);
            hashTupIdxForLarger[i] = indexMap.get(attribute);
            outputTupIdxForLarger[i] = outputMap.get(attribute);
        }

        // Perform the actual join and project each tuple onto the output schema
        Relation result = new Relation(schema);
        for (Tuple t1 : larger) {
            Tuple hashTup = toHashTuple(t1, hashTupIdxForLarger, hashTupSize);
            Set<Tuple> tuples = table.get(hashTup);
            if (tuples != null) {
                // Create a template for the output tuple based on the terms in
                // t1.
                Value[] outputTempl = new Value[schema.size()];
                for (int i = 0; i < larger.bound; ++i) {
                    Integer j = outputTupIdxForLarger[i];
                    if (j != null) {
                        outputTempl[j] = t1.get(i);
                    }
                }

                // Create new tuples by adding terms from the tuples in the
                // smaller relation to the output template.
                for (Tuple t2 : tuples) {
                    Value[] output = new Value[outputTempl.length];
                    System.arraycopy(outputTempl, 0, output, 0, output.length);

                    for (int i = 0; i < smaller.bound; ++i) {
                        Integer j = outputTupIdxForSmaller[i];
                        if (j != null) {
                            output[j] = t2.get(i);
                        }
                    }
                    result.add(new Tuple(Arrays.asList(output)));
                }
            }
        }

        return result;
    }


    private static Tuple toHashTuple(Tuple input, Integer[] hashTupIdx,
                                     int hashTupSize) {
        Value[] terms = new Value[hashTupSize];
        for (int i = 0; i < input.size(); ++i) {
            Integer j = hashTupIdx[i];
            if (j != null) {
                terms[j] = input.get(i);
            }
        }
        return new Tuple(Arrays.asList(terms));
    }


    public Relation applyTuplesAsSubstitutions(Tuple x) {
        Relation r = new Relation(x.size());
        for (Tuple t : this.tuples) {
            Map<Value, Value> subst = new HashMap<>();
            for (int j = 0; j < this.bound; ++j) {
                subst.put(this.attributes.get(j), t.get(j));
            }
            List<Value> newTerms = new ArrayList<>();
            for (Value term : x.elts) {
                Value s = subst.get(term);
                if (s != null) {
                    term = s;
                }
                newTerms.add(term);
            }
            r.add(new Tuple(newTerms));
        }
        return r;
    }


    public Relation project(List<Boolean> colsToKeep) {
        assert colsToKeep.size() == this.bound;

        int nKeepers = 0;
        for (int i = 0; i < colsToKeep.size(); ++i) {
            if (colsToKeep.get(i)) {
                ++nKeepers;
            }
        }

        Relation r = new Relation(nKeepers);
        for (Tuple t : this.tuples) {
            List<Value> newTerms = new ArrayList<>();
            for (int i = 0; i < this.bound; ++i) {
                if (colsToKeep.get(i)) {
                    newTerms.add(t.get(i));
                }
            }
            r.add(new Tuple(newTerms));
        }
        return r;
    }


    public boolean contains(Tuple x) {
        return this.tuples.contains(x);
    }


    public TermSchema getAttributes() {
        return this.attributes;
    }


    public void renameAttributes(TermSchema schema) {
        if (schema.size() != this.bound) {
            throw new IllegalArgumentException("Schema of size "
                    + schema.size()
                    + " cannot be given to a relation of arity " + this.bound
                    + ".");
        }
        this.attributes = schema;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Attributes: " + attributes);
        sb.append("; Tuples: ");
        for (Iterator<Tuple> it = this.tuples.iterator(); it.hasNext();) {
            sb.append(it.next());
            if (it.hasNext()) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + bound;
        result = prime * result
                + ((attributes == null) ? 0 : attributes.hashCode());
        result = prime * result + ((tuples == null) ? 0 : tuples.hashCode());
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
        Relation other = (Relation) obj;
        if (bound != other.bound)
            return false;
        if (attributes == null) {
            if (other.attributes != null)
                return false;
        } else if (!attributes.equals(other.attributes))
            return false;
        if (tuples == null) {
            if (other.tuples != null)
                return false;
        } else if (!tuples.equals(other.tuples))
            return false;
        return true;
    }

    @Override
    public Iterator<Tuple> iterator() {
        return this.tuples.iterator();
    }

}
