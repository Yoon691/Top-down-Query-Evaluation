package fr.univlyon1.mif37.dex.mapping.topDown;

import fr.univlyon1.mif37.dex.mapping.Value;
import fr.univlyon1.mif37.dex.mapping.Variable;

import java.util.*;
/**
 * @juba BDD
 */
/**
 * A template containing the attribute schemata of the supplementary relations
 * for a given rule in QSQ evaluation.
 *
 */
public class QsqTemplate {
    /**
     * The attribute schemata for the supplementary relations.
     */
    private  List<TermSchema> schemata;
    /**
     * Constructs a template from an adorned rule.
     *
     * @param rule
     *            the adorned rule
     */
    public QsqTemplate(AdornedTgd rule) {
        // Find the last occurrence of each variable.
        Map<Value, Integer> lastPos = new HashMap<>();
        for (int i = 0; i < rule.getBody().size(); ++i) {
            for (Value t : rule.getBody().get(i).getArgs()) {
                if (t instanceof Variable) {
                    lastPos.put(t, i);
                }
            }
        }

        // Determine the schemata for the first and last supplementary
        // relations.
        List<TermSchema> schemata = new ArrayList<>();
        Set<Value> bound = new HashSet<>();
        List<Value> firstSchema = new ArrayList<>();
        List<Value> lastSchema = new ArrayList<>();
        for (int i = 0; i < rule.getHead().getArgs().length; ++i) {
            Value t = rule.getHead().getArgs()[i];
            if (t instanceof Variable) {
                // All variables in the head of the rule are in the schema for
                // the final supplementary relation.
                if (lastSchema.indexOf(t) == -1) {
                    lastSchema.add(t);
                    lastPos.put(t, rule.getBody().size() - 1);
                }

                // Only bound variables in the head of the rule are in the
                // schema for the first supplementary relation.
                if (rule.getHead().getAtom().getAdornment().get(i)) {
                    firstSchema.add(t);
                    bound.add(t);
                }
            }
        }

        // Determine the intermediary schema.
        schemata.add(new TermSchema(firstSchema));
        for (int i = 0; i < rule.getBody().size() - 1; ++i) {
            List<Value> schema = new ArrayList<>();
            for (Value t : rule.getBody().get(i).getArgs()) {
                if (t instanceof Variable) {
                    bound.add(t);
                }
            }

            for (Iterator<Value> iter = bound.iterator(); iter.hasNext();) {
                Value v = iter.next();
                if (lastPos.get(v) == i) {
                    iter.remove();
                } else {
                    schema.add(v);
                }
            }
            schemata.add(new TermSchema(schema));
        }
        schemata.add(new TermSchema(lastSchema));

        this.schemata = new ArrayList<>(schemata);
    }

    /**
     * Returns the schema for the ith (0-indexed) supplementary relation.
     *
     * @param i
     *            the index
     * @return the schema
     */
    public TermSchema get(int i) {
        return this.schemata.get(i);
    }

    /**
     * Returns the number of schemata in this template.
     *
     * @return the number of schemata
     */
    public int size() {
        return this.schemata.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Iterator<TermSchema> it = this.schemata.iterator(); it.hasNext();) {
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
        result = prime * result
                + ((schemata == null) ? 0 : schemata.hashCode());
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
        QsqTemplate other = (QsqTemplate) obj;
        if (schemata == null) {
            if (other.schemata != null)
                return false;
        } else if (!schemata.equals(other.schemata))
            return false;
        return true;
    }



}
