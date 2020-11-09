package gr.forth.ics.isl.LDaQ.CostEstimator.costEstimationMethods;

import gr.forth.ics.isl.LDaQ.CostEstimator.ldaq.VarBindingData;
import gr.forth.ics.isl.LDaQ.CostEstimator.util.BindPair;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.mem.TupleSlot;

import java.nio.file.Path;
import java.util.Map;

/**
 * Estimate a LDaQ execution cost based on {@link Method1_noKnowledgeCostEst} plus a list of predicates
 * with average bindings. <br>
 * The predicates are the most common we extracted from dbpedia logs  <br>
 * We consequently calculated their average bindings
 *
 * @author Fafalios
 * @author Sklavos
 */
public class Method2_predicatesDataCostEst extends Method1_noKnowledgeCostEst {

    private static final Map<String, BindPair> knownPredicates =
            BindPair.getBindings(Path.of("textFiles/knownPredicatesBindings.csv"));

    /**
     * Estimate cost based on type of predicate, previous cost and other factors.
     *
     * @return the cost estimation
     */
    @Override
    protected final double estimateNumOfBindings(Node var) {

        double numOfBindings = 0;
        for (VarBindingData bv : boundVars) {
            if (bv.getSubjectOrObject().equals(var)) {
                System.out.println("\t# Estimating number of bindings of variable '" + var + "'...");
                System.out.println("\t# Bindings data: " + bv);

                if (bv.getPredicates().size() == 1) {
                    System.out.println("\t  ONE predicate was used to bind the variable! ");
                    Node predicate = bv.getPredicates().get(0);
                    TupleSlot positionInTriple = bv.getPositionsInTriple().get(0);

                    if (predicate.isURI()) { // the predicate is a uri
                        System.out.println("\t  Predicate is a URI! ");
                        if (positionInTriple.equals(TupleSlot.SUBJECT)) {
                            System.out.println("\t  Variable was the SUBJECT in the triple! ");
                            if (knownPredicates.containsKey(predicate.getURI())) {
                                numOfBindings = knownPredicates.get(predicate.getURI()).avgSubjBind;
                            } else {
                                numOfBindings = AVG_NUM_OF_SUBJECT_BINDINGS_FOR_A_GIVEN_PRED_OBJECT;
                            }
                        } else if (positionInTriple.equals(TupleSlot.OBJECT)) {
                            System.out.println("\t  Variable was the OBJECT in the triple! ");
                            if (knownPredicates.containsKey(predicate.getURI())) {
                                numOfBindings = knownPredicates.get(predicate.getURI()).avgObjBind;
                            } else {
                                numOfBindings = AVG_NUM_OF_OBJECT_BINDINGS_FOR_A_GIVEN_SUBJECT_PRED;
                            }
                        } else { // predicate
                            System.out.println("\t  Variable was the PREDICATE in the triple! ");
                            numOfBindings = AVG_NUM_OF_ENTITY_OUTGOING_PROPERTIES;
                        }
                    } else { //the predicate is a variable
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
}
