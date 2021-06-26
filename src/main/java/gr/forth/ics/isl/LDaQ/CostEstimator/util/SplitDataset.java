package gr.forth.ics.isl.LDaQ.CostEstimator.util;

import gr.forth.ics.isl.LDaQ.CostEstimator.costEstimationMethods.Method3_starShapedJoin;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * The methods we used to split our dataset to biased/unbiased and subsequently calculate the accuracy of our methods.
 *
 * @author Sklavos
 */
public class SplitDataset {

    /**
     * Randomly divide in two the {@link QueryWrapper} files in the folder given as input. <br>
     * We put copies of the files in folders "biased" or "unbiased".
     *
     * @param in the folder with all the queries we split
     * @throws IOException
     */
    public static void split(Path in) throws IOException {
        final List<QueryWrapper> qwList = Files.walk(in)
                                               .filter(file -> file.toString().contains("query_id"))
                                               .map(QueryWrapper::readFromJson)
                                               .filter(Optional::isPresent)
                                               .map(Optional::get)
                                               .collect(Collectors.toList());

        Collections.shuffle(qwList);

        final Path biased = in.resolveSibling("biased");
        final Path unbiased = in.resolveSibling("unbiased");

        if (!Files.isDirectory(biased)) Files.createDirectory(biased);
        if (!Files.isDirectory(unbiased)) Files.createDirectory(unbiased);

        int size = qwList.size();

        final List<QueryWrapper> biasedList = qwList.subList(0, size / 2);
        final List<QueryWrapper> unbiasedList = qwList.subList(size / 2, size);

        biasedList.forEach(qw -> qw.writeJson(biased));

        unbiasedList.forEach(qw -> qw.writeJson(unbiased));
    }

    /**
     * Copy all the {@link QueryWrapper} files in folder "in" that can take advantage of <br>
     * the third cost estimation method (queries with star-shaped joins)
     *
     * @param in        the folder with all the queries we split
     * @param dstFolder the output folder
     * @throws IOException
     */
    public static void method3Applies(Path in, final Path dstFolder) throws IOException {

        if (!Files.isDirectory(dstFolder)) Files.createDirectory(dstFolder);

        Files.walk(in)
             .filter(file -> file.toString().contains("query_id"))
             .forEach(file -> {
                 final QueryWrapper qw = QueryWrapper.readFromJson(file).orElseThrow();
                 final Query query = QueryFactory.create(qw.query);
                 final int size = Method3_starShapedJoin.getTriplesInStar(query).size();

                 if (size != 0) {
                     qw.writeJson(dstFolder);
                 }
             });
    }


    /**
     * Copy all the {@link QueryWrapper} files in folder "in" that can take advantage of <br>
     * the fourth cost estimation method (queries with filter clauses)
     *
     * @param in        the folder with all the queries we split
     * @param dstFolder the output folder
     * @throws IOException
     */
    public static void method4Applies(Path in, final Path dstFolder) throws IOException {
        if (!Files.isDirectory(dstFolder)) Files.createDirectory(dstFolder);

        Files.walk(in)
             .filter(file -> file.toString().contains("query_id"))
             .forEach(file -> {
                 final QueryWrapper qw = QueryWrapper.readFromJson(file).orElseThrow();

                 if (qw.query.toUpperCase().contains("FILTER")) {
                     qw.writeJson(dstFolder);
                 }
             });
    }

    /**
     * Copy all the {@link QueryWrapper} files in folder "in" that can take advantage of <br>
     * both our 3rd and 4th cost estimation methods (queries with star-shaped joins AND filter clauses)
     *
     * @param in        the folder with all the queries we split
     * @param dstFolder the output folder
     * @throws IOException
     */
    public static void method4Strict(Path in, final Path dstFolder) throws IOException {
        final AtomicInteger err = new AtomicInteger();

        if (!Files.isDirectory(dstFolder)) Files.createDirectory(dstFolder);

        Files.walk(in)
             .filter(file -> file.toString().contains("query_id"))
             .forEach(file -> {
                 final QueryWrapper qw = QueryWrapper.readFromJson(file).orElseThrow();
                 try {
                     final Query q = QueryFactory.create(qw.query);
                     final int size = Method3_starShapedJoin.getTriplesInStar(q).size();
                     if (size != 0 && qw.query.toUpperCase().contains("FILTER"))
                         qw.writeJson(dstFolder);

                 } catch (Exception e) {
                     err.getAndIncrement();
                 }

             });

        System.out.println("errors -> " + err.get());
    }

    /**
     * Write a txt file giving an overview of the amount of files in each folder
     * (as calculated using surrouding java methods)
     *
     * @param in      the folder with all the queries we split
     * @param outFile the overview goes here
     * @throws IOException
     */
    public static void overview(final Path in, final Path outFile) throws IOException {
        long biased =
                Files.walk(in.resolve("biased")).filter(f -> f.toString().contains(".json")).count();
        long biased_m3 =
                Files.walk(in.resolve("biased_m3")).filter(f -> f.toString().contains(".json")).count();
        long biased_m4 =
                Files.walk(in.resolve("biased_m4")).filter(f -> f.toString().contains(".json")).count();
        long biased_m4_strict =
                Files.walk(in.resolve("biased_m4_strict")).filter(f -> f.toString().contains(".json")).count();
        long unbiased =
                Files.walk(in.resolve("unbiased")).filter(f -> f.toString().contains(".json")).count();
        long unbiased_m3 =
                Files.walk(in.resolve("unbiased_m3")).filter(f -> f.toString().contains(".json")).count();
        long unbiased_m4 =
                Files.walk(in.resolve("unbiased_m4")).filter(f -> f.toString().contains(".json")).count();
        long unbiased_m4_strict =
                Files.walk(in.resolve("unbiased_m4_strict")).filter(f -> f.toString().contains(".json")).count();

        final String sb = (biased + unbiased) + "\tqueries in total" + '\n' +
                          biased + "\t biased queries" + '\n' +
                          unbiased + "\t unbiased queries" + '\n' +
                          biased_m3 + "\t biased_m3 queries" + '\n' +
                          unbiased_m3 + "\t unbiased_m3 queries" + '\n' +
                          biased_m4 + "\t biased_m4 queries" + '\n' +
                          unbiased_m4 + "\t unbiased_m4 queries" + '\n' +
                          biased_m4_strict + "\t biased_m4_strict queries" + '\n' +
                          unbiased_m4_strict + "\t unbiased_m4_strict queries" + '\n';
        Files.writeString(outFile, sb);
    }

    // public static void main(String[] args) {
    //     try {
    //         // split(Path.of("/Users/ansk/Downloads/thesis/all_queries"));
    //         method3Applies(Path.of("/Users/ansk/Downloads/thesis/biased"),
    //                        Path.of("/Users/ansk/Downloads/thesis/biased_m3"));
    //         method3Applies(Path.of("/Users/ansk/Downloads/thesis/unbiased"),
    //                        Path.of("/Users/ansk/Downloads/thesis/unbiased_m3"));
    //         method3Applies(Path.of("/Users/ansk/Downloads/thesis/biased"),
    //                        Path.of("/Users/ansk/Downloads/thesis/biased_m4"));
    //         method3Applies(Path.of("/Users/ansk/Downloads/thesis/unbiased"),
    //                        Path.of("/Users/ansk/Downloads/thesis/unbiased_m4"));
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }
    //
    // }
}
