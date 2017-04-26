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

import edu.utah.bmi.nlp.context.common.ConTextSpan;
import edu.utah.bmi.nlp.core.SimpleTokenizer;
import edu.utah.bmi.nlp.core.Span;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;


/**
 * Try different cases to test
 *
 * @author Jianlin Shi
 *         Created on 8/24/15.
 */
public class TestFastContextExampleCases {
    private FastContext fc;
    private ArrayList<String> rules;
    private String inputString;

    @Before
    public void init() {
        rules = new ArrayList<>();
    }

    @Test
    public void testPseudoNeg() {
        rules.clear();
        rules.add("free|backward|trigger|negated|10");
        rules.add("free air|both|pseudo|negated|10");
        fc = new FastContext(rules, true);
        inputString = "He is smoke free .";
        assert (eval(inputString, 2, 2, "negated", "free"));
        inputString = "He needs some free air to relax .";
        assert (eval(inputString, 6, 6, "negated", null));
    }


    @Test
    public void testPseudoUncertain() {
        rules.clear();
        rules.add("may be contributing|both|pseudo|uncertain|30");
        rules.add("may be|forward|trigger|uncertain|30");
        fc = new FastContext(rules, true);
//        fc.debug = true;
        inputString = "The fever may be caused by the infection .";
        assert (eval(inputString, 7, 7, "uncertain", "may be"));
        inputString = "The infection may be contributing his fever .";
        assert (eval(inputString, 6, 6, "uncertain", null));
    }

    @Test
    public void testPseudoHistorical() {
        rules.clear();
        rules.add("history of present illness|forward|pseudo|historical|30");
        rules.add("history of|forward|trigger|historical|30");
        fc = new FastContext(rules, true);
//        fc.debug = true;
        inputString = "The patient has a long history of HTN .";
//        assert (eval(inputString, 7, 7, "historical", "history of"));
        inputString = "History of Present Illness : The patient is a 78-year-old gentleman with abdomen pain .";
        assert (eval(inputString, 12, 13, "historical", null));
    }


    @Test
    public void testTerminationNegated() {
        rules.clear();
        rules.add("did have|forward|termination|negated|10");
        rules.add("denied|forward|trigger|negated|30");
        fc = new FastContext(rules, true);
        inputString = "He denied any drug abuse, as well as heavy drink history .";
        assert (eval(inputString, 8, 10, "negated", "denied"));
        inputString = "He denied any drug abuse, but did have heavy drink history .";
        assert (eval(inputString, 8, 10, "negated", null));
    }

    @Test
    public void testTerminationHistorical(){
        rules.clear();
        rules.add("presenting|both|termination|historical|30");
        rules.add("history of|forward|trigger|historical|30");
        fc = new FastContext(rules, true);
        inputString = "The patient is 45 yo male with history of HTN and diabetes .";
        assert (eval(inputString, 11, 11, "historical", "history of"));
        inputString = "The patient is 45 yo male with history of HTN , presenting with check pain .";
        assert (eval(inputString, 13, 14, "historical", null));
    }

    @Test
    public void testMixContext(){
        rules.clear();
        rules.add("did have|forward|termination|negated|10");
        rules.add("history of|forward|trigger|historical|30");
        rules.add("denied|forward|trigger|negated|30");
        fc = new FastContext(rules, true);
        inputString = "He denied history of smoking .";
        assert (eval(inputString, 4, 4, "historical", "history of"));
        assert (eval(inputString, 4, 4, "negated", "denied"));

    }

    private boolean eval(String inputString, int conceptBegin, int conceptEnd, String contextType, String contextString) {
        ArrayList<Span> sent = SimpleTokenizer.tokenizeOnWhitespaces(inputString);
        LinkedHashMap<String, ConTextSpan> matches = fc.processContextWEvidence(sent, conceptBegin, conceptEnd, inputString, 30);
        ConTextSpan conTextSpan = matches.get(contextType);
        if (conTextSpan != null) {
            String contextStr = inputString.substring(conTextSpan.begin, conTextSpan.end);
            return contextStr.equals(contextString);
        } else if (contextString == null) {
            return true;
        }
        return false;
    }

}