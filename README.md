# R-Star-Tree
A Java implementation of the R*-Tree, for indexing multi-dimensional data.

## About
The data structure of the implemented index file is the R*-tree (R Star Tree), which is an improved implementation of the original R-tree. The R*-tree (like the original R-tree) is a self-balanced tree which holds and processes spatial data of n-dimensions.

In this work we have dealt with the organization and processing of spatial data. The aim of this work was to build a secondary memory data structure that can organize multidimensional data and support the implementation of range queries and k-nearest neighbors queries.

## Main Implementations
- R*-Tree
- Range queries (within a circle's radius)
- Range queries (within rectangle's area)
- Κ-nearest neighbors queries
- Queries comparison between the R*-Tree and the Sequential Scan

## Characteristics of the R*-tree and differences compared to the original R-tree
The main difference between the R*-tree and the original R-tree, is that during the insertions of records in the R*-tree, in addition to minimizing the areas of the rectangles, other parameters are taken into account, such as the overlap between each other, the perimeter and the use of storage space (storage utilization). So with the utilization of the mentioned parameters, the tree will be constructed in such a way that there will not be much overlap between the rectangles, to separate-group the data in a better way, thus improving the performance of the queries. 

For pages of M objects, the process of insertion in the R*-tree is done with O (MlogM), where it is more complicated than the linear separation O(M) of the simple R-tree, but less complicated than the quadratic separation strategy O(M^2) and it only has a little effect on overall complexity. The overall complexity for insertions is still comparable to the R-tree, as re-inserts affect at most one branch of the tree, resulting in a maximum of O (NlogN) re-inserts, comparable to performing splitting on a regular R-tree. Thus, overall, the complexity of the R*-tree is similar to that of a R-tree. R*-trees have slightly higher construction costs than standard R-trees, as data may need to be re-inserted. However, the resulting tree usually performs better in queries.

## Data used
The data.csv file which has the data of each node, contains approximately 900,000 different points (real geographical data extracted from https://www.openstreetmap.org) that were used to create the included datafile and indexfile.

## Queries comparison between the R*-Tree and the Sequential Scan
Below are indicative graphs showing execution times as the area of interest for range queries grows and k increases for nearest neighbors queries, as well as comparisons with sequential scanning on the large number of the real data, which was extracted from https://www.openstreetmap.org/.
These graphs are generated from the tables created by the RunMultiple2DQueries.java class, which runs with the included data and index files. These tables are located in the included .csv files

![Image description](/images-readme/Range-query-circle.png)
![Image description](/images-readme/Range-query-rectangle.png)
![Image description](/images-readme/K-nearest-neighbors-query.png)

## References:
- Beckmann, N., Kriegel, H. P., Schneider, R., & Seeger, B. (1990). The R*-tree: an efficient and robust access method for points and rectangles. Proceedings of the 1990 ACM SIGMOD international conference on Management of data - SIGMOD '90.
- Guttman, A. (1984). R-Trees: A Dynamic Index Structure for Spatial Searching. Proceedings of the 1984 ACM SIGMOD international conference on Management of data - SIGMOD '84.
- Roussopoulos, N., Kelley, S., & Vincent, F. D. (1995). Nearest neighbor queries. Proceedings of the 1995 ACM SIGMOD international conference on Management of data – SIGMOD '95.
