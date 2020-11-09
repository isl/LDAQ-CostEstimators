package gr.forth.ics.isl.LDaQ.CostEstimator.costEstimationMethods;

import gr.forth.ics.isl.LDaQ.CostEstimator.ldaq.VarBindingData;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.mem.TupleSlot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static gr.forth.ics.isl.LDaQ.CostEstimator.ldaq.Transform.getTriples;

/**
 * Estimate a LDaQ execution cost based on no prior knowledge. <br>
 * Also serves as a basis for the rest of the cost estimation methods
 *
 * @author Fafalios
 * @author Sklavos
 */
public class Method1_noKnowledgeCostEst {

    /**
     * Numbers correspond to averages based on our tests (dbpedia).
     */
    protected static final double AVG_NUM_OF_ENTITY_OUTGOING_PROPERTIES = 25;
    protected static final double AVG_NUM_OF_ENTITY_INCOMING_PROPERTIES = 5;
    protected static final double AVG_NUM_OF_SUBJECT_BINDINGS_FOR_A_GIVEN_PRED_OBJECT = 1504.74;
    protected static final double AVG_NUM_OF_SUBJECT_BINDINGS_FOR_RDF_TYPE = 848;
    protected static final double AVG_NUM_OF_OBJECT_BINDINGS_FOR_A_GIVEN_SUBJECT_PRED = 1.86;

    protected final Set<VarBindingData> boundVars = new HashSet<>();
    protected final Set<String> URIs = new HashSet<>();
    protected final Set<Node> resolvedVars = new HashSet<>();
    protected final List<TriplePath> pendingTriples = new ArrayList<>();
    protected double cost = 0;
    protected double prevCost = 1;

    /**
     * We keep the query in order to avoid creating it repeatedly at each method that extends this one.
     */
    protected Query query = null;

    /**
     * Entry point of {@link Method1_noKnowledgeCostEst} and {@link Method2_predicatesDataCostEst}
     *
     * @param queryStr the string representation of a SPARQL query
     * @return the estimation
     */
    public double estimate(String queryStr) {

        if (query == null)
            query = QueryFactory.create(queryStr);

        final double estimate = estimate(query);
        return (double) Math.round(estimate * 100) / 100; // keep two decimal digits
    }

    protected final double estimate(Query query) {

        ArrayList<TriplePath> triples = getTriples(query.getQueryPattern());
        for (TriplePath triple : triples) {
            System.out.println("-------------------------------");
            System.out.println("# Triple: " + triple);
            System.out.println("# Status Until Now:");
            System.out.println("  - Bound vars: " + boundVars);
            System.out.println("  - Resolved vars: " + resolvedVars);
            System.out.println("  - Pending triples: " + pendingTriples);

            if (!checkTriple(triple)) {
                pendingTriples.add(triple);
            }
        }

        if (!pendingTriples.isEmpty()) {
            System.out.println("\n-------------------------------");
            System.out.println("# Start Checking pending triples...");
        }
        while (!pendingTriples.isEmpty()) {

            ArrayList<TriplePath> toRemove = new ArrayList<>();
            for (TriplePath triple : pendingTriples) {
                System.out.println("-------------------------------");
                System.out.println("# Triple: " + triple);
                System.out.println("# Status:");
                System.out.println("  - Bound vars: " + boundVars);
                System.out.println("  - Resolved vars: " + resolvedVars);
                System.out.println("  - Pending triples: " + pendingTriples);

                if (checkTriple(triple)) {
                    toRemove.add(triple);
                }
            }
            pendingTriples.removeAll(toRemove);
        }

        return cost;
    }

