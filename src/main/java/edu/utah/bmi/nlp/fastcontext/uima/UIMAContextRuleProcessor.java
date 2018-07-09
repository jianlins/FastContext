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
import edu.utah.bmi.nlp.fastcontext.ContextRuleProcessor;
import org.apache.uima.jcas.tcas.Annotation;

import java.util.*;

/**
 * Process directly on UIMA annotations without conversion to Spans.
 *
 * @author Jianlin Shi on 5/2/17.
 */
public class UIMAContextRuleProcessor extends ContextRuleProcessor {
    public UIMAContextRuleProcessor(String ruleFileName) {
        super(ruleFileName);
    }

    public UIMAContextRuleProcessor(String ruleFileName, String splitter) {
        super(ruleFileName, splitter);
    }

    public UIMAContextRuleProcessor(ArrayList<String> ruleslist) {
        super(ruleslist);
    }

    public UIMAContextRuleProcessor(String ruleFileName, boolean caseSensitive) {
        super(ruleFileName, caseSensitive);
    }

    public UIMAContextRuleProcessor(ArrayList<String> ruleslist, boolean caseInsensitive) {
        super(ruleslist, caseInsensitive);
    }

    public void processTokensWRules(List<Annotation> contextTokens, LinkedHashMap<String, ConTextSpan> matches) {
        processTokensWRules(contextTokens, 0, matches);

    }

    public void processTokensWRules(List<Annotation> contextTokens, int startposition, LinkedHashMap<String, ConTextSpan> matches) {
        // use the first "startposition" to remember the original start matching
        // position.
        // use the 2nd one to remember the start position in which recursion.


        for (int i = startposition; i < contextTokens.size(); i++) {
            // System.out.println(contextTokens.get(i));
            processTokensWRules(contextTokens, rulesMap, i, i, matches);
        }

    }

    protected void processTokensWRules(List<Annotation> contextTokens, HashMap rule, int matchBegin, int currentPosition,
                                       LinkedHashMap<String, ConTextSpan> matches) {
        // when reach the end of the tunedcontext, end the iteration
        if (currentPosition < contextTokens.size()) {
            // start processing the tunedcontext tokens
            String thisToken = contextTokens.get(currentPosition).getCoveredText();
            if(thisToken.trim().length()==0){
//                in case the token is not tokenized correctly
                processTokensWRules(contextTokens, rule, matchBegin, currentPosition + 1, matches);
                return;
            }
            if (!caseSensitive)
                thisToken = thisToken.toLowerCase();
//			System.out.println("thisToken-"+thisToken+"<");
            if (rule.containsKey("\\w+")) {
                processTokensWRules(contextTokens, (HashMap) rule.get("\\w+"), matchBegin, currentPosition + 1, matches);
            }
            if (rule.containsKey("\\W+") && isUpperCase(thisToken)) {
                processTokensWRules(contextTokens, (HashMap) rule.get("\\W+"), matchBegin, currentPosition + 1, matches);
            }
            // if the end of a rule is met
            if (rule.containsKey(END)) {
                addDeterminants(rule, matches, matchBegin, currentPosition,contextTokens.size());
            }
            // if the current token match the element of a rule
            if (rule.containsKey(thisToken)) {
                processTokensWRules(contextTokens, (HashMap) rule.get(thisToken), matchBegin, currentPosition + 1, matches);
            }
            if (rule.containsKey("\\>") && Character.isDigit(thisToken.charAt(0))) {
                processDigitTokens(contextTokens, '>',(HashMap) rule.get("\\>"), matchBegin, currentPosition, matches);
            }
            if (rule.containsKey("\\<") && Character.isDigit(thisToken.charAt(0))) {
                processDigitTokens(contextTokens, '<',(HashMap) rule.get("\\<"), matchBegin, currentPosition, matches);
            }
        } else if (currentPosition == contextTokens.size() && rule.containsKey(END)) {
            addDeterminants(rule, matches, matchBegin, currentPosition,contextTokens.size());
        }
    }

    protected void processDigitTokens(List<Annotation> contextTokens, char compare,HashMap rule, int matchBegin, int currentPosition,
                                      LinkedHashMap<String, ConTextSpan> matches) {
        String thisToken=contextTokens.get(currentPosition).getCoveredText();
        if(!caseSensitive)
            thisToken=thisToken.toLowerCase();
        mt = pdigit.matcher(thisToken);
        if (mt.find()) {
            double thisDigit;
//			prevent length over limit
            if (mt.group(1).length() < 4) {
                thisDigit = Double.parseDouble(mt.group(1));
            } else {
                thisDigit = 1000;
            }
            Set<String> numbers = rule.keySet();
            for (String num : numbers) {
                double ruleDigit = Double.parseDouble(num);
                if ((compare == '>' && thisDigit > ruleDigit) || (compare == '<' && thisDigit < ruleDigit)) {
                    if (mt.group(2) == null) {
                        // if this token is a number
                        processTokensWRules(contextTokens, (HashMap) rule.get(num), matchBegin, currentPosition + 1, matches);
                    } else {
                        // thisToken is like "30-days"
                        HashMap ruletmp = (HashMap) rule.get(ruleDigit + "");
                        String subtoken = mt.group(2).substring(1);
                        if (ruletmp.containsKey(subtoken)) {
                            processTokensWRules(contextTokens, (HashMap) ruletmp.get(subtoken), matchBegin, currentPosition + 1, matches);
                        }
                    }
                }
            }
        }
    }

}
