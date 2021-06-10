package fr.univlyon1.mif37.dex.mapping.topDown;

import fr.univlyon1.mif37.dex.mapping.*;

import java.util.*;
/**
 * @juba BDD
 */
/**
 * A Datalog evaluation engine that uses a recursive version of the
 * query-subquery top-down technique.
 *
 */
public class RecursiveQsqEngine {
//    private Collection<Relation> edbRelations ;
//    private Mapping idbRules;

    /**
     * A container for tracking global information passed back and forth between
     * recursion frames.
     *
     */
    private class QSQRState {
        /**
         * Tracks the answer tuples generated for each adorned predicate.
         */
        private Map<AdornedAtom,Relation> ans;
        /**
         * Tracks which input tuples have been used for each rule.
         */
        private Map<AdornedTgd,Relation> inputByRule;
        /**
         * Holds all the adorned rules for a given adorned predicate.
         */
        private Map<AdornedAtom,Set<AdornedTgd>> adornedRules;
        /**
         * Holds all the unadorned rules for a given predicate.
         */
        private final Map<Object, Object> unadornedRules;

        private int inputCount;
        /**
         * Tracks the total number of answer tuples that have been generated.
         */
        private int ansCount;

        /**
         * Initializes state with a set of all unadorned rules for the program.
         *
         * @param unadornedRules
         *            set of unadorned rules
         */
        public QSQRState(Map<Object,Object> unadornedRules) {
            this.ans = new LinkedHashMap<>();
            this.inputByRule = new LinkedHashMap<>();
            this.adornedRules = new LinkedHashMap<>();
            this.unadornedRules = unadornedRules;
            this.inputCount = 0;
            this.ansCount = 0;
        }


        public Set<AdornedTgd> getAdornedRules(AdornedAtom p) {
            Set<AdornedTgd> rules = this.adornedRules.get(p);
            // Lazily create adorned rules.
            if (rules == null) {
                rules = new LinkedHashSet<>();
                Set<AdornedTgd.ValidTgd> unadornedRules = (Set<AdornedTgd.ValidTgd>) this.unadornedRules.get(p
                        .getAtom());
                // No applicable rules for predicate...
                if (unadornedRules == null) {
                    return null;
                }
                for (AdornedTgd.ValidTgd c : unadornedRules) {
                    AdornedTgd adornedRule = new AdornedTgd();
                    adornedRule = adornedRule.fromClause(p.getAdornment(), c);
                    rules.add(adornedRule);
                    this.inputByRule.put(adornedRule, new Relation(p.getBound()));
                }
                adornedRules.put(p, rules);
            }
            return rules;
        }



        public int getInputCount() {
            return this.inputCount;
        }

        /**
         * Get the current answer count.
         *
         * @return current answer count
         */
        public int getAnsCount() {
            return this.ansCount;
        }

        /**
         * Given a rule and a relation, returns a subset of that relation containing
         * the tuples that have not yet been processed for this rule.
         *
         * @param rule
         *            rule
         * @param newTuples
         *            relation
         * @return subset of input relation
         */
        public Relation filterNewInput(AdornedTgd rule, Relation newTuples) {
            Relation delta = new Relation(newTuples);
            delta.removeAll(this.getInput(rule));
            return delta;
        }

        /**
         * Adds tuples in delta to the extant input relation for this rule,
         * increasing the input count by the number of new tuples in delta.
         *
         * @param rule
         *            rule
         * @param delta
         *            input relation
         * @return number of new tuples added
         */
        public int addToInput(AdornedTgd rule, Relation delta) {
            Relation input = this.getInput(rule);
            int oldSize = input.size();
            input.addAll(delta);
            int diff = input.size() - oldSize;
            this.inputCount += diff;
            return diff;
        }

        /**
         * Retrieves the current input relation for rule.
         *
         * @param rule
         *            rule
         * @return input relation
         */
        public Relation getInput(AdornedTgd rule) {
            Relation r = this.inputByRule.get(rule);
            if (r == null) {
                r = new Relation(rule.getHead().getBound());
                this.inputByRule.put(rule, r);
            }
            return r;
        }

        /**
         * Adds the tuples in supplied relation to the answer relation for the
         * adorned predicate p, increasing the answer count for that predicate by
         * the number of new tuples.
         *
         * @param p
         *            adorned predicate
         * @param newTuples
         *            relation
         */
        public void updateAns(AdornedAtom p, Relation newTuples) {
            Relation answer = this.getAns(p);
            int oldSize = answer.size();
            answer.addAll(newTuples);
            this.ansCount += answer.size() - oldSize;
        }

        /**
         * Adds the supplied tuple to the answer relation for the adorned predicate
         * p, increasing the answer count for that predicate if the tuple is new.
         *
         * @param p
         *            adorned predicate
         * @param newTuple
         *            tuple
         */
        public void updateAns(AdornedAtom p, Tuple newTuple) {
            if (this.getAns(p).add(newTuple)) {
                ++this.ansCount;
            }
        }

        /**
         * Retrieves the current answer relation for the adorned predicate p.
         *
         * @param p
         *            adorned predicate
         * @return answer relation
         */
        public Relation getAns(AdornedAtom p) {
            Relation r = (Relation) this.ans.get(p);
            if (r == null) {
                r = new Relation(p.getBound());
                this.ans.put(p, r);
            }
            return r;
        }
    }

