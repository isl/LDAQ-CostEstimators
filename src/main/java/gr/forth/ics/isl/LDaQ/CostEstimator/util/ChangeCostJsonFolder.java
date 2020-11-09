package gr.forth.ics.isl.LDaQ.CostEstimator.util;

import gr.forth.ics.isl.LDaQ.CostEstimator.costEstimationMethods.Method3_starShapedJoin;
import gr.forth.ics.isl.LDaQ.CostEstimator.costEstimationMethods.Method4_filter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Change the cost of all .json {@link QueryWrapper} files.
 *
 * @author Sklavos
 */
public class ChangeCostJsonFolder {

    public static void changeCost(Path in, boolean change3, boolean change4) throws IOException {
        final List<QueryWrapper> toWrite = Collections.synchronizedList(new ArrayList<>());

        final AtomicInteger queriesConsidered = new AtomicInteger();
        final AtomicInteger errors = new AtomicInteger();

        final List<QueryWrapper> list = Files.walk(in)
                                             .filter(file -> file.toString().contains("query_id"))
                                             .map(QueryWrapper::readFromJson)
                                             .filter(Optional::isPresent)
                                             .map(Optional::get).collect(Collectors.toList());

        list.forEach(qw -> {
            queriesConsidered.getAndIncrement();
            try {
                double cost3 = qw.cost3;
                double cost4 = qw.cost4;

                if (change3) {
                    cost3 = new Method3_starShapedJoin().estimate(qw.query);
                }
                if (change4) {
                    cost4 = new Method4_filter().estimate(qw.query);
                }
                toWrite.add(new QueryWrapper.Builder(qw).cost3(cost3).cost4(cost4).build());
            } catch (Exception e) {
                errors.incrementAndGet();
            }
        });

        toWrite.forEach(qw -> qw.writeJson(in));

        System.out.println("queriesConsidered = " + queriesConsidered);
        System.out.println("errors = " + errors);
    }
}
