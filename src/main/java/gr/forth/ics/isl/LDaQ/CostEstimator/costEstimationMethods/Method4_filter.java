package gr.forth.ics.isl.LDaQ.CostEstimator.costEstimationMethods;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.syntax.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Estimate a LDaQ execution cost based on {@link Method3_starShapedJoin} in addition to considering FILTER clauses.
 *
 * @author Sklavos
 */
public class Method4_filter extends Method3_starShapedJoin {

    final Map<TriplePath, Boolean> triplesWithFilters = new HashMap<>();
    private double filterFactor = 0.9;

    /**
     * Get all triples in a SPARQL query that are followed by FILTER clauses
     */
    public static Map<TriplePath, Boolean> getTriplesWithFilters(Element graphPattern) {
        final Map<TriplePath, Boolean> triples = new HashMap<>();

        ElementWalker.walk(graphPattern, new ElementVisitorBase() {

            TriplePath last;

            @Override
            public void visit(ElementPathBlock el) {
                for (final TriplePath triple : el.getPattern()) {
                    triples.put(triple, false);
                    last = triple;
                }
            }

            @Override
            public void visit(ElementFilter el) {
                triples.put(last, true);
            }

        });
        return triples;
    }

    /**
     * Check if all filters of a query appear at the end (research interest).
     */
    public static boolean filterIsAtEnd(String query) throws RuntimeException {
        try {
            final Query q = QueryFactory.create(query);

            if (!query.toUpperCase().contains("FILTER"))
                throw new RuntimeException("Query has no filter");

            final AtomicBoolean isAtEnd = new AtomicBoolean(true);

            ElementWalker.walk(q.getQueryPattern(), new ElementVisitorBase() {

                boolean hasVisitedFilter = false;

                @Override
                public void visit(ElementPathBlock el) {
                    if (hasVisitedFilter)
                        isAtEnd.set(false);

                }

                @Override
                public void visit(ElementFilter el) {
                    hasVisitedFilter = true;
                }

            });

            return isAtEnd.get();

        } catch (Exception e) {
            throw new RuntimeException("Query cannot be parsed in isFilterAtEnd");
        }
    }

    /**
     * Support for a custom factor for reducing estimated bindings
     *
     * @param factor the custom factor to base method on
     */
    public final Method4_filter filterFactor(double factor) {
        this.filterFactor = factor;
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

        triplesWithFilters.putAll(getTriplesWithFilters(query.getQueryPattern()));

        return super.estimate(queryStr);
    }

    /**
     * This method's custom checkTriple implementation
     */
    @Override
    protected boolean checkTriple(TriplePath triple) {
        return checkTriple(triple, d -> {
            double toReturn = d;
            if (isInStar(triple)) toReturn *= filterFactor;
            if (triplesWithFilters.get(triple)) toReturn *= starFactor;
            return toReturn;
        });
    }
}
