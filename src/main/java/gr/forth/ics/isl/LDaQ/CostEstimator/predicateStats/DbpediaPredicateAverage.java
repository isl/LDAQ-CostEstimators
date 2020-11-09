package gr.forth.ics.isl.LDaQ.CostEstimator.predicateStats;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

/**
 * Calculate the average object and subject bindings of a given predicate on dbpedia. <br>
 * Uses JENA Framework. <br>
 * Similar functionality to {@link DbpediaPredicateAverageHttp}
 *
 * @author Sklavos
 */
public final class DbpediaPredicateAverage {
    public static final String endpoint = "http://dbpedia.org/sparql";
    public final String predicate;

    private QueryExecution queryExecution;

    public DbpediaPredicateAverage(String predicate) {
        this.predicate = predicate;
    }

    public double avgObjBindings() {
        String query = "SELECT ?s (COUNT(?o) AS ?count) WHERE { ?s " + predicate + " ?o } GROUP BY ?s";
        ResultSet rs = executeQuery(query);
        return resultSetAverage(rs);
    }

    public double avgSubjBindings() {
        String query = "SELECT (COUNT(?o) AS ?count) ?o WHERE { ?s " + predicate + " ?o } GROUP BY ?o";
        ResultSet rs = executeQuery(query);
        return resultSetAverage(rs);
    }

    private ResultSet executeQuery(String q) {
        queryExecution = QueryExecutionFactory.sparqlService(endpoint, q);
        return queryExecution.execSelect();
    }

    private double resultSetAverage(ResultSet rs) {
        int sum = 0;
        int count = 0;

        while (rs.hasNext()) {
            QuerySolution qs = rs.next();

            int bindings = (int) qs.get("count").asNode().getLiteralValue();
            sum += bindings;
            count++;
        }

        queryExecution.close();

        return (double) sum / count;
    }
}
