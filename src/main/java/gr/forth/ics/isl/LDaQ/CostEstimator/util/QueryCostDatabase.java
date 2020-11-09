package gr.forth.ics.isl.LDaQ.CostEstimator.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Build a Database file from {@link QueryWrapper} files.
 *
 * @author Sklavos
 */
public final class QueryCostDatabase {
    /**
     * The header for the TSV file contaning the generated database. <br>
     * Useful with Spreadsheets.  <br>
     * !Make sure # of columns == db columns
     */
    public static final String HEADER = TSV.concat("ID",
                                                   "Query",
                                                   "realCost",
                                                   "noKnowledgeCost",
                                                   "predicatesDataCost",
                                                   "joins",
                                                   "triplesWithFilter");


    /**
     * Build a Database file from {@link QueryWrapper} files.
     *
     * @param txtOutput The .tsv database file to write to
     * @param in        The folder containing the {@link QueryWrapper} files to build the database from
     * @throws IOException if read/write fails
     */
    public static void build(Path in, Path txtOutput) throws IOException {

        // Treemap in order to have lines in database in ascending order of QueryWrapper id's.
        final Map<Integer, String> map = Collections.synchronizedMap(new TreeMap<>());
        final AtomicInteger i = new AtomicInteger(1);

        Files.walk(in, 1)
             .filter(file -> file.toString().contains("query_id"))
             .map(QueryWrapper::readFromJson)
             .filter(Optional::isPresent)
             .map(Optional::get)
             .forEach(qw -> {
                 i.getAndIncrement();
                 final String dbLine = TSV.concat(qw.id, qw.query, qw.realCost, qw.cost1, qw.cost2, qw.cost3, qw.cost4);
                 map.put(qw.id, dbLine);
             });

        final List<String> dbLines = new ArrayList<>(map.values());
        dbLines.add(0, HEADER);
        Files.write(txtOutput, dbLines);
        System.out.println("queries written in db -> " + i);
    }
}
