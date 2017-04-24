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
import edu.utah.bmi.nlp.context.common.ContextValueSet.TriggerTypes;
import edu.utah.bmi.nlp.context.common.ContextRule;
import edu.utah.bmi.nlp.context.common.ConTextSpan;
import edu.utah.bmi.nlp.core.Span;

import java.util.*;

/**
 * This class take context token/Annotation ArrayList as input, process the ConText algorithm.
 *
 * @author Jianlin Shi
 */
public class FastContext implements ConTextAdvancedInterface {
    protected ContextRules contextRules;
    public boolean debug = false;


    public FastContext(String ruleFile) {
        initiate(ruleFile);
    }

    protected FastContext() {
    }

    public FastContext(String ruleFile, boolean lowerCase) {
        contextRules = new ContextRules(ruleFile, lowerCase);
    }

    public FastContext(ArrayList<String> ruleslist, boolean lowerCase) {
        initiate(ruleslist, lowerCase);
    }

    public void initiate(String ruleFile) {
        contextRules = new ContextRules(ruleFile);
    }

    public void initiate(ArrayList<String> rulesList) {
        contextRules = new ContextRules(rulesList);
    }

    public void initiate(ArrayList<String> rulesList, boolean lowerCase) {
        contextRules = new ContextRules(rulesList);
        contextRules.setCaseInsensitive(lowerCase);
    }


    public ArrayList<String> processContext(ArrayList<String> tokens, int conceptStartPosition, int conceptEndPosition, int windowsize) {
        ArrayList<String> context = convertOutput(processContextWEvidence(tokens, conceptStartPosition, conceptEndPosition, windowsize));
        return context;
    }

    public ArrayList<String> processContext(ArrayList<Span> tokens, int conceptStartPosition,
                                            int conceptEndPosition, String text, int windowsize) {
        ArrayList<String> context = convertOutput(processContextWEvidence(tokens, conceptStartPosition, conceptEndPosition, text, windowsize));
        return context;
    }

    public ArrayList<String> processContext(String sentence, int conceptBegin, int conceptEnd, int windowsize) {
        String preContextString = sentence.substring(0, conceptBegin);
        String postContextString = sentence.substring(conceptEnd, sentence.length());
        ArrayList<String> preContextTokens = new ArrayList<String>();
        ArrayList<String> postContextTokens = new ArrayList<String>();
        if (preContextString.length() > 0) {
            preContextTokens.addAll(Arrays.asList(preContextString.split("\\s+")));
        }
        if (postContextString.length() > 0) {
            postContextTokens.addAll(Arrays.asList(postContextString.split("\\s+")));
        }
        ArrayList<String> context = convertOutput(processContextWEvidence(preContextTokens, postContextTokens, windowsize));
        return context;
    }


    protected ArrayList<String> convertOutput(LinkedHashMap<String, ConTextSpan> contexts) {
        ArrayList<String> output = new ArrayList<String>();
        output.addAll(contexts.keySet());
        return output;
    }


    public LinkedHashMap<String, ConTextSpan> processContextWEvidence(ArrayList<Span> tokens, int conceptStartPosition,
                                                                      int conceptEndPosition, String text, int windowsize) {
        ArrayList<String> preContext = new ArrayList<String>();
        ArrayList<String> postContext = new ArrayList<String>();
        for (int i = 0; i < conceptStartPosition; i++) {
            preContext.add(text.substring(tokens.get(i).begin, tokens.get(i).end));
        }
        for (int i = conceptEndPosition + 1; i < tokens.size(); i++) {
            postContext.add(text.substring(tokens.get(i).begin, tokens.get(i).end));
        }
        LinkedHashMap<String, ConTextSpan> context = processContextWEvidence(preContext, postContext, windowsize);
        return context;
    }

