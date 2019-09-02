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
import java.util.Arrays;
import java.util.LinkedHashMap;


/**
 * Test different APIs, also serves as the demos of the API use.
 * @author Jianlin Shi
 *         Created on 8/24/15.
 */
public class TestFastContextAPIs {
    private FastContext fc;
    private String inputString;

    @BeforeEach
    public void init() {
        ArrayList<String> rules = new ArrayList<>();
        rules.add("denied|forward|trigger|negated|30");
        rules.add("although|forward|termination|negated|10");
        fc = new FastContext(rules, true);
//        fc.debug=true;
        inputString = "The patient denied any fever , although he complained some headache .";
    }


    @Test
    public void test1() {
        ArrayList<String> sent = new ArrayList<>();
        sent.addAll(Arrays.asList(inputString.split("\\s+")));
        ArrayList<String> res = fc.processContext(sent, 4, 4, 30);
        assert (res.size() == 1 && res.get(0).equals("negated"));

        res = fc.processContext(sent, 10, 10, 30);
        assert (res.size() == 0);
    }

    @Test
    public void test2() {
        ArrayList<Span> sent = SimpleParser.tokenizeOnWhitespaces(inputString);
        ArrayList<String> res = fc.processContext(sent, 4, 4, inputString, 30);
        assert (res.size() == 1 && res.get(0).equals("negated"));

        res = fc.processContext(sent, 10, 10, inputString, 30);
        assert (res.size() == 0);
    }

    @Test
    public void test3() {
        String concept = "fever";
        int conceptBeginOffset = inputString.indexOf(concept);
        int conceptEndOffset = conceptBeginOffset + concept.length();
        ArrayList<String> res = fc.processContext(inputString, conceptBeginOffset, conceptEndOffset, 30);
        assert (res.size() == 1 && res.get(0).equals("negated"));

        concept = "headache";
        conceptBeginOffset = inputString.indexOf(concept);
        conceptEndOffset = conceptBeginOffset + concept.length();
        res = fc.processContext(inputString, conceptBeginOffset, conceptEndOffset, 30);
        assert (res.size() == 0);
    }

    @Test
    public void test4() {
        ArrayList<String> sent = new ArrayList<>();
        sent.addAll(Arrays.asList(inputString.split("\\s+")));
        LinkedHashMap<String, ConTextSpan> matches = fc.processContextWEvidence(sent, 4, 4, 30);
        ConTextSpan conTextSpan = matches.get("negated");
        assert (conTextSpan.begin == 2 && conTextSpan.end == 2 && conTextSpan.ruleId == 0);
//        System.out.println(sent.subList(conTextSpan.begin,conTextSpan.end+1));
        assert (sent.subList(conTextSpan.begin, conTextSpan.end + 1).get(0).equals("denied"));
    }

    @Test
    public void test5() {
        ArrayList<Span> sent = SimpleParser.tokenizeOnWhitespaces(inputString);
        LinkedHashMap<String, ConTextSpan> matches = fc.processContextWEvidence(sent, 4, 4, inputString, 30);
        ConTextSpan conTextSpan = matches.get("negated");
        assert (conTextSpan.begin == 12 && conTextSpan.end == 18 && conTextSpan.ruleId == 0);
        assert (inputString.substring(conTextSpan.begin, conTextSpan.end).equals("denied"));

    }

    @Test
    public void test6() {
        String concept = "fever";
        int conceptBeginOffset = inputString.indexOf(concept);
        int conceptEndOffset = conceptBeginOffset + concept.length();
        LinkedHashMap<String, ConTextSpan> matches = fc.processContextWEvidence(inputString, conceptBeginOffset, conceptEndOffset, 30);
        ConTextSpan conTextSpan = matches.get("negated");
        assert (conTextSpan.begin == 12 && conTextSpan.end == 18 && conTextSpan.ruleId == 0);
        assert (inputString.substring(conTextSpan.begin, conTextSpan.end).equals("denied"));
    }

}