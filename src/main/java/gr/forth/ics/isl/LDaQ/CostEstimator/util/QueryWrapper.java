package gr.forth.ics.isl.LDaQ.CostEstimator.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Class used to organize LDaQ with execution data into files. <br>
 * Used mainly with Builder subclass (pattern from Effective Java by J.Bloch)
 *
 * @author Sklavos
 */
public final class QueryWrapper {
    public final int id;
    public final String query;
    public final String LDaQ;
    public final String pattern;
    public final double realCost;
    public final double cost1;
    public final double cost2;
    public final double cost3;
    public final double cost4;
    public final LocalDateTime timestamp;
    public final List<String> UrisAccessed;

    public QueryWrapper(int id, String query, String LDaQ, String pattern, double realCost, double cost1, double cost2,
                        double cost3, double cost4, LocalDateTime timestamp, List<String> urisAccessed) {
        this.id = id;
        this.query = query;
        this.LDaQ = LDaQ;
        this.pattern = pattern;
        this.realCost = realCost;
        this.cost1 = cost1;
        this.cost2 = cost2;
        this.cost3 = cost3;
        this.cost4 = cost4;
        this.timestamp = timestamp;
        UrisAccessed = urisAccessed;
    }

    public QueryWrapper(Builder builder) {
        this.id = builder.id;
        this.query = builder.query;
        this.LDaQ = builder.LDaQ;
        this.pattern = builder.pattern;
        this.realCost = builder.realCost;
        this.cost1 = builder.cost1;
        this.cost2 = builder.cost2;
        this.cost3 = builder.cost3;
        this.cost4 = builder.cost4;
        this.timestamp = builder.timestamp;
        this.UrisAccessed = builder.UrisAccessed;
    }

    public static Optional<QueryWrapper> readFromJson(Path json) {
        try {
            final String s = Files.readString(json);
            final QueryWrapper qw = new Gson().fromJson(s, QueryWrapper.class);
            return Optional.of(qw);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    /**
     * Serialize class into JSON format using Gson.
     *
     * @param folder the folder to write the .json file into.
     */
    public void writeJson(Path folder) {
        if (!Files.isDirectory(folder)) {
            throw new IllegalStateException("Argument is not a folder");
        }
        final String s = new GsonBuilder().setPrettyPrinting().create().toJson(this);
        final Path filename = folder.resolve("query_id_" + id + ".json");

        try {
            Files.writeString(filename, s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueryWrapper that = (QueryWrapper) o;
        return id == that.id &&
               Double.compare(that.realCost, realCost) == 0 &&
               Double.compare(that.cost1, cost1) == 0 &&
               Double.compare(that.cost2, cost2) == 0 &&
               Double.compare(that.cost3, cost3) == 0 &&
               query.equals(that.query) &&
               LDaQ.equals(that.LDaQ) &&
               pattern.equals(that.pattern) &&
               timestamp.equals(that.timestamp) &&
               Objects.equals(UrisAccessed, that.UrisAccessed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, query, LDaQ, pattern, realCost, cost1, cost2, cost3, timestamp, UrisAccessed);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(id).append('\n');
        sb.append(query).append('\n');
        sb.append(LDaQ).append('\n');
        sb.append(pattern).append('\n');
        sb.append(realCost).append('\n');
        sb.append(cost1).append('\n');
        sb.append(cost2).append('\n');
        sb.append(cost3).append('\n');
        sb.append(timestamp).append('\n');
        if (UrisAccessed != null)
            UrisAccessed.forEach(s -> sb.append(s).append('\n'));
        return sb.toString();
    }

    /**
     * Helper class meant to be used instead of constructor. <br>
     * Allows parameterized construction - pattern from Effective Java by J.Bloch
     */
    public static class Builder {
        // Required Parameter
        private final String query;
        // Optional Parameters
        private int id;
        private double realCost = -2;
        private LocalDateTime timestamp = LocalDateTime.MIN;
        private List<String> UrisAccessed;
        private String LDaQ = "--LDaQ--";
        private String pattern = "--pattern--";
        private double cost1 = -2;
        private double cost2 = -2;
        private double cost3 = -2;
        private double cost4 = -2;

        public Builder(int id, String query) {
            this.id = id;
            this.query = query;
        }

        public Builder(QueryWrapper qw) {
            id = qw.id;
            query = qw.query;
            realCost = qw.realCost;
            timestamp = qw.timestamp;
            UrisAccessed = qw.UrisAccessed;
            LDaQ = qw.LDaQ;
            pattern = qw.pattern;
            cost1 = qw.cost1;
            cost2 = qw.cost2;
            cost3 = qw.cost3;
        }

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder urisAccessed(List<String> urisAccessed) {
            this.UrisAccessed = urisAccessed;
            return this;
        }

        public Builder ldaq(String LDaQ) {
            this.LDaQ = LDaQ;
            return this;
        }

        public Builder pattern(String pattern) {
            this.pattern = pattern;
            return this;
        }

        public Builder realCost(double realCost) {
            this.realCost = realCost;
            return this;
        }

        public Builder cost1(double cost1) {
            this.cost1 = cost1;
            return this;
        }

        public Builder cost2(double cost2) {
            this.cost2 = cost2;
            return this;
        }

        public Builder cost3(double cost3) {
            this.cost3 = cost3;
            return this;
        }

        public Builder cost4(double cost4) {
            this.cost4 = cost4;
            return this;
        }

        public QueryWrapper build() {
            return new QueryWrapper(this);
        }
    }
}
