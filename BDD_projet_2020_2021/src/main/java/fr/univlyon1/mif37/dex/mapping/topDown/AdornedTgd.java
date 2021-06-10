package fr.univlyon1.mif37.dex.mapping.topDown;
import  fr.univlyon1.mif37.dex.mapping.Variable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @juba BDD
 */
/**
 * An adorned tgd (i.e., a Horn clause where every atom is itself adorned).
 *
 */

public class AdornedTgd {
    private  AdornedAtom head;
    private  List<AdornedAtom> body;

    /**
     * Constructs an adorned tgd given an adorned atom for the head and a
     * list of adorned atoms for the body.
     *
     * @param head
     *            head atom of clause
     * @param body
     *            atoms for body of clause
     */
    public AdornedTgd(AdornedAtom head, List<AdornedAtom> body) {
        this.head = head;
        this.body = body;
    }

    public AdornedTgd() {

    }

    public AdornedTgd fromClause(List<Boolean> headAdornment, ValidTgd clause) {

        return new AdornedTgd(this.head, this.body);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(head);
        if (!body.isEmpty()) {
            sb.append(" :- ");
            for (int i = 0; i < body.size(); ++i) {
                sb.append(body.get(i));
                if (i < this.body.size() - 1) {
                    sb.append(", ");
                }
            }
        }
        sb.append('.');
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((body == null) ? 0 : body.hashCode());
        result = prime * result + ((head == null) ? 0 : head.hashCode());
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
        AdornedTgd other = (AdornedTgd) obj;
        if (body == null) {
            if (other.body != null)
                return false;
        } else if (!body.equals(other.body))
            return false;
        if (head == null) {
            if (other.head != null)
                return false;
        } else if (!head.equals(other.head))
            return false;
        return true;
    }

    public List<AdornedAtom> getBody() {
        return body;
    }

    public AdornedAtom getHead() {
        return head;
    }




    public static final class ValidTgd extends AdornedTgd {

        public ValidTgd(AdornedAtom head, List<AdornedAtom> body) {
            super(head, body);
        }
    }

}
