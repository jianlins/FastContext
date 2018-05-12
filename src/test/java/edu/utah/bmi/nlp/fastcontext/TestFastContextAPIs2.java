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
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;


/**
 * Test FastContext APIs with updated full features
 *
 * @author Jianlin Shi
 *         Created on 8/24/15.
 */
public class TestFastContextAPIs2 {
	private FastContext fc;
	private String inputString;

	@Before
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
		rules.add("( > 0 %) |backward|trigger|no|10");
		fc = new FastContext(rules, true);
//        fc.debug=true;
		inputString = "538 patients (5.1%) ";
		ArrayList<Span> sent = SimpleParser.tokenizeDecimalSmart(inputString,true);
		LinkedHashMap<String, ConTextSpan> matches = fc.getFullContextFeatures("Concept", sent, 0, 1, inputString);
		ConTextSpan conTextSpan = matches.get("Percentage");
		assert (conTextSpan.begin>0);
	}


}