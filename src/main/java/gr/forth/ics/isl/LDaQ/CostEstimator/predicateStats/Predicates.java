package gr.forth.ics.isl.LDaQ.CostEstimator.predicateStats;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Container for methods below which deal with predicates from SPARQL queries.
 *
 * @author Sklavos
 */
public final class Predicates {

    /**
     * Get a frequency map of all the predicates from a new-line-separated SPARQL query file.
     *
     * @return map of (predicate, occurrences), sorted on values -> most frequent predicate on top
     */
    public static Map<String, Integer> filePredicatesFrequencyMap(Path file) throws IOException {
        var freqMap = Collections.synchronizedMap(new HashMap<String, Integer>());

        Files.lines(file)
             .limit(10000)
             .forEach(line -> {
                 try {
                     Query q = QueryFactory.create(line);
                     for (String s : allQueryPredicates(q))
                         freqMap.put(s, freqMap.getOrDefault(s, 0) + 1);
                 } catch (Exception e) {
                     System.out.println("could not create a query from \n" + line);
                 }
             });

        // sort on values, descending and return
        return freqMap.entrySet()
                      .stream()
                      .sorted((c1, c2) -> c2.getValue().compareTo(c1.getValue()))
                      .collect(Collectors.toMap(
                              Map.Entry::getKey,
                              Map.Entry::getValue,
                              (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }

    /**
     * Get all the predicates of a JENA Query.
     *
     * @param q the query
     * @return a list of predicates
     */
    public static List<String> allQueryPredicates(Query q) {
        Element graphPattern = q.getQueryPattern();

        List<String> triples = Collections.synchronizedList(new ArrayList<>());
        ElementWalker.walk(graphPattern, new ElementVisitorBase() {

            @Override
            public void visit(ElementPathBlock el) {
                for (TriplePath triple : el.getPattern()) {
                    if (triple.getPath() == null) continue;
                    var pred = triple.getPath().toString();
                    if (!triples.contains(pred)) triples.add(pred);
                }
            }
        });
        return triples;
    }
}
