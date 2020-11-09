package gr.forth.ics.isl.LDaQ.CostEstimator.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Simple class storing a pair of doubles representing the average object and subject bindings of a predicate. <br>
 * Is meant to store the data of textFiles/knownPredicatesBindings
 *
 * @author Sklavos
 */
public final class BindPair {
    public final double avgObjBind;
    public final double avgSubjBind;

    public BindPair(double avgObjBind, double avgSubjBind) {
        this.avgObjBind = avgObjBind;
        this.avgSubjBind = avgSubjBind;
    }

    /**
     * Read a file of predicate with their corresponding average object and subject bindings.
     * {@link gr.forth.ics.isl.LDaQ.CostEstimator.costEstimationMethods.Method2_predicatesDataCostEst} uses this to load the known predicate
     * bindings.
     *
     * @param fileLocation input, such astextFiles/knownPredicatesBindings.csv
     */
    public static Map<String, BindPair> getBindings(Path fileLocation) {
        final Map<String, BindPair> map = Collections.synchronizedMap(new HashMap<>());

        try (Stream<String> a = Files.lines(fileLocation)) {
            a.skip(1) // skip header
             .map(l -> l.split(","))
             .forEach(split -> {
                 // remove csv quotes, "<" and ">"
                 final String pred = split[0].replaceAll("[\"<>]", "");
                 final double avgObjBind = Double.parseDouble(split[1]);
                 final double avgSubjBind = Double.parseDouble(split[2]);

                 map.put(pred, new BindPair(avgObjBind, avgSubjBind));
             });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BindPair bindPair = (BindPair) o;
        return Double.compare(bindPair.avgObjBind, avgObjBind) == 0 &&
               Double.compare(bindPair.avgSubjBind, avgSubjBind) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(avgObjBind, avgSubjBind);
    }
}
