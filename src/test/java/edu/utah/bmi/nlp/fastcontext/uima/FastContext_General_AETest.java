/*
 * ******************************************************************************
 *  * Copyright  2017  Department of Biomedical Informatics, University of Utah
 *  * <p>
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  * <p>
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  * <p>
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  ******************************************************************************
 */

package edu.utah.bmi.nlp.fastcontext.uima;

import edu.utah.bmi.nlp.core.DeterminantValueSet;
import edu.utah.bmi.nlp.core.TypeDefinition;
import edu.utah.bmi.nlp.type.system.Concept;
import edu.utah.bmi.nlp.type.system.Context;
import edu.utah.bmi.nlp.type.system.Sentence;
import edu.utah.bmi.nlp.uima.AdaptableUIMACPERunner;
import edu.utah.bmi.nlp.uima.ae.SimpleParser_AE;
import edu.utah.bmi.nlp.uima.common.AnnotationOper;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.AnnotationFactory;
import org.apache.uima.fit.util.FSUtil;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.jupiter.api.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;

/**
 * @author Jianlin Shi on 5/7/17.
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
public class FastContext_General_AETest {

    private AnalysisEngine fastContext_AE;
    private JCas jCas;
    private AdaptableUIMACPERunner runner;
    private AnalysisEngine simpleParser_AE;
    private String typeDescriptor = "desc/type/All_Types";

    @BeforeEach
    public void setUp() {
        runner = new AdaptableUIMACPERunner(typeDescriptor, "target/generated-test-sources/");

        runner.addConceptTypes(new FastContext_General_AE().getTypeDefs("conf/context.xlsx").values());
        runner.reInitTypeSystem("target/generated-test-sources/customized");
        jCas = runner.initJCas();
//      Set up the parameters
        Object[] configurationData = new Object[]{FastContext_General_AE.PARAM_RULE_STR, "conf/context.txt",
                FastContext_General_AE.PARAM_MARK_CLUE, true};
        try {
            fastContext_AE = createEngine(FastContext_General_AE.class,
                    configurationData);
            simpleParser_AE = createEngine(SimpleParser_AE.class, new Object[]{SimpleParser_AE.PARAM_INCLUDE_PUNCTUATION, true});
        } catch (ResourceInitializationException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test() throws AnalysisEngineProcessException {
        String text = "The patient Denies any problem with visual changes or hearing changes.";
        String targetWords = "visual changes";
        jCas.setDocumentText(text);
        simpleParser_AE.process(jCas);
        int begin = text.indexOf(targetWords);
        int end = begin + targetWords.length();
        Concept concept = new Concept(jCas, begin, end);
        concept.addToIndexes();
        fastContext_AE.process(jCas);
        Collection<Concept> targets = JCasUtil.select(jCas, Concept.class);
        for (Concept target : targets) {
            System.out.println(target.toString());
        }
        for (Context context : JCasUtil.select(jCas, Context.class)) {
            System.out.println(context.toString());
        }
    }

    @Test
    public void test2() throws AnalysisEngineProcessException {
        String text = "He history of smoking: no .";
        ;
        String targetWords = "smoking";
        jCas.setDocumentText(text);
        simpleParser_AE.process(jCas);
        int begin = text.indexOf(targetWords);
        int end = begin + targetWords.length();
        Concept concept = new Concept(jCas, begin, end);
        concept.addToIndexes();
        fastContext_AE.process(jCas);
        Collection<Concept> targets = JCasUtil.select(jCas, Concept.class);
        for (Concept target : targets) {
            System.out.println(target.toString());
        }
        for (Context context : JCasUtil.select(jCas, Context.class)) {
            System.out.println(context.toString());
        }
    }

    @Test
    public void test3() throws AnalysisEngineProcessException, ResourceInitializationException {
        String text = "1.CAD, s/p MI: currently stable.";
        String targetWords = "MI";
        jCas.setDocumentText(text);
        Object[] configurationData = new Object[]{FastContext_General_AE.PARAM_RULE_STR, "@CONCEPT_FEATURES|Concept|Negation|Certainty|Temporality|Experiencer\n" +
                "@FEATURE_VALUES|Negation|affirm|negated\n" +
                "@FEATURE_VALUES|Certainty|certain|uncertain\n" +
                "@FEATURE_VALUES|Temporality|present|historical|hypothetical\n" +
                "@FEATURE_VALUES|Experiencer|patient|nonpatient\n" +
                "s / p|both|trigger|historical|10\n" +
                "currently|both|termination|historical|10\n" +
                "currently|both|trigger|present|30",  FastContext_General_AE.PARAM_MARK_CLUE, true};
        fastContext_AE = createEngine(FastContext_General_AE.class,
                configurationData);
        simpleParser_AE.process(jCas);
        int begin = text.indexOf(targetWords);
        int end = begin + targetWords.length();
        Concept concept = new Concept(jCas, begin, end);
        concept.addToIndexes();
        fastContext_AE.process(jCas);
        Collection<Concept> targets = JCasUtil.select(jCas, Concept.class);
        for (Concept target : targets) {
            System.out.println(target.toString());
        }
        for (Context context : JCasUtil.select(jCas, Context.class)) {
            System.out.println(context.toString());
        }
    }


    @Test
    public void test4() throws AnalysisEngineProcessException, ResourceInitializationException {
        String text = "Pain was 6/10 at worst, unchanged with exertion and similar in quality (albeit less intense) to pain during STEMI.";
        String targetWords = "STEMI";
        jCas.setDocumentText(text);
        Object[] configurationData = new Object[]{FastContext_General_AE.PARAM_RULE_STR, "@CONCEPT_FEATURES|Concept|Negation|Certainty|Temporality|Experiencer\n" +
                "@FEATURE_VALUES|Negation|affirm|negated\n" +
                "@FEATURE_VALUES|Certainty|certain|uncertain\n" +
                "@FEATURE_VALUES|Temporality|present|historical|hypothetical\n" +
                "@FEATURE_VALUES|Experiencer|patient|nonpatient\n" +
                "similar in quality \\w+ \\w+ \\w+ \\w+ \\w+ \\w+ to|forward|trigger|uncertain|20\n" +
                "similar in quality \\w+ \\w+ \\w+ \\w+ \\w+ to|forward|trigger|uncertain|20\n" +
                "similar in quality \\w+ \\w+ \\w+ \\w+ to|forward|trigger|uncertain|20\n" +
                "similar in quality \\w+ \\w+ \\w+ to|forward|trigger|uncertain|20\n" +
                "similar in quality \\w+ \\w+ to|forward|trigger|uncertain|20\n" +
                "similar in quality \\w+ to|forward|trigger|uncertain|15\n" +
                "similar \\w+ \\w+ \\w+ to|forward|trigger|uncertain|15\n" +
                "similar \\w+ \\w+ to|forward|trigger|uncertain|8\n" +
                "similar \\w+ to|forward|trigger|uncertain|8\n" +
                "similar to|forward|trigger|uncertain|8\n",
                FastContext_General_AE.PARAM_MARK_CLUE, true};
        fastContext_AE = createEngine(FastContext_General_AE.class,
                configurationData);
        simpleParser_AE.process(jCas);
        int begin = text.indexOf(targetWords);
        int end = begin + targetWords.length();
        Concept concept = new Concept(jCas, begin, end);
        concept.addToIndexes();
        fastContext_AE.process(jCas);
        Collection<Concept> targets = JCasUtil.select(jCas, Concept.class);
        for (Concept target : targets) {
            System.out.println(target.toString());
        }
        for (Context context : JCasUtil.select(jCas, Context.class)) {
            System.out.println(context.toString());
        }
    }

    @Test
    public void test0() throws AnalysisEngineProcessException, ResourceInitializationException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        String text = "subjects in this analysis is confined to 3,388 (of the total 5,297) subjects treated at \n";
        String rules = "@CONCEPT_FEATURES|CLUE|Negation|Certainty|Temporality|SampleSize|Unit\n" +
                "@FEATURE_VALUES|SampleSize|ss|notss\n" +
                "@FEATURE_VALUES|Unit|otherunit|pt\n" +
                "@FEATURE_VALUES|Negation|affirm|negated\n" +
                "@FEATURE_VALUES|Certainty|certain|uncertain\n" +
                "@FEATURE_VALUES|Temporality|present|historical|hypothetical\n" +
                ")|backward|trigger|notss|2";
        String targetWords = "388";
        Collection<TypeDefinition> clsDef = new FastContext_General_AE().getTypeDefs(rules).values();
        runner.addConceptTypes(clsDef);
        runner.reInitTypeSystem("target/generated-test-sources/customized");
        System.out.println(AnnotationOper.getTypeClass("CLUE"));
        System.out.println(AnnotationOper.getTypeClass("edu.utah.bmi.nlp.type.system.CLUE"));
        Class<? extends Concept> cls = AnnotationOper.getTypeClass("edu.utah.bmi.nlp.type.system.CLUE").asSubclass(Concept.class);
        jCas = runner.initJCas();
        jCas.setDocumentText(text);
        Object[] configurationData = new Object[]{FastContext_General_AE.PARAM_RULE_STR,
                rules, FastContext_General_AE.PARAM_MARK_CLUE, true};
        fastContext_AE = createEngine(FastContext_General_AE.class,
                configurationData);

        simpleParser_AE.process(jCas);
        int begin = text.indexOf(targetWords);
        int end = begin + targetWords.length();
        cls = AnnotationOper.getTypeClass(DeterminantValueSet.checkNameSpace("CLUE")).asSubclass(Concept.class);
        Constructor<? extends Concept> cons = cls.getConstructor(JCas.class, int.class, int.class);
        Concept concept = AnnotationFactory.createAnnotation(jCas,begin,end, cls);
        concept.addToIndexes();
        fastContext_AE.process(jCas);
        Collection<Concept> targets = JCasUtil.select(jCas, Concept.class);
        assert(targets.size()==1);
        Concept anno=targets.iterator().next();
//        System.out.println(FSUtil.getFeature(anno, "SampleSize", String.class));
        assert(FSUtil.getFeature(anno, "SampleSize", String.class).equals("ss"));
        for (Concept target : targets) {
            System.out.println(target.toString());
        }
        Collection<Context> contexts = JCasUtil.select(jCas, Context.class);
        assert (contexts.size()==0);

    }

    @Test
    public void test6() throws AnalysisEngineProcessException, ResourceInitializationException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        String text = "no vomiting currently , although she did in the past .\n";
        text = "no vomiting  in the past, although she did currently.\n";
        String rules = "@CONCEPT_FEATURES|CLUE|Negation|Certainty|Temporality\n" +
                "@FEATURE_VALUES|Negation|affirm|negated\n" +
                "@FEATURE_VALUES|Certainty|certain|uncertain\n" +
                "@FEATURE_VALUES|Temporality|present|historical|hypothetical\n" +
                "currently|both|trigger|present|30\n" +
                "in the past|both|trigger|historical|30";
        String targetWords = "vomiting";
        runner.addConceptTypes(FastContext_General_AE.getTypeDefinitions(rules, true).values());
        runner.reInitTypeSystem("target/generated-test-sources/customized");
        jCas = runner.initJCas();
        jCas.setDocumentText(text);
        Object[] configurationData = new Object[]{FastContext_General_AE.PARAM_RULE_STR,
                rules,
                FastContext_General_AE.PARAM_DEBUG, true, FastContext_General_AE.PARAM_MARK_CLUE, true};
        fastContext_AE = createEngine(FastContext_General_AE.class,
                configurationData);

        simpleParser_AE.process(jCas);
        int begin = text.indexOf(targetWords);
        int end = begin + targetWords.length();
        Class<? extends Concept> cls = AnnotationOper.getTypeClass("CLUE").asSubclass(Concept.class);
        Concept concept = AnnotationFactory.createAnnotation(jCas,begin,end, cls);
        concept.addToIndexes();
        fastContext_AE.process(jCas);
        Collection<Concept> targets = JCasUtil.select(jCas, Concept.class);
        for (Concept target : targets) {
            System.out.println(target.toString());
        }
        for (Context context : JCasUtil.select(jCas, Context.class)) {
            System.out.println(context.toString());
        }
    }

    @Test
    public void test7() throws AnalysisEngineProcessException, ResourceInitializationException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        String text = "no vomiting, nausea, cough now, but he does have fever currently.\n";
        String rules = "@CONCEPT_FEATURES|CLUE|Negation|Certainty|Temporality\n" +
                "@FEATURE_VALUES|Negation|affirm|negated\n" +
                "@FEATURE_VALUES|Certainty|certain|uncertain\n" +
                "@FEATURE_VALUES|Temporality|present|historical|hypothetical\n" +
                "currently|both|trigger|historical|11\n" +
                "now|both|trigger|historical|3";
        String targetWords = "vomiting";
        runner.addConceptTypes(FastContext_General_AE.getTypeDefinitions(rules, true).values());
        runner.reInitTypeSystem("target/generated-test-sources/customized");
        jCas = runner.initJCas();
        jCas.setDocumentText(text);
        Object[] configurationData = new Object[]{FastContext_General_AE.PARAM_RULE_STR,
                rules,FastContext_General_AE.PARAM_MARK_CLUE, true};
        fastContext_AE = createEngine(FastContext_General_AE.class,
                configurationData);
        simpleParser_AE.process(jCas);
        int begin = text.indexOf(targetWords);
        int end = begin + targetWords.length();
        Class<? extends Concept> cls = AnnotationOper.getTypeClass("CLUE").asSubclass(Concept.class);
        Concept concept = AnnotationFactory.createAnnotation(jCas,begin,end, cls);
        concept.addToIndexes();
        fastContext_AE.process(jCas);
        Collection<Concept> targets = JCasUtil.select(jCas, Concept.class);
        for (Concept target : targets) {
            System.out.println(target.toString());
        }
        for (Context context : JCasUtil.select(jCas, Context.class)) {
            System.out.println(context.toString());
        }
    }

    @Test
    public void test8() throws AnalysisEngineProcessException, ResourceInitializationException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        String text = "no vomiting currently , although she did in the past .\n";
        text = "currently, no vomiting   although she did in the past.\n";
        String rules = "@CONCEPT_FEATURES|CLUE|Negation|Certainty|Temporality\n" +
                "@FEATURE_VALUES|Negation|affirm|negated\n" +
                "@FEATURE_VALUES|Certainty|certain|uncertain\n" +
                "@FEATURE_VALUES|Temporality|present|historical|hypothetical\n" +
                "currently|both|trigger|present|30\n" +
                "in the past|both|trigger|historical|30";
        String targetWords = "vomiting";
        runner.addConceptTypes(FastContext_General_AE.getTypeDefinitions(rules, true).values());
        runner.reInitTypeSystem("target/generated-test-sources/customized");
        jCas = runner.initJCas();
        jCas.setDocumentText(text);
        Object[] configurationData = new Object[]{FastContext_General_AE.PARAM_RULE_STR,
                rules,
                FastContext_General_AE.PARAM_DEBUG, true, FastContext_General_AE.PARAM_MARK_CLUE, true};
        fastContext_AE = createEngine(FastContext_General_AE.class,
                configurationData);

        simpleParser_AE.process(jCas);
        int begin = text.indexOf(targetWords);
        int end = begin + targetWords.length();
        Class<? extends Concept> cls = AnnotationOper.getTypeClass("CLUE").asSubclass(Concept.class);
        Concept concept = AnnotationFactory.createAnnotation(jCas,begin,end, cls);
        concept.addToIndexes();
        fastContext_AE.process(jCas);
        Collection<Concept> targets = JCasUtil.select(jCas, Concept.class);
        for (Concept target : targets) {
            System.out.println(target.toString());
        }
        for (Context context : JCasUtil.select(jCas, Context.class)) {
            System.out.println(context.toString());
        }
    }

    @Test
    public void test9() throws AnalysisEngineProcessException, ResourceInitializationException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        String text = "no vomiting currently , although she did in the past .\n";
        text = "currently, no vomiting   although she did in the past.\n";
        String rules = "@CONCEPT_FEATURES|edu.utah.bmi.nlp.type.system.CLUE|Negation|Certainty|Temporality\n" +
                "@FEATURE_VALUES|Negation|affirm|negated\n" +
                "@FEATURE_VALUES|Certainty|certain|uncertain\n" +
                "@FEATURE_VALUES|Temporality|present|historical|hypothetical\n" +
                "currently|both|trigger|present|30\n" +
                "in the past|both|trigger|historical|30";
        String targetWords = "vomiting";
        runner.addConceptTypes(FastContext_General_AE.getTypeDefinitions(rules, true).values());
        runner.reInitTypeSystem("target/generated-test-sources/customized");
        jCas = runner.initJCas();
        jCas.setDocumentText(text);
        Object[] configurationData = new Object[]{FastContext_General_AE.PARAM_RULE_STR,
                rules,
                FastContext_General_AE.PARAM_DEBUG, true, FastContext_General_AE.PARAM_MARK_CLUE, true};
        fastContext_AE = createEngine(FastContext_General_AE.class,
                configurationData);

        simpleParser_AE.process(jCas);
        int begin = text.indexOf(targetWords);
        int end = begin + targetWords.length();
        Class<? extends Concept> cls = AnnotationOper.getTypeClass("CLUE").asSubclass(Concept.class);
        Concept concept = AnnotationFactory.createAnnotation(jCas,begin,end, cls);
        concept.addToIndexes();
        fastContext_AE.process(jCas);
        Collection<Concept> targets = JCasUtil.select(jCas, Concept.class);
        for (Concept target : targets) {
            System.out.println(target.toString());
        }
        for (Context context : JCasUtil.select(jCas, Context.class)) {
            System.out.println(context.toString());
        }
    }

    /**
     * Test improper segemented sentence error
     *
     * @throws AnalysisEngineProcessException
     * @throws ResourceInitializationException
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     */
    @Test
    public void test10() throws AnalysisEngineProcessException, ResourceInitializationException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        String text = "no vomiting currently , although she did in the past .\n";
        text = "currently, no vomiting   although she did in the past.\n";
        String rules = "@CONCEPT_FEATURES|edu.utah.bmi.nlp.type.system.CLUE|Negation|Certainty|Temporality\n" +
                "@FEATURE_VALUES|Negation|affirm|negated\n" +
                "@FEATURE_VALUES|Certainty|certain|uncertain\n" +
                "@FEATURE_VALUES|Temporality|present|historical|hypothetical\n" +
                "currently|both|trigger|present|30\n" +
                "in the past|both|trigger|historical|30";
        String targetWords = "vomiting";
        runner.addConceptTypes(new FastContext_General_AE().getTypeDefs(rules).values());
        runner.reInitTypeSystem("target/generated-test-sources/customized");
        jCas = runner.initJCas();
        jCas.setDocumentText(text);
        Object[] configurationData = new Object[]{FastContext_General_AE.PARAM_RULE_STR,
                rules, FastContext_General_AE.PARAM_MARK_CLUE, true};
        fastContext_AE = createEngine(FastContext_General_AE.class,
                configurationData);
        AnalysisEngine tokenizer = createEngine(SimpleParser_AE.class);
        int begin = text.indexOf(targetWords);
        int end = begin + targetWords.length();
        Class<? extends Concept> cls = AnnotationOper.getTypeClass("CLUE").asSubclass(Concept.class);
        Concept concept = AnnotationFactory.createAnnotation(jCas, begin,end, cls);
        concept.addToIndexes();
        tokenizer.process(jCas);
        fastContext_AE.process(jCas);
        Collection<Concept> targets = JCasUtil.select(jCas, Concept.class);
        assert (targets.size()==1);
        assert (FSUtil.getFeature(targets.iterator().next(), "Temporality", String.class).equals("historical"));
        assert (JCasUtil.select(jCas, Context.class).size()==1);

    }


}