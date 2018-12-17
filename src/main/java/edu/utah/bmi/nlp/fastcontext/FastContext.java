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
package edu.utah.bmi.nlp.fastcontext;

import edu.utah.bmi.nlp.context.common.ConTextAdvancedInterface;
import edu.utah.bmi.nlp.context.common.ConTextSpan;
import edu.utah.bmi.nlp.context.common.ContextRule;
import edu.utah.bmi.nlp.context.common.ContextValueSet.TriggerTypes;
import edu.utah.bmi.nlp.core.*;
import org.apache.commons.io.IOUtils;
import org.apache.uima.jcas.tcas.Annotation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class take context token/Annotation ArrayList as input, process the ConText algorithm.
 *
 * @author Jianlin Shi
 */
public class FastContext implements ConTextAdvancedInterface {
    public static Logger logger = IOUtil.getLogger(FastContext.class);
    protected ContextRuleProcessor crp;
    @Deprecated
    public boolean debug = false;


    public FastContext(String ruleFile) {
        initiate(ruleFile, false);
    }

    protected FastContext() {
    }

    public FastContext(String ruleFile, boolean caseSensitive) {
        initiate(ruleFile, caseSensitive);
    }

    public FastContext(InputStream ruleStream) {
        initiate(ruleStream, false, StandardCharsets.UTF_8.toString());
    }

    public FastContext(InputStream ruleStream, boolean caseSensitive) {
        initiate(ruleStream, caseSensitive, StandardCharsets.UTF_8.name());
    }

    public FastContext(InputStream ruleStream, boolean caseSensitive, String encoding) {
        initiate(ruleStream, caseSensitive, encoding);
    }

    public FastContext(ArrayList<String> rulesList, boolean caseSensitive) {
        initiate(rulesList, caseSensitive);
    }

    public void initiate(InputStream ruleStream, boolean caseSensitive, String encoding) {
        String ruleStr = null;
        try {
            ruleStr = IOUtils.toString(ruleStream, encoding);
        } catch (IOException e) {
            e.printStackTrace();
        }
        crp = new ContextRuleProcessor(ruleStr, caseSensitive);
    }

    public void initiate(String ruleFile, boolean caseSensitive) {
        crp = new ContextRuleProcessor(ruleFile, caseSensitive);
    }

    public void initiate(ArrayList<String> rulesList, boolean caseInsensitive) {
        crp = new ContextRuleProcessor(rulesList);
        crp.setCaseSensitive(caseInsensitive);
    }


    public ArrayList<String> processContext(ArrayList<String> tokens, int conceptStartPosition, int conceptEndPosition, int windowSize) {
        ArrayList<String> context = convertOutput(processContextWEvidence(tokens, conceptStartPosition, conceptEndPosition, windowSize));
        return context;
    }


    public ArrayList<String> processContext(ArrayList<Span> tokens, int conceptStartPosition,
                                            int conceptEndPosition, String text, int windowSize) {
        ArrayList<String> context = convertOutput(processContextWEvidence(tokens, conceptStartPosition, conceptEndPosition, text, windowSize));
        return context;
    }

    public ArrayList<String> processContext(String sentence, int conceptBeginOffset, int conceptEndOffset, int windowSize) {
        ArrayList<String> context = convertOutput(processContextWEvidence(sentence, conceptBeginOffset, conceptEndOffset, windowSize));
        return context;
    }

