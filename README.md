# FastContext

FastContext is an optimized Java implementation of ConText algorithm (https://www.ncbi.nlm.nih.gov/pubmed/23920642). It runs two orders of magnitude faster and more accurate than previous two popluar implementations: [JavaConText](https://storage.googleapis.com/google-code-archive-downloads/v2/code.google.com/negex/JavaConText.zip) and [GeneralConText](https://storage.googleapis.com/google-code-archive-downloads/v2/code.google.com/negex/GeneralConText.Java.v.1.0_10272010.zip).

## Maven dependency set up
```xml
<dependency>
  <groupId>edu.utah.bmi.nlp</groupId>
  <artifactId>fastcontext</artifactId>
  <version>1.3.1.8</version>
</dependency>
```
Note: the maven distribution doesn't include the context rule file, you can download it [here](https://github.com/jianlins/FastContext/blob/master/conf/context.csv) if needed.
## Quick start
```java
// Initiate FastContext
FastContext fc = new FastContext("conf/context.csv");
String inputString = "The patient denied any fever , although he complained some headache .";
// To find the context information of "fever"
ArrayList<String> res = fc.processContext(inputString, 23, 28, 30);
```
For more detailed API uses, please refer to [TestFastContextAPIs.java](https://github.com/jianlins/FastContext/blob/master/src/test/java/edu/utah/bmi/nlp/fastcontext/TestFastContextAPIs.java)

## Acknowledgement
Special thanks to Olga Patterson and Guy Divita for contributing rules as part of the context rule set.

## Citation
If you are using FastContext within your research work, please cite the following publication:
``` 
Shi, Jianlin, and John F. Hurdle. “Trie-Based Rule Processing for Clinical NLP: A Use-Case Study of n-Trie, Making the ConText Algorithm More Efficient and Scalable.” Journal of Biomedical Informatics, August 6, 2018. https://doi.org/10.1016/j.jbi.2018.08.002.
```

Free full text are available at (for 50 days): 
https://authors.elsevier.com/a/1XXom5SMDQYjA8
