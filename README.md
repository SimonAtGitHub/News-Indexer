News-Indexer
============

Implement a news reader that ranks the articles and displays results according to the relevancy of the query. Uses both tf-idf 
and okapi model to rank the articles based on relevancy.

1. Run the 'Runner.java' file with 2 arguments, namely: 1) Directory where the news articles resides 2) Directory where the 
indexed files should be saved.

2. Create an object for 'SearchRunner' class created in 'SearchRunner.java' with arguments to the constructor as 1) Directory 
where the indexed files are created as a result of Step 1. 2) Directory where the news articles resides 3) mode of the output 
(q or e) - means either it gives the result in a stream or in a file respectively 4) stream - can be for a file or an UI (depends
 on the mode)
 
 3. If query mode is required, then call 'query' member function in 'SearchRunner' class with the arguments as 1) user query as 
 'String' 2) ranking model - (tf-idf or okapi) Enum is given in 'ScoringModel.java'
 
 4. If evaluation mode is required then call 'query' member function in 'SearchRunner' class with user query as the only argument.
 
