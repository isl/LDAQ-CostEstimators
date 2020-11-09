package gr.forth.ics.isl.LDaQ.CostEstimator.util;

import org.apache.jena.query.QueryFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Extract correct queries from a log file (queries separated by '\n').
 *
 * @author Sklavos
 */
public class FilterLogFile {

    /**
     * Check if a string is a correct SPARQL query.
     *
     * @param queryStr a string representing a SPARQL query
     * @return true if query is correct and meets criteria
     */
    public static boolean isValidQuery(String queryStr) {
        try {
            QueryFactory.create(queryStr);
            final String q = queryStr.toUpperCase();
            final String[] invalid = new String[]{"UNION", "|", " / ", "^", "[", "]"};
            for (String inv : invalid)
                if (q.contains(inv)) return false;
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    /**
     * Filter a Query log file. <br>
     * Write correct queries (based on {@link #isValidQuery(String)}) to separate file.
     *
     * @param in  the log file
     * @param out the filtered log
     */
    public static void filterCorrectQueries(Path in, Path out) {
        try (BufferedReader br = new BufferedReader(new FileReader(in.toFile()))) {
            String line;
            int i = 0;
            while ((line = br.readLine()) != null) {
                i++;
                if (isValidQuery(line)) {
                    final String s = line.replaceAll("\\s+", " ") + "\n";
                    Files.writeString(out, s, StandardOpenOption.APPEND);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
