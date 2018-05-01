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

import edu.utah.bmi.nlp.type.system.Concept;
import edu.utah.bmi.nlp.type.system.Context;
import edu.utah.bmi.nlp.uima.AdaptableUIMACPERunner;
import edu.utah.bmi.nlp.uima.ae.SimpleParser_AE;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;

/**
 * @author Jianlin Shi on 5/7/17.
 */
public class FastContext_General_AETest {

	private AnalysisEngine fastContext_AE;
	private JCas jCas;
	private AdaptableUIMACPERunner runner;
	private AnalysisEngine simpleParser_AE;

	@Before
	public void setUp() {
		String typeDescriptor = "desc/type/All_Types";
		runner = new AdaptableUIMACPERunner(typeDescriptor, "target/generated-test-sources/");
		runner.addConceptTypes(FastContext_General_AE.getTypeDefinitions("conf/context.xlsx", true).values());
		runner.reInitTypeSystem("target/generated-test-sources/customized");
		jCas = runner.initJCas();
//      Set up the parameters
		Object[] configurationData = new Object[]{FastContext_General_AE.PARAM_CONTEXT_RULES_STR, "conf/context.tsv",
				FastContext_General_AE.PARAM_DEBUG, true, FastContext_General_AE.PARAM_MARK_CLUE, true};
		try {
			fastContext_AE = createEngine(FastContext_General_AE.class,
					configurationData);
			simpleParser_AE = createEngine(SimpleParser_AE.class, new Object[]{SimpleParser_AE.PARAM_INCLUDE_PUNCTUATION,true});
		} catch (ResourceInitializationException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test() throws AnalysisEngineProcessException {
		String text = "The patient denies any problem with visual changes or hearing changes.";
		String targetWords = "visual changes";
		jCas.setDocumentText(text);
		simpleParser_AE.process(jCas);
		int begin = text.indexOf(targetWords);
		int end = begin + targetWords.length();
		Concept concept = new Concept(jCas, begin, end);
		concept.addToIndexes();
		fastContext_AE.process(jCas);
		Collection<Concept> targets = JCasUtil.select(jCas, Concept.class);
		for(Concept target:targets){
			System.out.println(target.toString());
		}
		for (Context context:JCasUtil.select(jCas,Context.class)){
			System.out.println(context.toString());
		}
	}

	@Test
	public void test2() throws AnalysisEngineProcessException {
		String text = "He history of smoking: no .";;
		String targetWords = "smoking";
		jCas.setDocumentText(text);
		simpleParser_AE.process(jCas);
		int begin = text.indexOf(targetWords);
		int end = begin + targetWords.length();
		Concept concept = new Concept(jCas, begin, end);
		concept.addToIndexes();
		fastContext_AE.process(jCas);
		Collection<Concept> targets = JCasUtil.select(jCas, Concept.class);
		for(Concept target:targets){
			System.out.println(target.toString());
		}
		for (Context context:JCasUtil.select(jCas,Context.class)){
			System.out.println(context.toString());
		}
	}

	@Test
	public void test3() throws AnalysisEngineProcessException, ResourceInitializationException {
		String text = "1.CAD, s/p MI: currently stable.";
		String targetWords = "MI";
		jCas.setDocumentText(text);
		Object[] configurationData = new Object[]{FastContext_General_AE.PARAM_CONTEXT_RULES_STR, "@CONCEPT_FEATURES|Concept|Negation|Certainty|Temporality|Experiencer\n" +
				"@FEATURE_VALUES|Negation|affirm|negated\n" +
				"@FEATURE_VALUES|Certainty|certain|uncertain\n" +
				"@FEATURE_VALUES|Temporality|present|historical|hypothetical\n" +
				"@FEATURE_VALUES|Experiencer|patient|nonpatient\n" +
				"s / p|both|trigger|historical|10\n" +
				"currently|both|termination|historical|10\n" +
				"currently|both|trigger|present|30",
				FastContext_General_AE.PARAM_DEBUG, true, FastContext_General_AE.PARAM_MARK_CLUE, true};
		fastContext_AE = createEngine(FastContext_General_AE.class,
				configurationData);
		simpleParser_AE.process(jCas);
		int begin = text.indexOf(targetWords);
		int end = begin + targetWords.length();
		Concept concept = new Concept(jCas, begin, end);
		concept.addToIndexes();
		fastContext_AE.process(jCas);
		Collection<Concept> targets = JCasUtil.select(jCas, Concept.class);
		for(Concept target:targets){
			System.out.println(target.toString());
		}
		for (Context context:JCasUtil.select(jCas,Context.class)){
			System.out.println(context.toString());
		}
	}
}