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
package edu.utah.bmi.fastcontext;

import edu.utah.bmi.context.common.ConTextSpan;
import edu.utah.bmi.context.common.ContextRule;
import edu.utah.bmi.context.common.ContextValueSet.TriggerTypes;
import edu.utah.bmi.context.common.IOUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is to construct the chained-up HashMaps structure for the rulesMap, and provide methods to
 * process the rulesMap
 *
 * @author Jianlin Shi
 *         The results will be added to the input HashMap<Determinants,Span>, because there might be more than one applicable rule.
 *         -The Span ( @see Span#Span(int, int) ) stores the span information of the evidence support the corresponding Determinants
 *         -Determinants are defined in ContextValueSet.Determinants ( @see ContextValueSet#ContextValueSet()), which is corresponding
 *         to the last two elements in each rule defined in the rule CSV file.
 */
@SuppressWarnings("rawtypes")
public class ContextRules {
    //   the nested map structure for rule processing
    private HashMap rulesMap = new HashMap();
    //   map a rule to its corresponding line number
    public LinkedHashMap<Integer, ContextRule> rules = new LinkedHashMap<Integer, ContextRule>();


    private Pattern pdigit;
    private Matcher mt;
    private final String END = "<END>";
    protected boolean toLowerCase = false;

    public ContextRules(String ruleFileName) {
        initiate(IOUtil.readRuleFile(ruleFileName));
    }

    public ContextRules(String ruleFileName, String splitter) {
        initiate(IOUtil.readCSVRuleFile(ruleFileName, splitter));
    }

    public ContextRules(ArrayList<String> ruleslist) {
        initiate(IOUtil.convertListToRuleMap(ruleslist, "\\|"));
    }


    public ContextRules(String ruleFileName, boolean toLowerCase) {
        this.toLowerCase = toLowerCase;
        initiate(IOUtil.readRuleFile(ruleFileName));
    }

    public ContextRules(ArrayList<String> ruleslist, boolean toLowerCase) {
        this.toLowerCase = toLowerCase;
        initiate(IOUtil.convertListToRuleMap(ruleslist, "\\|"));
    }


    public void setCaseInsensitive(boolean toLowerCase) {
        this.toLowerCase = toLowerCase;
    }


    private void initiate(LinkedHashMap<Integer, ContextRule> rules) {
        this.rules = rules;
        rulesMap.clear();
        if (pdigit == null)
            pdigit = Pattern.compile("(\\d+)(-\\w+)?");
        for (ContextRule rule : rules.values()) {
            if (rule.getDirection() == TriggerTypes.both) {
                rule.setDirection(TriggerTypes.forward);
                addRule(rule);
                rule.setDirection(TriggerTypes.backward);
                addRule(rule);
                rule.setDirection(TriggerTypes.both);
            } else
                addRule(rule);
        }
    }


    /**
     * @param rule
     * @return true: if the rule is added
     * false: if the rule is a duplicate
     */
    private boolean addRule(ContextRule rule) {
        // use to store the HashMap sub-chain that have the key chain that meet
        // the rule[]
        ArrayList<HashMap> rules_tmp = new ArrayList<HashMap>();
        HashMap rule1 = rulesMap;
        HashMap rule2 = new HashMap();
        HashMap rulet = new HashMap();
        if (toLowerCase)
            rule.rule = rule.rule.toLowerCase();
        String[] ruleContent = rule.rule.split("\\s+");
        int length = ruleContent.length;
        int i = 0;
        rules_tmp.add(rulesMap);
        while (i < length && rule1 != null && rule1.containsKey(ruleContent[i])) {
            rule1 = (HashMap) rule1.get(ruleContent[i]);
            i++;
        }
        // if the rule has been included
        if (i > length)
            return false;
        // start with the determinant, construct the last descendant HashMap
        // <Determinant, null>
        if (i == length) {
            if (rule1.containsKey(END)) {
                ((HashMap) rule1.get(END)).put(rule.determinant, rule.id);
            } else {
                rule2.put(rule.determinant, rule.id);
                rule1.put(END, rule2.clone());
            }
            return true;
        } else {
            rule2.put(rule.determinant, rule.id);
            rule2.put(END, rule2.clone());
            rule2.remove(rule.determinant);

            // filling the HashMap chain which rules doesn't have the key chain
            for (int j = length - 1; j > i; j--) {
                rulet = (HashMap) rule2.clone();
                rule2.clear();
                rule2.put(ruleContent[j], rulet);
            }
        }
        rule1.put(ruleContent[i], rule2.clone());
        return true;
    }

    /**
     * @param contextTokens
     * @param startposition
     * @param matches
     */
    public void processRules(ArrayList<String> contextTokens, int startposition, LinkedHashMap<String, ConTextSpan> matches,
                             boolean preferRight) {
        // use the first "startposition" to remember the original start matching
        // position.
        // use the 2nd one to remember the start position in which recursion.
        if (toLowerCase)
            for (int i = startposition; i < contextTokens.size(); i++) {
                contextTokens.set(i, contextTokens.get(i).toLowerCase());
            }
        for (int i = startposition; i < contextTokens.size(); i++) {
            // System.out.println(contextTokens.get(i));
            processRules(contextTokens, rulesMap, i, i, matches, preferRight);
        }

    }

    /**
     * @param contextTokens
     * @param rule
     * @param matchBegin
     * @param startPosition
     * @param matches       K: determinant
     *                      V: position of the last token that matches the rule in the input
     *                      ArrayList
     * @param preferRight
     */
    private void processRules(ArrayList<String> contextTokens, HashMap rule, int matchBegin, int startPosition,
                              LinkedHashMap<String, ConTextSpan> matches, boolean preferRight) {
        // when reach the end of the tunedcontext, end the iteration
        if (startPosition < contextTokens.size()) {
            // start processing the tunedcontext tokens
            String thisToken = contextTokens.get(startPosition);
//			System.out.println("thisToken-"+thisToken+"<");
            if (rule.containsKey("\\w+")) {
                processRules(contextTokens, (HashMap) rule.get("\\w+"), matchBegin, startPosition + 1, matches, preferRight);
            }
            if (rule.containsKey("\\W+") && isUpperCase(thisToken)) {
                processRules(contextTokens, (HashMap) rule.get("\\W+"), matchBegin, startPosition + 1, matches, preferRight);
            }
            // if the end of a rule is met
            if (rule.containsKey(END)) {
                addDeterminants(rule, matches, matchBegin, startPosition, preferRight);
            }
            // if the current token match the element of a rule
            if (rule.containsKey(thisToken)) {
                processRules(contextTokens, (HashMap) rule.get(thisToken), matchBegin, startPosition + 1, matches,
                        preferRight);
            }
            if (rule.containsKey(">") && Character.isDigit(thisToken.charAt(0))) {
                processDigits(contextTokens, (HashMap) rule.get(">"), matchBegin, startPosition, matches, preferRight);
            }
        } else if (startPosition == contextTokens.size() && rule.containsKey(END)) {
            addDeterminants(rule, matches, matchBegin, startPosition, preferRight);
        }
    }

    /**
     * Because the digit regular expressions are revised into the format like
     * "> 13 days", the rulesMap need to be handled differently to the token
     * matching
     * rulesMap
     *
     * @param contextTokens
     * @param rule
     * @param matchBegin
     * @param i
     * @param matches
     */
    private void processDigits(ArrayList<String> contextTokens, HashMap rule, int matchBegin, int i,
                               LinkedHashMap<String, ConTextSpan> matches, boolean preferRight) {
        mt = pdigit.matcher(contextTokens.get(i));
        if (mt.find()) {
            int thisDigit;
//			prevent length over limit
            if (mt.group(1).length() < 4) {
                thisDigit = Integer.parseInt(mt.group(1));
            } else {
                thisDigit = 1000;
            }
            Set<String> numbers = rule.keySet();
            for (String num : numbers) {
                int ruleDigit = Integer.parseInt(num);
                if (thisDigit > ruleDigit) {
                    if (mt.group(2) == null) {
                        // if this token is a number
                        processRules(contextTokens, (HashMap) rule.get(ruleDigit + ""), matchBegin, i + 1, matches,
                                preferRight);
                    } else {
                        // thisToken is like "30-days"
                        HashMap ruletmp = (HashMap) rule.get(ruleDigit + "");
                        String subtoken = mt.group(2).substring(1);
                        if (ruletmp.containsKey(subtoken)) {
                            processRules(contextTokens, (HashMap) ruletmp.get(subtoken), matchBegin, i + 1, matches,
                                    preferRight);
                        }
                    }
                }
            }
        }
    }

    /**
     * if reaches the end of one or more rulesMap, add all corresponding
     * determinants into the results
     * <p/>
     * The priority of multiple applicable rulesMap can be modified. This version
     * uses the following three rulesMap:
     * 1. if determinant spans overlap, choose the determinant with the widest
     * span
     * 2. else if prefer right determinant, choose the determinant with the
     * largest end.
     * 3. else if prefer left determinant, choose the determinant with the
     * smallest begin.
     *
     * @param rule
     * @param matches
     * @param matchBegin
     * @param i
     * @param preferRight
     */
    private void addDeterminants(HashMap rule, LinkedHashMap<String, ConTextSpan> matches, int matchBegin, int i,
                                 boolean preferRight) {
        HashMap<String, ?> matchedRules = (HashMap<String, ?>) rule.get(END);

//        int id = (Integer) rule.values().iterator().next();
//        ContextRule matchedRule = rules.get(id);
//        Span currentSpan = new Span(matchBegin, i - 1, id);
        for (String key : matchedRules.keySet()) {
            ConTextSpan originalSpan = null;
            int id = (Integer) matchedRules.get(key);
            char matchedDirection = key.charAt(0);
            ConTextSpan currentSpan = new ConTextSpan(matchBegin, i - 1, id);
            if (matches.containsKey(key)) {
                originalSpan = matches.get(key);
                switch (matchedDirection) {
                    case 'f':
                        if (originalSpan.begin >= currentSpan.end) {
                            continue;
                        } else if (originalSpan.width > currentSpan.width && originalSpan.end >= currentSpan.begin) {
                            continue;
                        }
                        break;
                    case 'b':
                        if (originalSpan.end <= currentSpan.begin) {
                            continue;
                        } else if (originalSpan.width > currentSpan.width && originalSpan.begin >= currentSpan.end) {
                            continue;
                        }
                        break;
                }

                ContextRule matchedRule = rules.get(id);
//                TODO need more test on the window size adjustment
                //  adjust context window, for later version that support combining modifiers with shared context window terminals
                if (originalSpan.begin != -1 && (matchedDirection == 'f')) {
                    if (matchedRule.triggerType == TriggerTypes.terminal) {
                        currentSpan.winBegin = originalSpan.winBegin > currentSpan.end ? originalSpan.winBegin : currentSpan.end;
                    } else {
                        currentSpan.winBegin = originalSpan.winBegin;
                    }
                } else if (originalSpan.end != -1 && (matchedDirection == 'b')) {
                    if (matchedRule.triggerType == TriggerTypes.terminal) {
                        currentSpan.winEnd = originalSpan.winEnd < currentSpan.winEnd ? originalSpan.winEnd : currentSpan.begin;
                    } else {
                        currentSpan.winEnd = originalSpan.winEnd;
                    }
                }
            }
            matches.put(key, currentSpan);
        }

    }

    protected boolean isUpperCase(String token) {
        boolean res = true;
        for (int i = 0; i < token.length(); i++) {
            if (!Character.isUpperCase(token.charAt(i))) {
                res = false;
                break;
            }
        }
        return res;
    }

}
