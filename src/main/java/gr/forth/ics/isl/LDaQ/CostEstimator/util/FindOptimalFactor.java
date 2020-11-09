package gr.forth.ics.isl.LDaQ.CostEstimator.util;

import gr.forth.ics.isl.LDaQ.CostEstimator.costEstimationMethods.Method3_starShapedJoin;
import gr.forth.ics.isl.LDaQ.CostEstimator.costEstimationMethods.Method4_filter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Find the optimal factors of {@link Method3_starShapedJoin} or {@link Method4_filter}. <br>
 * Was used on biased sets.
 *
 * @author Sklavos
 */
public class FindOptimalFactor {

    /**
     * Apply {@link Method3_starShapedJoin} with a factor in a given set and get its comparison to the real cost
     *
     * @param list   a list o {@link QueryWrapper} files to apply {@link Method3_starShapedJoin} estimation to
     * @param factor a factor for {@link Method3_starShapedJoin} applied to all estimations
     * @return {@link Method3_starShapedJoin} cost comparison
     */
    public static String method3(List<QueryWrapper> list, double factor) {

        final List<QueryWrapper> l = Collections.synchronizedList(new ArrayList<>());

        list.forEach(qw -> {
            try {
                final double estimate = new Method3_starShapedJoin().chainFactor(factor).estimate(qw.query);
                l.add(new QueryWrapper.Builder(qw).cost3(estimate).build());
            } catch (Exception ignored) {
            }
        });

        return CompareCostEstimations.compare(l);
    }

    /**
     * Apply {@link Method4_filter} with a factor in a given set and get its comparison to the real cost
     *
     * @param list   a list o {@link QueryWrapper} files to apply {@link Method4_filter} estimation to
     * @param factor a factor for {@link Method4_filter} applied to all estimations
     * @return {@link Method4_filter} cost comparison
     */
    public static String method4(List<QueryWrapper> list, double factor) {

        final List<QueryWrapper> l = Collections.synchronizedList(new ArrayList<>());

        list.forEach(qw -> {
            try {
                final double estimate = new Method4_filter().filterFactor(factor).estimate(qw.query);
                l.add(new QueryWrapper.Builder(qw).cost4(estimate).build());
            } catch (Exception ignored) {
            }
        });

        return CompareCostEstimations.compare(l);
    }

    /**
     * Read a folder of {@link QueryWrapper} files into a list.
     * @param in the folder
     * @return A list af {@link QueryWrapper}
     */
    public static List<QueryWrapper> readFolder(Path in) throws IOException {
        return Files.walk(in, 1)
                    .filter(file -> file.toString().contains("query_id")) // not necessary because of optional
                    .map(QueryWrapper::readFromJson)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
    }


    /**
     * A script for checking a list of factors on a list of {@link QueryWrapper}.
     *
     * @param method either {@link #method3(List, double)} or {@link #method4(List, double)}
     * @param qwList the list of {@link QueryWrapper} - use {@link #readFolder(Path)}
     * @param factList a list of all the factors for either method 3 or method 4 to check.
     * @return for every factor, the {@link CompareCostEstimations#compare(List)}
     */
    public static Map<Double, String> applyMethod(BiFunction<List<QueryWrapper>, Double, String> method,
                                                  List<QueryWrapper> qwList, List<Double> factList) {
        final Map<Double, String> map = new TreeMap<>();

        for (Double f : factList) {
            map.put(f, method.apply(qwList, f));
        }

        return map;
    }

    /**
     * Example usage
     */
    public static void main(String[] args) throws IOException {

        final Path folder = Path.of("...");

        List<QueryWrapper> qwList = readFolder(folder);
        final List<Double> factors = List.of(0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9);

        final var map = applyMethod(FindOptimalFactor::method4, qwList, factors);

        final StringBuffer sb = new StringBuffer("biased_m4\n");

        map.forEach((k, v) -> {
            sb.append("filterFactor -> ").append(k).append('\n');
            sb.append(v).append('\n');
        });

        Files.writeString(folder.resolve("..."), sb.toString());
    }
}
