package gr.forth.ics.isl.LDaQ.CostEstimator.util;

/**
 * Deal with TSV formatting
 *
 * @author Sklavos
 */
public final class TSV {

    /**
     * @param items items implementing toString
     * @return a valid tsv line containing the String representation of the items
     */
    @SafeVarargs
    public static <T> String concat(T... items) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.length; i++) {
            sb.append(items[i].toString());
            if (i + 1 != items.length)
                sb.append('\t');
        }
        return sb.toString();
    }

    /**
     * Split a tsv line into a String[]
     *
     * @param csvLine tab-separated String
     * @return split string values from input line
     */
    public static String[] split(String csvLine) {
        return csvLine.split("\t");
    }
}
