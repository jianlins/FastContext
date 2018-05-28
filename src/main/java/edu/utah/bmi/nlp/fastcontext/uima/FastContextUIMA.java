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
import edu.utah.bmi.nlp.context.common.ContextRule;
import edu.utah.bmi.nlp.context.common.ContextValueSet;
import edu.utah.bmi.nlp.core.IOUtil;
import edu.utah.bmi.nlp.fastcontext.FastContext;
import org.apache.uima.jcas.tcas.Annotation;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Extend the FastContext to be capable to handle UIMA annotation directly.
 *
 * @author Jianlin Shi
 */
public class FastContextUIMA extends FastContext {
    public static Logger logger = IOUtil.getLogger(FastContext_General_AE.class);


    public FastContextUIMA(String ruleFile) {
        initiate(ruleFile);
    }

    public FastContextUIMA(String ruleFile, boolean caseSensitive) {
        initiate(ruleFile, caseSensitive);
    }

    public FastContextUIMA(ArrayList<String> ruleslist, boolean caseSensitive) {
        initiate(ruleslist, caseSensitive);
    }


    protected FastContextUIMA() {
    }

    public void initiate(String ruleFile) {
        initiate(ruleFile, true);
    }

    public void initiate(String ruleFile, boolean caseSensitive) {
        crp = new UIMAContextRuleProcessor(ruleFile, caseSensitive);
    }

    public void initiate(ArrayList<String> rulesList, boolean caseInsensitive) {
        crp = new UIMAContextRuleProcessor(rulesList);
        crp.setCaseSensitive(caseInsensitive);
        logger.fine(crp.getClass().getCanonicalName());
    }


    public ArrayList<String> processContext(List<Annotation> tokens, int conceptStartPosition,
                                            int conceptEndPosition) {
        ArrayList<String> context = convertOutput(processContextWEvidence(tokens, conceptStartPosition, conceptEndPosition));
        return context;
    }


    public LinkedHashMap<String, ConTextSpan> processContextWEvidence(List<Annotation> tokens, int conceptStartPosition,
                                                                      int conceptEndPosition) {
        List<Annotation> preContext = tokens.subList(0, conceptStartPosition);
        List<Annotation> postContext = tokens.subList(conceptEndPosition + 1, tokens.size());
        LinkedHashMap<String, ConTextSpan> context = processContextWEvidence(preContext, postContext);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Final output: ");
            for (Map.Entry<String, ConTextSpan> entry : context.entrySet()) {
                ConTextSpan evidence = entry.getValue();
                logger.fine("\t" + entry.getKey() + ": " + evidence.begin + "-" + evidence.end);
            }
        }
        return context;
    }

    protected LinkedHashMap<String, ConTextSpan> processContextWEvidence(List<Annotation> preContext, List<Annotation> postContext) {
        LinkedHashMap<String, ConTextSpan> matchedPreRules = new LinkedHashMap<>();
        LinkedHashMap<String, ConTextSpan> matchedPostRules = new LinkedHashMap<>();
        matchedPreRules.clear();
        matchedPostRules.clear();
        ((UIMAContextRuleProcessor) crp).processTokensWRules(preContext, matchedPreRules);
        ((UIMAContextRuleProcessor) crp).processTokensWRules(postContext, matchedPostRules);
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

        applyAnnotationConTexts(preContext, matchedPreRules, contexts, ContextValueSet.TriggerTypes.forward, preContext.size());
        applyAnnotationConTexts(postContext, matchedPostRules, contexts, ContextValueSet.TriggerTypes.backward, preContext.size());

        return contexts;
    }

    protected void applyAnnotationConTexts(List<Annotation> tokens, HashMap<String, ConTextSpan> matchedRules, LinkedHashMap<String, ConTextSpan> contexts, ContextValueSet.TriggerTypes direction, int conceptBegin) {
        for (Map.Entry<String, ConTextSpan> ent : matchedRules.entrySet()) {
            ContextRule matchedRule = crp.rules.get(ent.getValue().ruleId);
            ConTextSpan relativeSpan = ent.getValue();
            if (matchedRule.triggerType == ContextValueSet.TriggerTypes.trigger && relativeSpan.matchedDirection == direction) {
                if (direction == ContextValueSet.TriggerTypes.forward || direction == ContextValueSet.TriggerTypes.both) {
                    if (conceptBegin - ent.getValue().end <= matchedRule.windowSize)
                        contexts.put(matchedRule.modifier, new ConTextSpan(tokens.get(relativeSpan.begin).getBegin(), tokens.get(relativeSpan.end).getEnd(), relativeSpan.ruleId));
                } else if (direction == ContextValueSet.TriggerTypes.backward || direction == ContextValueSet.TriggerTypes.both) {
                    if (ent.getValue().begin <= matchedRule.windowSize) {
//                        if (contexts.containsKey(matchedRule.modifier)) {
//                            ConTextSpan previousSpan = contexts.get(matchedRule.modifier);
//                            ContextRule previousMatchedRule = crp.rules.get(previousSpan.ruleId);
//
//
//                        } else
                        contexts.put(matchedRule.modifier, new ConTextSpan(tokens.get(relativeSpan.begin).getBegin(), tokens.get(relativeSpan.end).getEnd(), relativeSpan.ruleId));
                    } else if (contexts.containsKey(matchedRule.modifier)) {
                        if(contexts.get(matchedRule.modifier).winBegin>ent.getValue().begin)
                            contexts.put(matchedRule.modifier, new ConTextSpan(tokens.get(relativeSpan.begin).getBegin(), tokens.get(relativeSpan.end).getEnd(), relativeSpan.ruleId));
                    }
                }

            }
        }
    }

    public LinkedHashMap<String, ConTextSpan> getFullContextFeatures(String conceptTypeName, List<Annotation> tokens, int conceptStartPosition,
                                                                     int conceptEndPosition) {
        LinkedHashMap<String, ConTextSpan> contexts = processContextWEvidence(tokens, conceptStartPosition, conceptEndPosition);
        return updateFeaturesWEvidence(conceptTypeName, contexts);

    }

    protected LinkedHashMap<String, ConTextSpan> getFullContextFeatures(String conceptTypeName, List<Annotation> preContext, List<Annotation> postContext) {
        LinkedHashMap<String, ConTextSpan> contexts = processContextWEvidence(preContext, postContext);
        return updateFeaturesWEvidence(conceptTypeName, contexts);
    }


}
