# Estimating the Cost of Link Traversal-based Execution of SPARQL Queries

This library allows the user to estimate the execution cost of a
[SPARQL-LD](https://github.com/anskl/sparql-ld) query.\
Given a SPARQL query, the user can:
1. transform it to its equivalent SPARQL-LD (possible in most cases) using
   [Transform.java](src/main/java/gr/forth/ics/isl/LDaQ/CostEstimator/ldaq/Transform.java).
2. Estimate its execution cost using one of our
   [methods](src/main/java/gr/forth/ics/isl/LDaQ/CostEstimator/costEstimationMethods).
3. Decide whether to proceed with the link traversal-based execution.


## Brief description of our four cost estimation methods.
Each method below extends its previous method.
1. [Method 1 - No prior knowledge](src/main/java/gr/forth/ics/isl/LDaQ/CostEstimator/costEstimationMethods/Method1_noKnowledgeCostEst.java) \
    Estimate the query cost execution without assuming anything about
    its content (e.g. known predicates).
2. [Method 2 - Known predicate bindings](src/main/java/gr/forth/ics/isl/LDaQ/CostEstimator/costEstimationMethods/Method2_predicatesDataCostEst.java) \
    Estimate the query cost execution by taking into consideration known
    predicates with their average object and subject bindings.
3. [Method 3 - Star-shaped Joins](src/main/java/gr/forth/ics/isl/LDaQ/CostEstimator/costEstimationMethods/Method3_starShapedJoin.java) \
    Estimate the query cost execution by extending Method 2 and taking
    into consideration cost limiting star-shaped joins.
4. [Method 4 - Filters](src/main/java/gr/forth/ics/isl/LDaQ/CostEstimator/costEstimationMethods/Method4_filter.java) \
    **Our most complete method** \
    Estimate the query cost execution by extending Method 3, in addition to
    considering SPARQL filter clauses.


## Brief description of JAVA packages and classes:

### costEstimationMethods package
All 4 cost estimation methods as described above.

### ldaq package
Code for checking whether a SPARQL query is linked-data answerable,
transforming it to its equivalent SPARQL-LD etc. \
See [LDaQ](https://github.com/fafalios/LDaQ).

### predicateStats package
Mainly used for [Method 2](src/main/java/gr/forth/ics/isl/LDaQ/CostEstimator/costEstimationMethods/Method2_predicatesDataCostEst.java). \
Code we used to work with predicates such as getting their average subject bindings.

### util package
**Scripts and helpful code used in our work.**
* [BindPair.java](src/main/java/gr/forth/ics/isl/LDaQ/CostEstimator/util/BindPair.java)
    A Simple pair of doubles.
* [ChangeCostJsonFolder.java](src/main/java/gr/forth/ics/isl/LDaQ/CostEstimator/util/ChangeCostJsonFolder.java)
    Script for changing the values of a folder of [QueryWrapper.java](src/main/java/gr/forth/ics/isl/LDaQ/CostEstimator/util/QueryWrapper.java)
    files, like our [ground_truth folder](dataset/ground_truth/all_queries.7z).
* [CompareCostEstimations.java](src/main/java/gr/forth/ics/isl/LDaQ/CostEstimator/util/CompareCostEstimations.java)
    Compare all methods in package [costEstimationMethods](src/main/java/gr/forth/ics/isl/LDaQ/CostEstimator/costEstimationMethods).
* [FilterLogFile.java](src/main/java/gr/forth/ics/isl/LDaQ/CostEstimator/util/FilterLogFile.java)
    Deal with big log file of SPARQL queries.
* [FindOptimalFactor.java](src/main/java/gr/forth/ics/isl/LDaQ/CostEstimator/util/FindOptimalFactor.java)
    Find the optimal factor of [Method 3](src/main/java/gr/forth/ics/isl/LDaQ/CostEstimator/costEstimationMethods/Method3_starShapedJoin.java) or
    [Method 4](src/main/java/gr/forth/ics/isl/LDaQ/CostEstimator/costEstimationMethods/Method4_filter.java).
* [QueryCostDatabase.java](src/main/java/gr/forth/ics/isl/LDaQ/CostEstimator/util/QueryCostDatabase.java)
    Build a database from [QueryWrapper.java](src/main/java/gr/forth/ics/isl/LDaQ/CostEstimator/util/QueryWrapper.java) files.
* [QueryWrapper.java](src/main/java/gr/forth/ics/isl/LDaQ/CostEstimator/util/QueryWrapper.java)
    Store interesting data relating to a Query, its execution and cost estimating methods.
* [SplitDataset.java](src/main/java/gr/forth/ics/isl/LDaQ/CostEstimator/util/SplitDataset.java)
    What we used to split the dataset into two in order to base optimizations on one half
    and run tests on the other avoiding any biases.
* [TSV.java](src/main/java/gr/forth/ics/isl/LDaQ/CostEstimator/util/TSV.java)
    Code for dealing with tsv formatting.


## Dataset Description
**We provide a complete dataset, with results and our ground-truth**
* ground_truth folder contains:
  * [all_queries.7z](dataset/ground_truth/all_queries.7z) All the queries we gathered, in .json files.
   Each .json file is serialized in [QueryWrapper.java](src/main/java/gr/forth/ics/isl/LDaQ/CostEstimator/util/QueryWrapper.java) and contains the following fields:
     1. a unique query id
     2. the SPARQL query
     3. its equivalent SPARQL-LD query
     4. the query pattern
     5. the real cost we got when executing the query (see timestamp below)
     6. the method 1 cost estimation
     7. the method 2 cost estimation
     8. the method 3 cost estimation
     9. the method 4 cost estimation
     10. the timestamp of our real cost execution
     11. a list of all the uris that were access during the execution

  * [ground_truth.tsv](dataset/ground_truth/ground_truth.tsv) a tab-separated file containing all queries. Each line represents a
  different query and contains the following:
    1. a unique query id
    2. the SPARQL query
    3. Its equivalent SPARQL-LD query
    4. The real cost

* results

**We calculated the accuracy of our methods using different queries (non-biased) from the ones we optimized the methods for (biased).** 

Each file below contains the following columns:
1. a unique query id
2. the SPARQL query
3. The estimated cost of [Method 1](src/main/java/gr/forth/ics/isl/LDaQ/CostEstimator/costEstimationMethods/Method1_noKnowledgeCostEst.java)
4. The estimated cost of [Method 2](src/main/java/gr/forth/ics/isl/LDaQ/CostEstimator/costEstimationMethods/Method2_predicatesDataCostEst.java)
5. The estimated cost of [Method 3](src/main/java/gr/forth/ics/isl/LDaQ/CostEstimator/costEstimationMethods/Method3_starShapedJoin.java)
6. The estimated cost of [Method 4](src/main/java/gr/forth/ics/isl/LDaQ/CostEstimator/costEstimationMethods/Method4_filter.java)

The folder contains the following files:
  * [QueryCostDatabase_OnlyMethod3Queries.tsv](dataset/results/QueryCostDatabase_OnlyMethod3Queries.tsv) Accuracy of
        [Method 3](src/main/java/gr/forth/ics/isl/LDaQ/CostEstimator/costEstimationMethods/Method3_starShapedJoin.java) on queries with star-shaped joins.
  * [QueryCostDatabase_OnlyMethod4Queries.tsv](dataset/results/QueryCostDatabase_OnlyMethod4Queries.tsv) Accuracy of
        [Method 4](src/main/java/gr/forth/ics/isl/LDaQ/CostEstimator/costEstimationMethods/Method4_filter.java) on queries with filters.
  * [QueryCostDatabase_OnlyMethod4Queries_strict.tsv](dataset/results/QueryCostDatabase_OnlyMethod4Queries_strict.tsv) Accuracy of
        [Method 4](src/main/java/gr/forth/ics/isl/LDaQ/CostEstimator/costEstimationMethods/Method4_filter.java) on queries with filter clauses AND
            star-shaped joins.
  * [QueryCostDatabase.tsv](dataset/results/QueryCostDatabase.tsv) Comparing all of our methods on our non-biased queries.


## External Dependencies
* [Apache Jena 3.16.0](https://search.maven.org/artifact/org.apache.jena/jena-arq/3.16.0/jar) Work with SPARQL queries
* [Json](https://search.maven.org/artifact/org.json/json/20200518/bundle) Build Json objects, used in
      [DbpediaPredicateAverageHttp.java](src/main/java/gr/forth/ics/isl/LDaQ/CostEstimator/predicateStats/DbpediaPredicateAverageHttp.java)
* [Gson]() Serialize Java objects into json files, used in [QueryWrapper.java](src/main/java/gr/forth/ics/isl/LDaQ/CostEstimator/util/QueryWrapper.java)


## Acknowledgements

This work has received funding from the European Union's Horizon 2020 research and innovation programme under the Marie Sklodowska-Curie grant agreement No 890861 (<a href="https://reknow.ics.forth.gr/">Project ReKnow</a>). 
