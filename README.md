# FastContext

FastContext is an optimized Java implementation of ConText algorithm (https://www.ncbi.nlm.nih.gov/pubmed/23920642). It runs two orders of magnitude faster and more accurate than previous two popluar implementations: [JavaConText](https://storage.googleapis.com/google-code-archive-downloads/v2/code.google.com/negex/JavaConText.zip) and [GeneralConText](https://storage.googleapis.com/google-code-archive-downloads/v2/code.google.com/negex/GeneralConText.Java.v.1.0_10272010.zip).

## Maven dependency set up
```xml
<dependency>
  <groupId>edu.utah.bmi.nlp</groupId>
  <artifactId>fastcontext</artifactId>
  <version>2.0</version>
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
