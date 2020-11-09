package gr.forth.ics.isl.LDaQ.CostEstimator.costEstimationMethods;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.TriplePath;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static gr.forth.ics.isl.LDaQ.CostEstimator.ldaq.Transform.getTriples;

/**
 * Estimate a LDaQ execution cost based on {@link Method2_predicatesDataCostEst} with the addition of chained triples
 * <p>Filtering Triples:</p>
 * <p>
 * A triple that forms a <b>star shaped join</b> with previous triples
 * <br><u>and</u><br>
 *     <ul>
 *         <li>its object is not part of any triple's subject, or</li>
 *         <li>it is not used in SELECT</li>
 *     </ul>
 * </p>
 *
 * @author Sklavos
 */
public class Method3_starShapedJoin extends Method2_predicatesDataCostEst {

    final Set<TriplePath> triplesInStar = new HashSet<>();
    protected double starFactor = 0.9;

    /**
     * Get all triples of a query that appear in a star pattern.
     *
     * @param query the query
     * @return a set of triples that that appear in a star pattern
     */
    public static Set<TriplePath> getTriplesInStar(Query query) {
        final Set<TriplePath> triplesInJoin = new HashSet<>();

        final var allSubjects = getTriples(query.getQueryPattern())
                .stream()
                .map(TriplePath::getSubject)
                .filter(Node::isVariable)
                .map(Node::toString)
                .collect(Collectors.toSet());

        final ArrayList<TriplePath> triples = getTriples(query.getQueryPattern());
        for (int i = 0; i < triples.size(); i++) {
            final TriplePath tr = triples.get(i);
            var obj = tr.getObject();
            String objStr = obj.toString();
            if (obj.isVariable() && i != 0 && i != triples.size() - 1 && !allSubjects.contains(objStr)) {
                triplesInJoin.add(tr);
            }
        }
        return triplesInJoin;
    }

    /**
     * Support for a custom factor for reducing estimated bindings
     *
     * @param chainFactor the custom factor to base method on
     */
    public final Method3_starShapedJoin chainFactor(double chainFactor) {
        this.starFactor = chainFactor;
        return this;
    }

    /**
     * Entry point of method.
     *
     * @param queryStr the string representation of a SPARQL query
     * @return the estimation
     */
    @Override
    public double estimate(String queryStr) {

        if (query == null)
            query = QueryFactory.create(queryStr);

        triplesInStar.addAll(getTriplesInStar(query));

        return super.estimate(queryStr);
    }

    /**
     * This method's custom checkTriple implementation
     */
    @Override
    protected boolean checkTriple(TriplePath triple) {

        return checkTriple(triple, d -> {
            if (isInStar(triple))
                return d * starFactor;
            else
                return d;
        });
    }

    protected final boolean isInStar(TriplePath t) {
        return triplesInStar.contains(t);
    }
}