    public LinkedHashMap<String, ConTextSpan> processContextWEvidence(ArrayList<String> tokens, int conceptStartPosition, int conceptEndPosition, int windowSize) {
        ArrayList<Span> preContext = new ArrayList<>();
        ArrayList<Span> postContext = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i++) {
            if (i < conceptStartPosition) {
                preContext.add(new Span(i, i + 1, tokens.get(i)));
            } else if (i > conceptEndPosition) {
                postContext.add(new Span(i, i + 1, tokens.get(i)));
            }
        }
        return processContextWEvidence(preContext, postContext, conceptEndPosition, false);
    }

    public LinkedHashMap<String, ConTextSpan> processContextWEvidence(ArrayList<Span> tokens, int conceptStartPosition,
                                                                      int conceptEndPosition, String text, int windowSize) {
        List<Span> preContext = tokens.subList(0, conceptStartPosition);
        List<Span> postContext = tokens.subList(conceptEndPosition + 1, tokens.size());
        return processContextWEvidence(preContext, postContext, conceptEndPosition, true);
    }

    public LinkedHashMap<String, ConTextSpan> processContextWEvidence(String sentence, int conceptBeginOffset, int conceptEndOffset, int windowSize) {
        String preContextString = sentence.substring(0, conceptBeginOffset);
        String conceptString = sentence.substring(conceptBeginOffset, conceptEndOffset);
        String postContextString = sentence.substring(conceptEndOffset, sentence.length());
        ArrayList<Span> preContextTokens = SimpleParser.tokenizeOnWhitespaces(preContextString);
        ArrayList<Span> conceptTokens = SimpleParser.tokenizeOnWhitespaces(conceptString, conceptBeginOffset);
        ArrayList<Span> postContextTokens = SimpleParser.tokenizeOnWhitespaces(postContextString, conceptEndOffset);
        return processContextWEvidence(preContextTokens, postContextTokens, preContextTokens.size() + conceptTokens.size() - 1, true);
    }


    protected LinkedHashMap<String, ConTextSpan> processContextWEvidence(List<Span> preContext, List<Span> postContext, int conceptEndPosition, boolean absoluteOffsetSpan) {
        LinkedHashMap<String, ConTextSpan> matchedPreRules = new LinkedHashMap<>();
        LinkedHashMap<String, ConTextSpan> matchedPostRules = new LinkedHashMap<>();
        matchedPreRules.clear();
        matchedPostRules.clear();
        crp.processRules(preContext, 0, matchedPreRules);
        crp.processRules(postContext, 0, matchedPostRules);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("pre context matches:");
            for (Map.Entry<String, ConTextSpan> ent : matchedPreRules.entrySet()) {
                logger.fine(ent.getValue().ruleId + ": " + ent.getKey() + " " + crp.rules.get(ent.getValue().ruleId).triggerType + ":\t" + ent.getValue().begin + "-" + ent.getValue().end);
                logger.fine(crp.rules.get(ent.getValue().ruleId) + "\n");
            }
            logger.fine("post context matches:");
            for (Map.Entry<String, ConTextSpan> ent : matchedPostRules.entrySet()) {
                logger.fine(ent.getValue().ruleId + ": " + ent.getKey() + " " + crp.rules.get(ent.getValue().ruleId).triggerType + ":\t" + ent.getValue().begin + "-" + ent.getValue().end);
                logger.fine(crp.rules.get(ent.getValue().ruleId) + "\n");
            }
        }
        LinkedHashMap<String, ConTextSpan> contexts = new LinkedHashMap<>();
        if (absoluteOffsetSpan) {
            applyConTexts(preContext, matchedPreRules, contexts, TriggerTypes.forward, preContext.size());
            applyConTexts(postContext, matchedPostRules, contexts, TriggerTypes.backward, preContext.size());
        } else {
            applyConTexts(0, matchedPreRules, contexts, TriggerTypes.forward, preContext.size());
            applyConTexts(conceptEndPosition + 1, matchedPostRules, contexts, TriggerTypes.backward, preContext.size());
        }
        return contexts;
    }


    protected void applyConTexts(int contextOffset, HashMap<String, ConTextSpan> matchedRules, LinkedHashMap<String, ConTextSpan> contexts, TriggerTypes direction, int conceptBegin) {
        for (Map.Entry<String, ConTextSpan> ent : matchedRules.entrySet()) {
            ConTextSpan conTextSpan = ent.getValue();
            if (contextOffset > 0) {
                conTextSpan.begin = conTextSpan.begin + contextOffset;
                conTextSpan.end = conTextSpan.end + contextOffset;
            }
            ContextRule matchedRule = crp.rules.get(conTextSpan.ruleId);
            if (matchedRule.triggerType == TriggerTypes.trigger && conTextSpan.matchedDirection == direction) {
                if (direction == TriggerTypes.forward || direction == TriggerTypes.both) {
                    if (conceptBegin - ent.getValue().begin <= matchedRule.windowSize)
                        contexts.put(matchedRule.modifier, conTextSpan);
                } else if (direction == TriggerTypes.backward || direction == TriggerTypes.both) {
                    if (ent.getValue().end <= matchedRule.windowSize)
                        contexts.put(matchedRule.modifier, conTextSpan);
                }

            }
        }
    }


    protected void applyConTexts(List<Span> tokens, HashMap<String, ConTextSpan> matchedRules,
                                 LinkedHashMap<String, ConTextSpan> contexts, TriggerTypes direction,
                                 int conceptBegin) {
        for (Map.Entry<String, ConTextSpan> ent : matchedRules.entrySet()) {
            ContextRule matchedRule = crp.rules.get(ent.getValue().ruleId);
            ConTextSpan relativeSpan = ent.getValue();
            if (matchedRule.triggerType == TriggerTypes.trigger && relativeSpan.matchedDirection == direction) {
                if (direction == TriggerTypes.forward || direction == TriggerTypes.both) {
                    if (conceptBegin - ent.getValue().end <= matchedRule.windowSize)
                        contexts.put(matchedRule.modifier, new ConTextSpan(tokens.get(relativeSpan.begin).begin, tokens.get(relativeSpan.end).end, relativeSpan.ruleId));
                } else if (direction == TriggerTypes.backward || direction == TriggerTypes.both) {
                    if (ent.getValue().begin <= matchedRule.windowSize) {
                        contexts.put(matchedRule.modifier, new ConTextSpan(tokens.get(relativeSpan.begin).begin, tokens.get(relativeSpan.end).end, relativeSpan.ruleId));
                    }
                }

            }
        }
    }

    protected ArrayList<String> convertOutput(LinkedHashMap<String, ConTextSpan> contexts) {
        ArrayList<String> output = new ArrayList<>();
        output.addAll(contexts.keySet());
        return output;
    }

    public LinkedHashMap<String, ConTextSpan> updateFeaturesWEvidence(String conceptTypeName, LinkedHashMap<String, ConTextSpan> contexts) {
        LinkedHashMap<String, ConTextSpan> contextFeatures = new LinkedHashMap<>();
        conceptTypeName = DeterminantValueSet.checkNameSpace(conceptTypeName);
//      set all related features to default values
        while (!crp.conceptFeaturesMap.containsKey(conceptTypeName) && !conceptTypeName.equals(Annotation.class.getSimpleName())) {
            try {
                conceptTypeName = Class.forName(conceptTypeName).getSuperclass().getCanonicalName();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        for (String featureName : crp.conceptFeaturesMap.get(conceptTypeName).getNewFeatureNames()) {
            //  set ruleId to -1 to indicate this is a default modifier value
            contextFeatures.put(featureName, new ConTextSpan(-1, -1, -1));
        }
        //      add linguistic features
        if (crp.conceptFeaturesMap.containsKey("ANY")) {
            for (String featureName : crp.conceptFeaturesMap.get("ANY").getNewFeatureNames()) {
                //  set ruleId to -1 to indicate this is a default modifier value
                contextFeatures.put(featureName, new ConTextSpan(-1, -1, -1));
            }
        } else if (crp.conceptFeaturesMap.containsKey("Annotation")) {
            for (String featureName : crp.conceptFeaturesMap.get("Annotation").getNewFeatureNames()) {
                //  set ruleId to -1 to indicate this is a default modifier value
                contextFeatures.put(featureName, new ConTextSpan(-1, -1, -1));
            }
        }

        for (String modifierValue : contexts.keySet()) {
            String featureName = crp.valueFeatureNameMap.get(modifierValue);
            ContextRule matchedConTextRule = crp.getContextRuleById(contexts.get(modifierValue).ruleId);
            if (contextFeatures.containsKey(featureName) && contextFeatures.get(featureName).ruleId != -1) {
                ContextRule existingConTextRule = crp.getContextRuleById(contextFeatures.get(featureName).ruleId);
                String existingModifier = existingConTextRule.modifier;
                if (crp.valueWeightMap.get(existingModifier) > crp.valueWeightMap.get(modifierValue)
//                        && existingConTextRule.direction == matchedConTextRule.direction
                )
                    continue;
            }
            contextFeatures.put(featureName, contexts.get(modifierValue));
        }

        return contextFeatures;
    }

    public LinkedHashMap<String, String> updateFeaturesValues(String conceptTypeName, LinkedHashMap<String, ConTextSpan> contexts) {
        LinkedHashMap<String, String> featureValueMap = (LinkedHashMap<String, String>) crp.featureDefaultValueMap.clone();
        //      set all related features to default values
        while (!crp.conceptFeaturesMap.containsKey(conceptTypeName) && !conceptTypeName.equals(Annotation.class.getSimpleName())) {
            try {
                conceptTypeName = Class.forName(conceptTypeName).getSuperclass().getSimpleName();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        for (String featureName : crp.conceptFeaturesMap.get(conceptTypeName).getNewFeatureNames()) {
            featureValueMap.put(featureName, crp.featureDefaultValueMap.get(featureName));
        }
//      add linguistic features -- apply to any concept
        if (crp.conceptFeaturesMap.containsKey("ANY")) {
            for (String featureName : crp.conceptFeaturesMap.get("ANY").getNewFeatureNames()) {
                featureValueMap.put(featureName, crp.featureDefaultValueMap.get(featureName));
            }
        } else if (crp.conceptFeaturesMap.containsKey("Annotation")) {
            for (String featureName : crp.conceptFeaturesMap.get("Annotation").getNewFeatureNames()) {
                featureValueMap.put(featureName, crp.featureDefaultValueMap.get(featureName));
            }
        }
//      update processed values
        for (String modifierValue : contexts.keySet()) {
            String featureName = crp.valueFeatureNameMap.get(modifierValue);
            featureValueMap.put(featureName, modifierValue);
        }
        return featureValueMap;
    }

    public LinkedHashMap<String, ConTextSpan> getFullContextFeatures(String conceptTypeName, ArrayList<Span> tokens, int conceptStartPosition,
                                                                     int conceptEndPosition, String text) {
        LinkedHashMap<String, ConTextSpan> contexts = processContextWEvidence(tokens, conceptStartPosition, conceptEndPosition, text, 30);
        return updateFeaturesWEvidence(conceptTypeName, contexts);

    }


    public ContextRule getContextRuleByRuleId(int id) {
        return crp.rules.getOrDefault(id, null);
    }

    public String getContextModifierValueByRuleId(String featureName, int id) {
        String value;
        if (id == -1) {
            value = crp.featureDefaultValueMap.get(featureName);
        } else {
            value = crp.rules.get(id).modifier;
        }
        return value;
    }

    public LinkedHashMap<String, TypeDefinition> getTypeDefinitions() {
        return crp.conceptFeaturesMap;
    }

    public LinkedHashMap<String, String> getFeatureDefaultValueMap() {
        return crp.featureDefaultValueMap;
    }

}
