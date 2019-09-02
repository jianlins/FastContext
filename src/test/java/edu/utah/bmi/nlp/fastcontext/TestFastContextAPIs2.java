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

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;


/**
 * Test FastContext APIs with updated full features
 *
 * @author Jianlin Shi
 * Created on 8/24/15.
 */
public class TestFastContextAPIs2 {
    private FastContext fc;
    private String inputString;

    @BeforeEach
    public void init() {
        ArrayList<String> rules = new ArrayList<>();
        rules.add("@CONCEPT_FEATURES|Concept|Negation|Certainty|Temporality|Experiencer");
        rules.add("@FEATURE_VALUES|Negation|affirm|negated");
        rules.add("@FEATURE_VALUES|Certainty|certain|uncertain");
        rules.add("@FEATURE_VALUES|Temporality|present|historical|hypothetical");
        rules.add("@FEATURE_VALUES|Experiencer|patient|nonpatient");
        rules.add("denied|forward|trigger|negated|30");
        rules.add("although|forward|termination|negated|10");
        fc = new FastContext(rules, true);
//        fc.debug=true;
        inputString = "The patient denied any fever , although he complained some headache .";
    }


    @Test
    public void test1() {
        ArrayList<Span> sent = SimpleParser.tokenizeOnWhitespaces(inputString);
        LinkedHashMap<String, ConTextSpan> matches = fc.getFullContextFeatures("Concept", sent, 4, 4, inputString);
        ConTextSpan conTextSpan = matches.get("Negation");
        assert (conTextSpan.begin == 12 && conTextSpan.end == 18 && conTextSpan.ruleId == 5);
        conTextSpan = matches.get("Experiencer");
        assert (conTextSpan.ruleId == -1);
        conTextSpan = matches.get("Certainty");
        assert (conTextSpan.ruleId == -1);
        conTextSpan = matches.get("Temporality");
        assert (conTextSpan.ruleId == -1);
    }

    @Test
    public void test2() {
        ArrayList<String> rules = new ArrayList<>();
        rules.add("@CONCEPT_FEATURES|Concept|Percentage");
        rules.add("@FEATURE_VALUES|Percentage|yes|no");
        rules.add("( \\> 0 %) |backward|trigger|no|10");
        fc = new FastContext(rules, true);
//        fc.debug=true;
        inputString = "538 patients (5.1%) ";
        ArrayList<Span> sent = SimpleParser.tokenizeDecimalSmart(inputString, true);
        LinkedHashMap<String, ConTextSpan> matches = fc.getFullContextFeatures("Concept", sent, 0, 1, inputString);
        ConTextSpan conTextSpan = matches.get("Percentage");
        assert (conTextSpan.begin > 0);
    }

    @Test
    public void test3() {
        ArrayList<String> rules = new ArrayList<>();
        rules.add("@CONCEPT_FEATURES|Concept|Percentage");
        rules.add("@FEATURE_VALUES|Percentage|yes|no");
        rules.add("\\> 0 \\< 5 |both|trigger|no|10");
        fc = new FastContext(rules, true);
//        fc.debug=true;
        inputString = "there are 4.1% of 538 patients. ";
        ArrayList<Span> sent = SimpleParser.tokenizeDecimalSmart(inputString, true);
        for(Span sp:sent){
            System.out.println(inputString.substring(sp.begin,sp.end));
        }
        LinkedHashMap<String, ConTextSpan> matches = fc.getFullContextFeatures("Concept", sent, 0, 1, inputString);
        ConTextSpan conTextSpan = matches.get("Percentage");
        System.out.println(conTextSpan);
        System.out.println(inputString.substring(conTextSpan.begin,conTextSpan.end));
//        assert (conTextSpan.begin > 0);
    }

    @Test
    public void test4() {
        ArrayList<String> rules = new ArrayList<>();
        rules.add("@CONCEPT_FEATURES|Concept|Percentage");
        rules.add("@FEATURE_VALUES|Percentage|a|b|c|d");
        rules.add("\\> 0 \\< 5 |both|trigger|a|10");
        rules.add("\\> 10 \\< 20 |both|trigger|b|10");
        rules.add("\\> 20 \\< 50 |both|trigger|c|10");
        rules.add("\\> 50 \\< 100 |both|trigger|d|10");
        fc = new FastContext(rules, true);
//        fc.debug=true;
        inputString = "there are 4.1% of in 30 cities of 14 countries, 90 patients . ";
        ArrayList<Span> sent = SimpleParser.tokenizeDecimalSmart(inputString, true);
        for(Span sp:sent){
            System.out.println(inputString.substring(sp.begin,sp.end));
        }
        LinkedHashMap<String, ConTextSpan> matches = fc.getFullContextFeatures("Concept", sent, 0, 1, inputString);
        ConTextSpan conTextSpan = matches.get("Percentage");
        System.out.println(conTextSpan);
        System.out.println(inputString.substring(conTextSpan.begin,conTextSpan.end));
//        assert (conTextSpan.begin > 0);
    }

    @Test
    public void testInputStream() {
        ArrayList<String> rules = new ArrayList<>();
        rules.add("@CONCEPT_FEATURES|Concept|Percentage");
        rules.add("@FEATURE_VALUES|Percentage|yes|no");
        rules.add("( \\> 0 %) |backward|trigger|no|10");
        fc = new FastContext(new ByteArrayInputStream(String.join("\n", rules).getBytes(StandardCharsets.UTF_8)),
                false, StandardCharsets.UTF_8.name());
//        fc.debug=true;
        inputString = "538 patients (5.1%) ";
        ArrayList<Span> sent = SimpleParser.tokenizeDecimalSmart(inputString, true);
        LinkedHashMap<String, ConTextSpan> matches = fc.getFullContextFeatures("Concept", sent,
                0, 1, inputString);
        ConTextSpan conTextSpan = matches.get("Percentage");
        assert (conTextSpan.begin > 0);
    }
}