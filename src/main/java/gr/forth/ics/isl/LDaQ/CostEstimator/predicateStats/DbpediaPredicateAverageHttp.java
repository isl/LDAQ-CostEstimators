package gr.forth.ics.isl.LDaQ.CostEstimator.predicateStats;

import org.json.JSONArray;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.util.HashMap;

/**
 * Calculate the average object and subject bindings of a given predicate on dbpedia. <br>
 * Uses HTTP requests. <br>
 * Similar functionality to {@link DbpediaPredicateAverage}
 *
 * @author Sklavos
 */
public final class DbpediaPredicateAverageHttp {

    final String predicate;

    public DbpediaPredicateAverageHttp(final String predicate) {
        this.predicate = predicate;
    }

    private static double jsonAvg(JSONArray jsonArray) {

        int sum = 0;
        int count = 0;
        try {
            var t = jsonArray.getJSONObject(0).getJSONObject("results").get("bindings");
            var list = ((JSONArray) t).toList();
            for (var x : list) {
                String value = (String) ((HashMap) ((HashMap) x).get("count")).get("value");

                int bindings = Integer.parseInt(value);
                sum += bindings;
                count++;
            }
        } catch (Exception e) {
            System.out.println("Exception in jsonAvg");
            return -1;
        }

        return (count == 0) ? 0 : (double) sum / count;
    }

    private JSONArray dbpediaRequest(String query) {
        final String encodedRequest =
                "http://dbpedia.org/sparql/?default-graph-uri=http%3A%2F%2Fdbpedia.org&query=" +
                URLEncoder.encode(query, Charset.defaultCharset()) +
                "&format=application%2Fsparql-results%2Bjson&CXML_redir_for_subjs=121&CXML_redir_for_hrefs=" +
                "&timeout=30000&debug=on&run=+Run+Query+";

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                                         .uri(URI.create(encodedRequest))
                                         .GET()
                                         .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return new JSONArray('[' + response.body() + ']');
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public double avgObjBindings() {
        String query = "SELECT ?s (COUNT(?o) AS ?count) WHERE { ?s " + predicate + " ?o } GROUP BY ?s";

        JSONArray jsonArray = dbpediaRequest(query);

        return (jsonArray != null) ? jsonAvg(jsonArray) : -1;
    }

    public double avgSubjBindings() {
        String query = "SELECT (COUNT(?o) AS ?count) ?o WHERE { ?s " + predicate + " ?o } GROUP BY ?o";

        JSONArray jsonArray = dbpediaRequest(query);

        return (jsonArray != null) ? jsonAvg(jsonArray) : -1;
    }
}
