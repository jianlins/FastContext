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
import edu.utah.bmi.nlp.core.SimpleParser;
import edu.utah.bmi.nlp.core.Span;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    @BeforeEach
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
        assert (eval(inputString, 6, 6, "affirm", null));
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
        rules.add("presenting|forward|termination|historical|30");
        rules.add("history of|forward|trigger|historical|30");
        fc = new FastContext(rules, true);
        inputString = "The patient is 45 yo male with history of HTN and diabetes .";
        assert (eval(inputString, 11, 11, "historical", "history of"));
        inputString = "The patient is 45 yo male with history of HTN , presenting with check pain .";
        assert (eval(inputString, 13, 14, "historical", null));
    }

    @Test
    public void testTerminationHistorical2(){
        rules.clear();
        rules.add("normal|backward|termination|negated|30");
        rules.add("absent|backward|trigger|negated|30");
        fc = new FastContext(rules, true);
        inputString = "The BP is absent";
        assert (eval(inputString, 1,1, "negated", "absent"));
        inputString = "The BP is normal , R is absent";
        assert (eval(inputString, 1, 1, "negated", null));
    }

    @Test
    public void testTerminationHistorical3(){
        rules.clear();

        rules.add("not|backward|termination|deep|30");
        rules.add("deep|both|trigger|deep|30");


        fc = new FastContext(rules, false);

        fc.debug=true;
        inputString = "The thrombus extends up to the axillary confluence does not enter into the deep venous system";
        assert (eval(inputString, 1,1, "deep", null));
    }


    @Test
    public void testDigit() {
        rules.clear();
        rules.add(">|forward|trigger|negated|10");
        rules.add("\\> 5|forward|trigger|historical|10");
        fc = new FastContext(rules, true);
        inputString = "He is  > 6 smoke.";
        assert (eval(inputString, 4, 4, "negated", ">"));
        assert (eval(inputString, 4, 4, "historical", "6"));
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

    @Test
    public void testMixContext2(){
        rules.clear();
        rules.add("no|backward|termination|negated|10");
        rules.add(": no|backward|trigger|negated|30");
        rules.add("no|forward|trigger|negated|30");

        fc = new FastContext(rules, true);
        fc.debug=true;
        inputString = "He history of smoking : no .";
        assert (eval(inputString, 3, 3, "negated", ": no"));
//        assert (eval(inputString, 4, 4, "negated", "denied"));

    }


    @Test
    public void testMixContext3(){
        rules.clear();
        rules.add("advised|forward|trigger|uncertain|30");
        rules.add("or|both|trigger|uncertain|3");
        fc = new FastContext(rules, true);
        fc.debug=true;
        inputString = "I advised she resume OCP or take progesterone in some form Depo , IUD , cyclic provera to prevent endometrial cancer.";
        assert (eval(inputString, 13, 13, "uncertain", "advised"));
//        assert (eval(inputString, 4, 4, "negated", "denied"));

    }

    @Test
    public void testMixContext4(){
        rules.clear();
        rules.add("removed|backward|trigger|negated|30");
        rules.add("without negated|forward|trigger|negated|3");
        fc = new FastContext(rules, true);
        fc.debug=true;
        inputString = "since IUD was removed she has had periods without significant pain";
        eval(inputString, 1, 1, "uncertain", "advised");
//        assert (eval(inputString, 4, 4, "negated", "denied"));

    }

    @Test
    public void testMixContext5(){
        rules.clear();
        rules.add("remove|forward|trigger|removed|30");
        rules.add("does not want to remove|forward|trigger|notremoved|30");
        rules.add("does not want to remove|forward|pseudo|removed|30");
        fc = new FastContext(rules, true);
        fc.debug=true;
        inputString = "she does not want to remove IUD.";
        assert(eval(inputString, 6, 6, "notremoved", "does not want to remove"));
        assert(eval(inputString, 6, 6, "removed", null));
//        assert (eval(inputString, 4, 4, "negated", "denied"));

    }


    @Test
    public void testMixContext6(){
        rules.clear();
        rules.add("currently|both|trigger|current|30");
        rules.add("in the past|both|trigger|historical|30");
        fc = new FastContext(rules, true);
        fc.debug=true;
        inputString = "no vomiting currently , although she did in the past .";
        assert(eval(inputString, 1, 1, "current", "currently"));
        assert(eval(inputString, 1, 1, "historical", "in the past"));
//        assert (eval(inputString, 4, 4, "negated", "denied"));

    }

    @Test
    public void testMixContext7(){
        rules.clear();
        rules.add("but|both|termination|negated|30");
        rules.add("no|both|trigger|negated|30");
        fc = new FastContext(rules, true);
        inputString = "but there is no fever.";
        assert(eval(inputString, 4, 4, "negated", "no"));
//        assert (eval(inputString, 4, 4, "negated", "denied"));

    }

    @Test
    public void testMixContext8(){
        rules.clear();
        rules.add("concern for|forward|trigger|uncertain|30");
        rules.add("for|forward|termination|uncertain|30");

        fc = new FastContext(rules, true);
        inputString = "There is a concern for skin infection.";
        assert(eval(inputString, 5, 6, "uncertain", "concern for"));
//        assert (eval(inputString, 4, 4, "negated", "denied"));

    }


    @Test
    public void testMixContext9(){
        rules.clear();
        rules.add("\\> 0|forward|trigger|uncertain|30");
        FastContext fc = new FastContext(rules,false);
        String inputString = "Vitals - Tm=Tc:98.2 (range 97.0-98.2 o/n) HR:64-86 myocardial infarction";
        String concept = "myocardial infarction";
        int conceptBeginOffset = inputString.indexOf(concept);
        int conceptEndOffset = conceptBeginOffset + concept.length();
        LinkedHashMap<String, ConTextSpan> matches = fc.processContextWEvidence(inputString, conceptBeginOffset, conceptEndOffset, 30);
        System.out.println(matches);

    }



    private boolean eval(String inputString, int conceptBegin, int conceptEnd, String contextType, String contextString) {
        ArrayList<Span> sent = SimpleParser.tokenizeOnWhitespaces(inputString);
        LinkedHashMap<String, ConTextSpan> matches = fc.processContextWEvidence(sent, conceptBegin, conceptEnd, inputString, 30);
        ConTextSpan conTextSpan = matches.get(contextType);
        if (conTextSpan != null) {
            String contextStr = inputString.substring(conTextSpan.begin, conTextSpan.end);
            System.out.println(contextStr);
            return contextStr.equals(contextString);
        } else if (contextString == null) {
            return true;
        }
        return false;
    }


}