    /**
     * Estimate cost based on type of predicate, previous cost and other factors.
     *
     * @return the cost estimation
     */
    protected double estimateNumOfBindings(Node var) {

        double numOfBindings = 0;
        for (VarBindingData bv : boundVars) {
            if (bv.getSubjectOrObject().equals(var)) {
                System.out.println("\t# Estimating number of bindings of variable '" + var + "'...");
                System.out.println("\t# Bindings data: " + bv);

                if (bv.getPredicates().size() == 1) {
                    System.out.println("\t  ONE predicate was used to bind the variable! ");
                    Node predicate = bv.getPredicates().get(0);
                    TupleSlot positionInTriple = bv.getPositionsInTriple().get(0);

                    if (predicate.isURI()) {
                        System.out.println("\t  Predicate is a URI! ");
                        if (positionInTriple.equals(TupleSlot.SUBJECT)) {
                            System.out.println("\t  Variable was the SUBJECT in the triple! ");
                            if (predicate.getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
                                numOfBindings = AVG_NUM_OF_SUBJECT_BINDINGS_FOR_RDF_TYPE;
                            } else {
                                numOfBindings = AVG_NUM_OF_SUBJECT_BINDINGS_FOR_A_GIVEN_PRED_OBJECT;
                            }
                        } else if (positionInTriple.equals(TupleSlot.OBJECT)) {
                            System.out.println("\t  Variable was the OBJECT in the triple! ");
                            numOfBindings = AVG_NUM_OF_OBJECT_BINDINGS_FOR_A_GIVEN_SUBJECT_PRED;
                        } else { // predicate
                            System.out.println("\t  Variable was the PREDICATE in the triple! ");
                            numOfBindings = AVG_NUM_OF_ENTITY_OUTGOING_PROPERTIES;
                        }
                    } else { //the predicate is  a variable
                        System.out.println("\t  Predicate is a VARIABLE! ");
                        if (positionInTriple.equals(TupleSlot.SUBJECT)) {
                            System.out.println("\t  Variable was the SUBJECT in the triple! ");
                            numOfBindings = AVG_NUM_OF_SUBJECT_BINDINGS_FOR_A_GIVEN_PRED_OBJECT *
                                            AVG_NUM_OF_ENTITY_INCOMING_PROPERTIES;
                        } else if (positionInTriple.equals(TupleSlot.OBJECT)) {
                            System.out.println("\t  Variable was the OBJECT in the triple! ");
                            numOfBindings = AVG_NUM_OF_ENTITY_OUTGOING_PROPERTIES *
                                            AVG_NUM_OF_OBJECT_BINDINGS_FOR_A_GIVEN_SUBJECT_PRED;
                        } else { // predicate
                            System.out.println("\t  Variable was the PREDICATE in the triple! ");
                            numOfBindings = AVG_NUM_OF_ENTITY_OUTGOING_PROPERTIES;
                        }
                    }
                } else {
                    System.out.println("\t  MULTIPLE predicates were used to bind the variable! ");
                }
            }
        }

        double tempCost = numOfBindings * prevCost;
        prevCost = tempCost;
        return tempCost;
    }

    /**
     * Does nothing here, but needed to be extracted into function in order to support
     * {@link Method3_starShapedJoin#checkTriple(TriplePath)} and <br>
     * {@link Method4_filter#checkTriple(TriplePath)}
     *
     * @param triple
     * @return
     */
    protected boolean checkTriple(TriplePath triple) {
        return checkTriple(triple, d -> d);
    }

