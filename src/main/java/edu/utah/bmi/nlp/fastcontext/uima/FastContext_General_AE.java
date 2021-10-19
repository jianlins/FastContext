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

import edu.utah.bmi.nlp.context.common.ConTextSpan;
import edu.utah.bmi.nlp.core.DeterminantValueSet;
import edu.utah.bmi.nlp.core.IOUtil;
import edu.utah.bmi.nlp.core.IntervalST;
import edu.utah.bmi.nlp.core.Interval1D;
import edu.utah.bmi.nlp.core.TypeDefinition;
import edu.utah.bmi.nlp.core.WildCardChecker;
import edu.utah.bmi.nlp.type.system.ConceptBASE;
import edu.utah.bmi.nlp.type.system.Context;
import edu.utah.bmi.nlp.uima.ae.RuleBasedAEInf;
import edu.utah.bmi.nlp.uima.common.AnnotationComparator;
import edu.utah.bmi.nlp.uima.common.AnnotationOper;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This is a specific version demo to for faircode. Run all dictionaries in one batch.
 *
 * @author Jianlin Shi
 */
public class FastContext_General_AE
        extends JCasAnnotator_ImplBase implements RuleBasedAEInf {
    public static Logger logger = IOUtil.getLogger(FastContext_General_AE.class);

    public static final String PARAM_RULE_STR = DeterminantValueSet.PARAM_RULE_STR;
    public static final String PARAM_SENTENCE_TYPE_NAME = "SentenceTypeName";
    public static final String PARAM_TOKEN_TYPE_NAME = "TokenTypeName";
    public static final String PARAM_CASE_SENSITIVE = DeterminantValueSet.PARAM_CASE_SENSITIVE;
    public static final String PARAM_MARK_CLUE = "MarkClues";
    public static final String PARAM_AUTO_EXPAND_SCOPE = "AutoExpanScope";
    @Deprecated
    public static final String PARAM_DEBUG = "Debug";


    private FastContextUIMA cp;
    private String sentenceTypeName = "", tokenTypeName = "";
    private int sentenceTypeId = 0, tokenTypeId = 0;
    private HashMap<String, HashMap<String, Method>> conceptFeatures = new HashMap<>();
    private HashMap<String, Class> conceptClassMap = new HashMap<>();
    @Deprecated
    private boolean debug;
    //  if the sentence is happened to segmented too short. Expand cp to  one sentence backwards and one sentence forwards.
    private boolean autoExpanScope = true, caseSensitive = false, markClues = false;

    @Override
    public void initialize(UimaContext cont) {
        try {
            super.initialize(cont);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String contextRuleStr = (String) (cont
                .getConfigParameterValue(PARAM_RULE_STR));
        sentenceTypeName = DeterminantValueSet.defaultNameSpace + "Sentence";
        tokenTypeName = DeterminantValueSet.defaultNameSpace + "Token";

        Object paraObj = cont.getConfigParameterValue(PARAM_DEBUG);
        if (paraObj != null && paraObj instanceof Boolean) {
            debug = (Boolean) paraObj;
        }

        paraObj = cont.getConfigParameterValue(PARAM_SENTENCE_TYPE_NAME);
        if (paraObj != null) {
            sentenceTypeName = (String) paraObj;
        }
        paraObj = cont.getConfigParameterValue(PARAM_TOKEN_TYPE_NAME);
        if (paraObj != null) {
            tokenTypeName = (String) paraObj;
        }


        paraObj = cont.getConfigParameterValue(PARAM_CASE_SENSITIVE);
        if (paraObj != null && paraObj instanceof Boolean) {
            caseSensitive = (Boolean) paraObj;
        }

        paraObj = cont.getConfigParameterValue(PARAM_AUTO_EXPAND_SCOPE);
        if (paraObj != null && paraObj instanceof Boolean) {
            autoExpanScope = (Boolean) paraObj;
        }

        paraObj = cont.getConfigParameterValue(PARAM_MARK_CLUE);
        if (paraObj != null && paraObj instanceof Boolean) {
            markClues = (Boolean) paraObj;
        }

        cp = new FastContextUIMA(contextRuleStr, caseSensitive);

        HashMap<String, TypeDefinition> conceptFeatureMap = cp.getTypeDefinitions();
        for (String conceptName : conceptFeatureMap.keySet()) {
            TypeDefinition typeDefinition = conceptFeatureMap.get(conceptName);
            try {
                Class contextTypeClass = AnnotationOper.getTypeClass((conceptName)).asSubclass(Annotation.class);
                conceptClassMap.put(conceptName, contextTypeClass);
                conceptFeatures.put(conceptName, new HashMap<>());
                HashMap<String, Method> featureMap = conceptFeatures.get(conceptName);
                for (String feature : typeDefinition.getNewFeatureNames()) {
                    Method method = contextTypeClass.getMethod(AnnotationOper.inferSetMethodName(feature), String.class);
                    featureMap.put(feature, method);
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        sentenceTypeId = AnnotationOper.getTypeId(sentenceTypeName);
        tokenTypeId = AnnotationOper.getTypeId(tokenTypeName);

    }


    public void process(JCas jcas) throws AnalysisEngineProcessException {
        String docText = jcas.getDocumentText();
        ArrayList<Annotation> sentences = new ArrayList<>();
        ArrayList<Annotation> tokens = new ArrayList<>();

        IntervalST<Integer> sentenceIndex = AnnotationOper.indexAnnotation(jcas, sentenceTypeId, sentences);
        IntervalST<Integer> tokenIndex = AnnotationOper.indexAnnotation(jcas, tokenTypeId, tokens);

        for (String conceptName : conceptFeatures.keySet()) {
            process(jcas, docText, sentences, tokens, sentenceIndex, tokenIndex, conceptClassMap.get(conceptName));
        }
    }


    public void process(JCas jcas, String docText, ArrayList<Annotation> sentences, ArrayList<Annotation> tokens,
                        IntervalST<Integer> sentenceIndex, IntervalST<Integer> tokenIndex, Class conceptClass) {
        ArrayList<Annotation> concepts = new ArrayList<>();
        concepts.addAll(JCasUtil.select(jcas, conceptClass));
        if (concepts.size() == 0) {
            return;
        }
        for (Annotation concept : concepts) {
            Integer sentenceId = sentenceIndex.get(new Interval1D(concept.getBegin(), concept.getEnd()));
            if (sentenceId == null) {
                logger.warning("\nConcept: \"" + docText.substring(concept.getBegin(), concept.getEnd()) + "\" is not in the indexed sentence boundaries.\n" +
                        "Check the sentence segmenter rules to see if the sentencs are segmented properly. \n" +
                        "And check the NER configurations to see if Section configuration includes the concept's section.\n");
                if (logger.isLoggable(Level.FINEST)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("\nThe indexed sentences includes (line breaks are replaced with whitespaces): \n");
                    for (Annotation sentence : sentences) {
                        sb.append("\n" + docText.substring(sentence.getBegin(), sentence.getEnd()).replaceAll("\\n", "") + "\n");
                    }
                    logger.finest(sb.toString());
                }
                continue;
            }
            Annotation sentence = sentences.get(sentenceIndex.get(new Interval1D(concept.getBegin(), concept.getEnd())));
            int scopeBegin = sentence.getBegin();
            int scopeEnd = sentence.getEnd();
            if (autoExpanScope) {
                int firstTokenIdofSentence = getFirstTokenId(sentence, docText, tokenIndex, tokens);
                int lastTokenIdfSentence = getLastTokenId(sentence, docText, tokenIndex, tokens);
                int firstTokenIdofConcept = getFirstTokenId(concept, docText, tokenIndex, tokens);
                int lastTokenIdofConcept = getLastTokenId(concept, docText, tokenIndex, tokens);
                if ((lastTokenIdfSentence - firstTokenIdofSentence) - (lastTokenIdofConcept - firstTokenIdofConcept) < 2) {
                    Annotation previousSentence = getPreviousSentence(sentences, tokens, sentenceIndex, firstTokenIdofSentence);
                    Annotation nextSentence = getNextSentence(sentences, tokens, sentenceIndex, lastTokenIdfSentence);
                    if (previousSentence != null) {
                        scopeBegin = previousSentence.getBegin();
                    }
                    if (nextSentence != null) {
                        scopeEnd = nextSentence.getEnd();
                    }
                }
            }
            Iterable<Integer> tokensInSentence = tokenIndex.getAll(new Interval1D(scopeBegin, scopeEnd));
            ArrayList<Annotation> preContext = new ArrayList<>();
            ArrayList<Annotation> postContext = new ArrayList<>();
            for (Integer tokenId : tokensInSentence) {
                Annotation token = tokens.get(tokenId);
                if (token.getEnd() <= concept.getBegin()) {
                    preContext.add(token);
                } else if (token.getBegin() >= concept.getEnd()) {
                    postContext.add(token);
                }
            }
            Collections.sort(preContext, new AnnotationComparator());
            Collections.sort(postContext, new AnnotationComparator());
            logger.finest("Detect context for concept:\t"+concept.getCoveredText());
            LinkedHashMap<String, ConTextSpan> contextInfo = cp.getFullContextFeatures(conceptClass.getCanonicalName(), preContext, postContext);
            saveContext(jcas, docText, conceptClass, concept, contextInfo);
        }
    }


    private int getFirstTokenId(Annotation sentence, String docText, IntervalST<Integer> tokenIndex, ArrayList<Annotation> tokens) {
        int i = sentence.getBegin();
        while (!(WildCardChecker.isPunctuation(docText.charAt(i))
                || Character.isAlphabetic(docText.charAt(i))
                || Character.isDigit(docText.charAt(i)))) {
            i++;
        }
        if (tokenIndex.get(new Interval1D(i, i + 1)) == null) {
            while (!(Character.isAlphabetic(docText.charAt(i))
                    || Character.isDigit(docText.charAt(i)))) {
                i++;
            }
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(sentence.getCoveredText());
            logger.fine(docText.substring(i, sentence.getEnd()));
            for (int tokenId : tokenIndex.getAll(new Interval1D(sentence.getBegin(), sentence.getEnd()))) {
                logger.fine(tokens.get(tokenId).getCoveredText());
            }
        }
        int firstTokenId = tokenIndex.get(new Interval1D(i, i + 1));
        return firstTokenId;
    }

    private int getLastTokenId(Annotation sentence, String docText, IntervalST<Integer> tokenIndex, ArrayList<Annotation> tokens) {
        int i = sentence.getEnd() - 1;
        while (!(WildCardChecker.isPunctuation(docText.charAt(i))
                || Character.isAlphabetic(docText.charAt(i))
                || Character.isDigit(docText.charAt(i)))) {
            i--;
        }
        if (tokenIndex.get(new Interval1D(i, i + 1)) == null) {
            while (!(Character.isAlphabetic(docText.charAt(i))
                    || Character.isDigit(docText.charAt(i)))) {
                i--;
            }
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(sentence.getCoveredText());
            logger.fine(docText.substring(i, sentence.getEnd()));
            for (int tokenId : tokenIndex.getAll(new Interval1D(sentence.getBegin(), sentence.getEnd()))) {
                logger.fine(tokens.get(tokenId).getCoveredText());
            }
        }
        int lastTokenId = tokenIndex.get(new Interval1D(i, i + 1));
        return lastTokenId;
    }

    private Annotation getPreviousSentence(ArrayList<Annotation> sentences, ArrayList<Annotation> tokens, IntervalST<Integer> sentenceIndex, int firstTokenIdofSentence) {
        if (firstTokenIdofSentence > 0) {
            Annotation previousToken = tokens.get(firstTokenIdofSentence - 1);
            Object sentenceId = sentenceIndex.get(new Interval1D(previousToken.getBegin(), previousToken.getEnd()));
            if (sentenceId == null) {
                logger.warning("Token (" + previousToken.getBegin() + "-" + previousToken.getEnd() + "): " + previousToken.getCoveredText() + " doesn't have a split sentence to cover. Check your sentence splitter.");
                return null;
            }
            return sentences.get((int) sentenceId);
        } else {
            return null;
        }
    }

    private Annotation getNextSentence(ArrayList<Annotation> sentences, ArrayList<Annotation> tokens, IntervalST<Integer> sentenceIndex, int lastTokenIdfSentence) {
        if (lastTokenIdfSentence < tokens.size() - 1) {
            Annotation nextToken = tokens.get(lastTokenIdfSentence + 1);
            Object sentenceId = sentenceIndex.get(new Interval1D(nextToken.getBegin(), nextToken.getEnd()));
            if (sentenceId == null) {
                logger.warning("Token (" + nextToken.getBegin() + "-" + nextToken.getEnd() + "): " + nextToken.getCoveredText() + " doesn't have a split sentence to cover. Check your sentence splitter.");
                return null;
            }
            return sentences.get((int) sentenceId);
        } else {
            return null;
        }
    }


    private void saveContext(JCas jcas, String docText, Class conceptClass, Annotation concept, LinkedHashMap<String, ConTextSpan> contextInfo) {
        HashMap<String, Method> featureMethods = conceptFeatures.get(conceptClass.getCanonicalName());
        for (String featureName : contextInfo.keySet()) {
            ConTextSpan conTextSpan = contextInfo.get(featureName);
            String value;
            if (conTextSpan.ruleId != -1)
                value = cp.getContextRuleByRuleId(conTextSpan.ruleId).modifier;
            else
                value = cp.getFeatureDefaultValueMap().getOrDefault(featureName, "");

            if (featureMethods.containsKey(featureName)) {
                AnnotationOper.setFeatureValue(featureName, concept, value);
            } else {
                logger.fine("Feature: " + featureName + " does not exist in Annotation " + concept.getClass().getCanonicalName());
                continue;
            }
            if (concept instanceof ConceptBASE) {
                ConceptBASE conceptBASE = (ConceptBASE) concept;
                String previousNote;
                if (conTextSpan.ruleId != -1) {
                    previousNote = conceptBASE.getNote();
                    previousNote = previousNote == null ? "" : previousNote;
                    conceptBASE.setNote((previousNote + "\n\t" + featureName + " clue:\t'"
                            + docText.substring(conTextSpan.begin, conTextSpan.end).replaceAll("[\\n|\\r]", " ")
                            + "' (" + conTextSpan.begin + "~" + conTextSpan.end + ")").trim());
                }
            }
            if (markClues && conTextSpan.ruleId != -1) {
                Context context = new Context(jcas, conTextSpan.begin, conTextSpan.end);
                context.setModifierName(featureName);
                context.setModifierValue(value.replaceAll("[\\n|\\r]", " "));
                context.setTargetConcept(concept.getCoveredText().replaceAll("[\\n|\\r]", " ") + " (" + concept.getBegin() + "~" + concept.getEnd() + ")");
                context.addToIndexes();
            }
        }

    }

    /**
     * Because implement a reinforced interface method (static is not reinforced), this is deprecated, just to
     * enable back-compatibility.
     *
     * @param ruleStr         Rule file path or rule content string
     * @param caseInsensitive whether to parse the rule in case-insensitive manner
     * @return Type name--Type definition map
     */
    @Deprecated
    public static HashMap<String, TypeDefinition> getTypeDefinitions(String ruleStr, boolean caseInsensitive) {
        return new FastContextUIMA(ruleStr, caseInsensitive).getTypeDefinitions();
    }


    @Override
    public LinkedHashMap<String, TypeDefinition> getTypeDefs(String ruleStr) {
        return new FastContextUIMA(ruleStr, false).getTypeDefinitions();
    }
}