    public LinkedHashMap<String, ConTextSpan> processContextWEvidence(ArrayList<String> tokens, int conceptStartPosition, int conceptEndPosition, int windowsize) {
        ArrayList<String> preContext = new ArrayList<String>(tokens.subList(0, conceptStartPosition));
//		System.out.println(preContext);
        ArrayList<String> postContext = new ArrayList<String>();
        if (conceptEndPosition < tokens.size())
            postContext = new ArrayList<String>(tokens.subList(conceptEndPosition + 1, tokens.size()));
//		System.out.println(postContext);
        LinkedHashMap<String, ConTextSpan> context = processContextWEvidence(preContext, postContext, windowsize);
        return context;
    }

    protected LinkedHashMap<String, ConTextSpan> processContextWEvidence(ArrayList<String> preContext, ArrayList<String> postContext,
                                                                         int windowsize) {
        LinkedHashMap<String, ConTextSpan> matchedPreRules = new LinkedHashMap<String, ConTextSpan>();
        LinkedHashMap<String, ConTextSpan> matchedPostRules = new LinkedHashMap<String, ConTextSpan>();
        matchedPreRules.clear();
        matchedPostRules.clear();
        contextRules.processRules(preContext, 0, matchedPreRules);
        contextRules.processRules(postContext, 0, matchedPostRules);
        if (debug) {
            System.out.println("pre context matches:");
            for (Map.Entry<String, ConTextSpan> ent : matchedPreRules.entrySet()) {
                System.out.println(ent.getValue().ruleId + ": " + ent.getKey() + " " + contextRules.rules.get(ent.getValue().ruleId).triggerType + ":\t" + ent.getValue().begin + "-" + ent.getValue().end);
                System.out.println(contextRules.rules.get(ent.getValue().ruleId) + "\n");
            }
            System.out.println("post context matches:");
            for (Map.Entry<String, ConTextSpan> ent : matchedPostRules.entrySet()) {
                System.out.println(ent.getValue().ruleId + ": " + ent.getKey() + " " + contextRules.rules.get(ent.getValue().ruleId).triggerType + ":\t" + ent.getValue().begin + "-" + ent.getValue().end);
                System.out.println(contextRules.rules.get(ent.getValue().ruleId) + "\n");
            }

        }
        LinkedHashMap<String, ConTextSpan> context = applyConTexts(matchedPreRules, matchedPostRules, preContext.size());
        return context;
    }

    protected LinkedHashMap<String, ConTextSpan> applyConTexts(HashMap<String, ConTextSpan> matchedPreRules,
                                                               HashMap<String, ConTextSpan> matchedPostRules, int conceptBegin) {
        LinkedHashMap<String, ConTextSpan> contexts = new LinkedHashMap<String, ConTextSpan>();
        applyConTexts(matchedPreRules, contexts, TriggerTypes.forward, conceptBegin);
        applyConTexts(matchedPostRules, contexts, TriggerTypes.backward, conceptBegin);
        return contexts;
    }

    protected void applyConTexts(HashMap<String, ConTextSpan> matchedRules, LinkedHashMap<String, ConTextSpan> contexts, TriggerTypes direction, int conceptBegin) {
        for (Map.Entry<String, ConTextSpan> ent : matchedRules.entrySet()) {
            ContextRule matchedRule = contextRules.rules.get(ent.getValue().ruleId);
            if (matchedRule.triggerType == TriggerTypes.trigger && (matchedRule.direction == direction || matchedRule.direction == TriggerTypes.both)) {
                if (direction == TriggerTypes.forward || direction == TriggerTypes.both) {
                    if (conceptBegin - ent.getValue().begin <= matchedRule.windowSize)
                        contexts.put(matchedRule.modifier, ent.getValue());
                } else if (direction == TriggerTypes.backward || direction == TriggerTypes.both) {
                    if (ent.getValue().end <= matchedRule.windowSize)
                        contexts.put(matchedRule.modifier, ent.getValue());
                }

            }
        }
    }


}