    protected Set<Atom> checkIfEdbQuery(Atom q) {
        Mapping mp = new Mapping();
        Collection<fr.univlyon1.mif37.dex.mapping.Relation> facts = mp.getEDB();
        if (facts != null) {
            Set<Atom> result = new LinkedHashSet<>();
            for (fr.univlyon1.mif37.dex.mapping.Relation fact : facts) {
                if (q.unify(new Tuple(q.getArgs())) != null) {
                    result.add(Atom.create(q.getName(), q.getArgs()));
                }
            }
            return result;
        }
        return null;
    }

    public Set<Atom> query(Atom q) {
        Mapping mp = new Mapping();
        // Is the query for EDB facts?
        Set<Atom> edbFacts = checkIfEdbQuery(q);
        if (edbFacts != null) {
            return edbFacts;
        }

        // Figure out adornment from query.
        List<Boolean> adornment = new ArrayList<>();
        List<Value> input = new ArrayList<>();
        for (Value t : q.getArgs()) {
            if (t instanceof Constant) {
                adornment.add(true);
                input.add(t);
            } else {
                adornment.add(false);
            }
        }

        // Create initial input for QSQR algorithm.
        AdornedAtom p = new AdornedAtom(q, adornment);
        Tuple t = new Tuple(input);
        Relation r = new Relation(input.size());
        r.add(t);
        QSQRState state = new QSQRState((Map<Object, Object>) mp.getIDB());

        qsqr(p, r, state);

        Set<Atom> results = new LinkedHashSet<>();
        for (Tuple fact : state.getAns(p)) {
            if (q.unify(new Tuple(q.getArgs())) != null) {
                results.add(Atom.create(q.getName(), fact.elts));
            }
        }
        return results;
    }


    /**
     * Evaluates the query represented by the adorned predicate p and the
     * relation newInput.
     *
     * @param p
     *            adorned predicate of query
     * @param newInput
     *            input tuples
     * @param state
     *            current state of evaluation-wide variables
     */
    private void qsqr(AdornedAtom p, Relation newInput, QSQRState state) {
        Set<AdornedTgd> rules = state.getAdornedRules(p);
        if (rules == null) {
            return;
        }

        // Calculate the new input per rule.
        Map<AdornedTgd, Relation> newInputByRule = new LinkedHashMap<>();
        for (AdornedTgd rule : rules) {
            Relation delta = state.filterNewInput(rule, newInput);
            newInputByRule.put(rule, delta);
        }

        int oldInputSize;
        int oldAnsSize;
        boolean firstTime = true;
        do {
            oldInputSize = state.getInputCount();
            oldAnsSize = state.getAnsCount();

            for (AdornedTgd rule : rules) {
                Relation input = newInputByRule.get(rule);
                // Record that the rule has been called with the new input.
                if (firstTime) {
                    // Adjust the record of the old input size to take into
                    // account the new input we just added.
                    oldAnsSize += state.addToInput(rule, input);
                }
                qsqrSubroutine(rule, input, state);
            }
            firstTime = false;
        } while (oldInputSize != state.getInputCount()
                || oldAnsSize != state.getAnsCount());
    }

    /**
     * Evaluates the supplied rule using the input tuples newInput.
     *
     * @param rule
     *            rule
     * @param newInput
     *            input tuples
     * @param state
     *            current state of evaluation-wide variables
     */
    private void qsqrSubroutine(AdornedTgd rule, Relation newInput, QSQRState state) {
        // See which input tuples actually unify with the head.
        Tuple headTuple = new Tuple(rule.getHead().getArgs());
        Relation sup = newInput;
        fr.univlyon1.mif37.dex.mapping.Relation output = null;

        // No new tuples to test, so exit.
        if (sup.isEmpty()) {
            return;
        }

        // Handles special case of explicit IDB fact. Note that we are currently
        // skipping the dependency stuff below.
        if (rule.getBody().isEmpty()) {
            state.updateAns(rule.getHead().getAdornedAtom(), headTuple);
            return;
        }

        // Want projection only of bound *variables*. This handles the case
        // where a rule has a constant in a bound position.
        List<Boolean> isBoundVar = new ArrayList<>();
        for (int i = 0; i < rule.getHead().getAdornedAtom().getBound(); ++i) {
            if (rule.getHead().getAtom().getAdornment().get(i)) {
                isBoundVar.add(rule.getHead().getArgs()[i] instanceof Variable);
            }
        }
        sup = sup.project(isBoundVar);

        QsqTemplate templ = new QsqTemplate(rule);
        sup.renameAttributes(templ.get(0));
        Mapping mp = new Mapping();
        // Process rule one atom/supplemental relation at a time.
        for (int i = 1; i < templ.size(); ++i) {
            AdornedAtom a = rule.getBody().get(i - 1);
            Collection<fr.univlyon1.mif37.dex.mapping.Relation> facts = mp.getEDB();
            if (facts != null) {
                // We have an EDB predicate.
                facts.add(output);

            } else {
                // We have an IDB predicate.
                Relation input = sup.applyTuplesAsSubstitutions(new Tuple(
                        a.getArgs()));
                input = input.project(a.getAtom().getAdornment());

                // Recurse down subquery.
                qsqr(a.getAdornedAtom(), input, state);

                Relation answers = state.getAns(a.getAdornedAtom());

                answers.renameAttributes(new TermSchema(Arrays.asList(a.getArgs())));
                sup = answers.joinAndProject(sup, templ.get(i));
            }
        }

        // Create a substitution from the final supplementary relation.
        state.updateAns(rule.getHead().getAdornedAtom(),
                sup.applyTuplesAsSubstitutions(new Tuple(rule.getHead().getArgs())));

    }

    private void applyBoundArgs(Tuple headTuple, List<Boolean> adornment, Tuple t) {
    }

}
