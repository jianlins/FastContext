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
import org.junit.Test;

import java.util.*;


/**
 * @author Jianlin Shi
 *         Created on 8/24/15.
 */
public class TestFastContext {
    @Test
    public void test1() {
        ArrayList<String> rules = new ArrayList<>();
        rules.add("hx of|forward|positive|neg|30");
        rules.add("presented with|forward|terminal|neg|30");
        ContextRules cr = new ContextRules(rules);
        String input = "with previous hx of anxiety/depression, HTN who presented with encephalopathy followi";
        ArrayList<String> sent = new ArrayList<String>();
        sent.addAll(Arrays.asList(input.split("\\s+")));
        LinkedHashMap<String, ConTextSpan> matches = new LinkedHashMap<String, ConTextSpan>();
        cr.processRules(sent, 0, matches, true);
        ConTextSpan conTextSpan = matches.get("forward_neg");
        assert (conTextSpan.begin == 7 && conTextSpan.end == 8 && conTextSpan.ruleId == 2);
    }

    @Test
    public void test2() {
        ArrayList<String> rules = new ArrayList<>();
        rules.add("hx of|forward|positive|neg|30");
        rules.add("presented with|forward|terminal|neg|30");
        FastContext fc = new FastContext(rules, true);
        String input = "previous hx of anxiety/depression, HTN who presented with encephalopathy following suspected intentional medication overdose/toxicity, suicidal attempt, alcohol intoxication.";
        ArrayList<String> sent = new ArrayList<String>();
        sent.addAll(Arrays.asList(input.split("\\s+")));
        LinkedHashMap<String, ConTextSpan> matches = new LinkedHashMap<String, ConTextSpan>();
        matches = fc.processContextWEvidence(sent, 3, 3, 30);
        ConTextSpan conTextSpan = matches.get("neg");
        assert (conTextSpan.begin == 1 && conTextSpan.end == 2 && conTextSpan.ruleId == 1);
        matches = fc.processContextWEvidence(sent, 9, 9, 30);
        assert (matches.size() == 0);
    }


}