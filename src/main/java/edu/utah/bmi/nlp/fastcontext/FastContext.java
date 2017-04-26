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
import edu.utah.bmi.nlp.core.SimpleTokenizer;
import edu.utah.bmi.nlp.core.Span;

import java.util.*;

/**
 * This class take context token/Annotation ArrayList as input, process the ConText algorithm.
 *
 * @author Jianlin Shi
 */
public class FastContext implements ConTextAdvancedInterface {
    protected ContextRuleProcessor crp;
    public boolean debug = false;


    public FastContext(String ruleFile) {
        initiate(ruleFile);
    }

    protected FastContext() {
    }

    public FastContext(String ruleFile, boolean lowerCase) {
        crp = new ContextRuleProcessor(ruleFile, lowerCase);
    }

    public FastContext(ArrayList<String> ruleslist, boolean lowerCase) {
        initiate(ruleslist, lowerCase);
    }

    public void initiate(String ruleFile) {
        crp = new ContextRuleProcessor(ruleFile);
    }

    public void initiate(ArrayList<String> rulesList) {
        initiate(rulesList, true);

    }

    public void initiate(ArrayList<String> rulesList, boolean caseInsensitive) {
        crp = new ContextRuleProcessor(rulesList);
        crp.setCaseInsensitive(caseInsensitive);
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
        ArrayList<Span> preContextTokens = SimpleTokenizer.tokenizeOnWhitespaces(preContextString);
        ArrayList<Span> conceptTokens = SimpleTokenizer.tokenizeOnWhitespaces(conceptString, conceptBeginOffset);
        ArrayList<Span> postContextTokens = SimpleTokenizer.tokenizeOnWhitespaces(postContextString, conceptEndOffset);
        return processContextWEvidence(preContextTokens, postContextTokens, preContextTokens.size() + conceptTokens.size() - 1, true);
    }


    private LinkedHashMap<String, ConTextSpan> processContextWEvidence(List<Span> preContext, List<Span> postContext, int conceptEndPosition, boolean absoluteOffsetSpan) {
        LinkedHashMap<String, ConTextSpan> matchedPreRules = new LinkedHashMap<>();
        LinkedHashMap<String, ConTextSpan> matchedPostRules = new LinkedHashMap<>();
        matchedPreRules.clear();
        matchedPostRules.clear();
        crp.processRules(preContext, 0, matchedPreRules);
        crp.processRules(postContext, 0, matchedPostRules);
        if (debug) {
            System.out.println("pre context matches:");
            for (Map.Entry<String, ConTextSpan> ent : matchedPreRules.entrySet()) {
                System.out.println(ent.getValue().ruleId + ": " + ent.getKey() + " " + crp.rules.get(ent.getValue().ruleId).triggerType + ":\t" + ent.getValue().begin + "-" + ent.getValue().end);
                System.out.println(crp.rules.get(ent.getValue().ruleId) + "\n");
            }
            System.out.println("post context matches:");
            for (Map.Entry<String, ConTextSpan> ent : matchedPostRules.entrySet()) {
                System.out.println(ent.getValue().ruleId + ": " + ent.getKey() + " " + crp.rules.get(ent.getValue().ruleId).triggerType + ":\t" + ent.getValue().begin + "-" + ent.getValue().end);
                System.out.println(crp.rules.get(ent.getValue().ruleId) + "\n");
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


    private void applyConTexts(int contextOffset, HashMap<String, ConTextSpan> matchedRules, LinkedHashMap<String, ConTextSpan> contexts, TriggerTypes direction, int conceptBegin) {
        for (Map.Entry<String, ConTextSpan> ent : matchedRules.entrySet()) {
            ConTextSpan conTextSpan = ent.getValue();
            if (contextOffset > 0) {
                conTextSpan.begin = conTextSpan.begin + contextOffset;
                conTextSpan.end = conTextSpan.end + contextOffset;
            }
            ContextRule matchedRule = crp.rules.get(conTextSpan.ruleId);
            if (matchedRule.triggerType == TriggerTypes.trigger && (matchedRule.direction == direction || matchedRule.direction == TriggerTypes.both)) {
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


    protected void applyConTexts(List<Span> tokens, HashMap<String, ConTextSpan> matchedRules, LinkedHashMap<String, ConTextSpan> contexts, TriggerTypes direction, int conceptBegin) {
        for (Map.Entry<String, ConTextSpan> ent : matchedRules.entrySet()) {
            ContextRule matchedRule = crp.rules.get(ent.getValue().ruleId);
            ConTextSpan relativeSpan = ent.getValue();
            if (matchedRule.triggerType == TriggerTypes.trigger && (matchedRule.direction == direction || matchedRule.direction == TriggerTypes.both)) {
                if (direction == TriggerTypes.forward || direction == TriggerTypes.both) {
                    if (conceptBegin - ent.getValue().begin <= matchedRule.windowSize)
                        contexts.put(matchedRule.modifier, new ConTextSpan(tokens.get(relativeSpan.begin).begin, tokens.get(relativeSpan.end).end, relativeSpan.ruleId));
                } else if (direction == TriggerTypes.backward || direction == TriggerTypes.both) {
                    if (ent.getValue().end <= matchedRule.windowSize) {
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


}