    /**
     * Checks when a triple contains a Var that cannot be bound and thus must be added in pendingTriples List. <br>
     * Returns false when
     * <ul>
     *     <li>Both subject and object are variables AND none of them have been bound</li>
     *     <li>Subject is a variable, object is a literal AND subject is not bound</li>
     * </ul>
     *
     * @param triple        the triple to check
     * @param alterCostFunc in order to support {@link Method3_starShapedJoin} and {@link Method4_filter}
     * @return bool if triple was correct, not added in pendingTriples list
     */
    protected final boolean checkTriple(TriplePath triple, Function<Double, Double> alterCostFunc) {

        if (triple.getSubject().isLiteral())
            throw new UnsupportedOperationException("subject is literal");
        Node subject = triple.getSubject();
        Node predicate = triple.getPredicate();
        Node object = triple.getObject();

        if (subject.isURI() || object.isURI()) {
            System.out.println("\t# We need to resolve one of the URIs in the triple," +
                               " thus we increase the cost if the URI is new.");

            String uri = subject.isURI() ? subject.getURI().toLowerCase() : object.getURI().toLowerCase();

            if (URIs.add(uri)) cost++;

            if (object.isVariable()) {
                System.out.println("\t# The variable '" + object + "' can get bound.");
                if (isBoundVar(object)) {
                    addVarData(object, predicate, TupleSlot.OBJECT, subject);
                } else {
                    VarBindingData vbd = new VarBindingData(object, predicate, TupleSlot.OBJECT, subject);
                    boundVars.add(vbd);
                }

                if (predicate.isVariable()) {
                    System.out.println("\t# The variable '" + predicate + "' can get bound.");
                    if (isBoundVar(predicate)) {
                        addVarData(predicate, predicate, TupleSlot.PREDICATE, null);
                    } else {
                        VarBindingData vbd = new VarBindingData(predicate, null, TupleSlot.PREDICATE, subject);
                        boundVars.add(vbd);
                    }
                }
            }

            if (subject.isVariable()) { // subject is var and object is uri
                System.out.println("\t# The variable '" + subject + "' can get bound.");
                if (isBoundVar(subject)) {
                    addVarData(subject, predicate, TupleSlot.SUBJECT, object);
                } else {
                    VarBindingData vbd = new VarBindingData(subject, predicate, TupleSlot.SUBJECT, object);
                    boundVars.add(vbd);
                }

                if (predicate.isVariable()) {
                    System.out.println("\t# The variable '" + predicate + "' can get bound.");
                    if (isBoundVar(predicate)) {
                        addVarData(predicate, predicate, TupleSlot.PREDICATE, null);
                    } else {
                        VarBindingData vbd = new VarBindingData(predicate, null, TupleSlot.PREDICATE, object);
                        boundVars.add(vbd);
                    }
                }
            }

            return true;
        } else if (subject.isVariable() && object.isVariable()) {
            if (isBoundVar(subject) && !isBoundVar(object)) {
                if (!resolvedVars.contains(subject)) {
                    System.out.println("\t# We need to resolve the URI bindings of the variable " + subject);
                    resolvedVars.add(subject);
                    double estimate = estimateNumOfBindings(subject);
                    estimate = alterCostFunc.apply(estimate);
                    this.cost += estimate;
                }
                System.out.println("\t# The variable '" + object + "' can get bound.");

                VarBindingData vbd = new VarBindingData(object, predicate, TupleSlot.OBJECT, subject);
                boundVars.add(vbd);

                if (predicate.isVariable()) {
                    System.out.println("\t# The variable '" + predicate + "' can get bound.");
                    if (isBoundVar(predicate)) {
                        addVarData(predicate, predicate, TupleSlot.PREDICATE, null);
                    } else {
                        VarBindingData vbdPred = new VarBindingData(predicate, null, TupleSlot.PREDICATE, subject);
                        boundVars.add(vbdPred);
                    }
                }
                // the subject variable was already bound, thus we have a join!
                return true;
            }

            if (isBoundVar(object) && !isBoundVar(subject)) {
                if (!resolvedVars.contains(object)) {
                    System.out.println("\t# We need to resolve the URI bindings of the variable " + object);
                    resolvedVars.add(object);
                    double estimate = estimateNumOfBindings(subject);
                    estimate = alterCostFunc.apply(estimate);
                    this.cost += estimate;
                }
                System.out.println("\t# The variable '" + subject + "' can get bound.");

                VarBindingData vbd = new VarBindingData(subject, predicate, TupleSlot.SUBJECT, object);
                boundVars.add(vbd);

                if (predicate.isVariable()) {
                    System.out.println("\t# The variable '" + predicate + "' can get bound.");
                    if (isBoundVar(predicate)) {
                        addVarData(predicate, predicate, TupleSlot.PREDICATE, null);
                    } else {
                        VarBindingData vbdPre = new VarBindingData(predicate, null, TupleSlot.PREDICATE, object);
                        boundVars.add(vbdPre);
                    }
                }
                return true;
            }

            if (!isBoundVar(subject) && !isBoundVar(object)) {
                System.out.println("\t# Adding the triple in the list of pending triples...");
                return false;
            }

            if (isBoundVar(subject) && isBoundVar(object)) {
                System.out.println("\t# No action is needed.");
                // nothing
                return true;
            }

        } else { // subject is a VARIABLE and object is a LITERAL
            if (isBoundVar(subject)) {
                if (!resolvedVars.contains(subject)) {
                    System.out.println("\t# We need to resolve the URI bindings of the variable " + subject);
                    resolvedVars.add(subject);
                    double estimate = estimateNumOfBindings(subject);
                    estimate = alterCostFunc.apply(estimate);
                    this.cost += estimate;
                }
                System.out.println("\t# Adding additional triple information for the variable '" + subject + "'.");
                addVarData(subject, predicate, TupleSlot.SUBJECT, object);
                if (predicate.isVariable()) {
                    System.out.println("\t# The variable '" + predicate + "' can get bound.");
                    if (isBoundVar(predicate)) {
                        addVarData(predicate, predicate, TupleSlot.PREDICATE, null);
                    } else {
                        VarBindingData vbd = new VarBindingData(predicate, null, TupleSlot.PREDICATE, subject);
                        boundVars.add(vbd);
                    }
                }
                return true;
            } else {
                System.out.println("\t# Adding the triple in the list of pending triples...");
                return false;
            }
        }
        return true;
    }

    protected final boolean isBoundVar(Node var) {
        return boundVars.stream()
                        .anyMatch(bv -> bv.getSubjectOrObject().equals(var));
    }

    protected final void addVarData(Node var, Node predicate, TupleSlot ts, Node otherNode) {
        for (VarBindingData bv : boundVars) {
            if (bv.getSubjectOrObject().equals(var)) {
                bv.addPredicate(predicate);
                bv.addPositionInTriple(ts);
                bv.addOtherNode(otherNode);
                return;
            }
        }
    }
}
