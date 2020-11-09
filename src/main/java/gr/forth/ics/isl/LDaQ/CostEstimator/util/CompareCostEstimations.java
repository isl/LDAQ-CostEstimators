package gr.forth.ics.isl.LDaQ.CostEstimator.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Just a script for comparing the accuracy of all {@link gr.forth.ics.isl.LDaQ.CostEstimator.costEstimationMethods}.
 *
 * @author Sklavos
 */
public class CompareCostEstimations {

    public static final NumberFormat nf = NumberFormat.getInstance(Locale.GERMAN);

    public static String compare(List<QueryWrapper> list) {
        final AtomicReference<Double> real = new AtomicReference<>((double) 0);
        final AtomicReference<Double> est1 = new AtomicReference<>((double) 0);
        final AtomicReference<Double> est2 = new AtomicReference<>((double) 0);
        final AtomicReference<Double> est3 = new AtomicReference<>((double) 0);
        final AtomicReference<Double> est4 = new AtomicReference<>((double) 0);

        list.forEach(qw -> {
            real.updateAndGet(v -> v + qw.realCost);
            est1.updateAndGet(v -> v + qw.cost1);
            est2.updateAndGet(v -> v + qw.cost2);
            est3.updateAndGet(v -> v + qw.cost3);
            est4.updateAndGet(v -> v + qw.cost4);
        });

        final StringBuilder sb = new StringBuilder();

        sb.append("real = ").append(nf.format(real.get())).append('\n');
        sb.append("cost1 = ").append(nf.format(est1.get())).append('\n');
        sb.append("cost2 = ").append(nf.format(est2.get())).append('\n');
        sb.append("cost3 = ").append(nf.format(est3.get())).append('\n');
        sb.append("cost4 = ").append(nf.format(est4.get())).append('\n');

        double cost1Pr = est1.get() / real.get();
        double cost2Pr = est2.get() / real.get();
        double cost3Pr = est3.get() / real.get();
        double cost4Pr = est4.get() / real.get();

        sb.append("cost1Pr = ").append(nf.format(cost1Pr)).append('\n');
        sb.append("cost2Pr = ").append(nf.format(cost2Pr)).append('\n');
        sb.append("cost3Pr = ").append(nf.format(cost3Pr)).append('\n');
        sb.append("cost4Pr = ").append(nf.format(cost4Pr));

        return sb.toString();
    }

    public static String compare(Path perQueryFolder, Path txtOutput) throws IOException {

        final List<QueryWrapper> wrapperList = Files.walk(perQueryFolder, 1)
                                                    .filter(file -> file.toString().contains("query_id"))
                                                    .map(QueryWrapper::readFromJson)
                                                    .filter(Optional::isPresent)
                                                    .map(Optional::get)
                                                    .collect(Collectors.toList());

        final String comparison = compare(wrapperList);

        try {
            if (Files.exists(txtOutput))
                Files.delete(txtOutput);
            Files.writeString(txtOutput, comparison);

            final Path back = txtOutput.getParent().resolveSibling(txtOutput.getFileName());
            if (Files.exists(back))
                Files.delete(back);
            Files.copy(txtOutput, back);
        } catch (IOException e) {
            System.err.println("could not write to " + txtOutput.toString());
        }
        return comparison;
    }
}
