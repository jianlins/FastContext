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
 * Test Chinese cases
 *
 * @author Jianlin Shi
 * Created on 1/24/2018.
 */
public class TestFastContextCNExampleCases {
    private FastContext fc;
    private ArrayList<String> rules;
    private String inputString;

    @BeforeEach
    public void init() {
        rules = new ArrayList<>();
    }

    @Test
    public void testNeg1() {
        rules.clear();
        rules.add("无|both|trigger|negated|10");
//        rules.add("无法|both|pseudo|negated|10");
        fc = new FastContext(rules, true);
        inputString = "患者无咳嗽";
        assert (evalOnChars(inputString, 3, 4, "negated", "无"));
    }

    @Test
    public void testNeg2() {
        rules.clear();
        rules.add("否 认|both|trigger|negated|10");
//        rules.add("无法|both|pseudo|negated|10");
        fc = new FastContext(rules, true);
        inputString = "患者否认咳嗽";
        assert (evalOnChars(inputString, 4, 5, "negated", "否认"));
    }

    @Test
    public void testPseudoNeg() {
        rules.clear();
        rules.add("无|backward|trigger|negated|10");
        rules.add("无 法|backward|pseudo|negated|10");
        fc = new FastContext(rules, true);
        inputString = "咳嗽: 无";
        assert (evalOnChars(inputString, 1, 2, "negated", "无"));
        inputString = "患者咳嗽无法自行咳出。";
        assert (evalOnChars(inputString, 2, 3, "negated", null));
    }

    @Test
    public void testPseudoUncertain() {
        rules.clear();
        rules.add("可　能　导　致|forward|pseudo|uncertain|30");
        rules.add("可　能|both|trigger|uncertain|30");
        fc = new FastContext(rules, true);
//        fc.debug = true;
        inputString = "发热可能由感染引起。";
        assert (evalOnChars(inputString, 0, 1, "uncertain", "可能"));
        inputString = "感染可能导致其发热";
        assert (evalOnChars(inputString, 7, 8, "uncertain", null));
    }

    @Test
    public void testPseudoHistorical() {
        rules.clear();
        rules.add("现　病　史|forward|pseudo|historical|30");
        rules.add("病 史|forward|trigger|historical|30");
        fc = new FastContext(rules, true);
//        fc.debug = true;
        inputString = "患者病史包括高血压";
        assert (evalOnChars(inputString, 6, 8, "historical", "病史"));
        inputString = "现病史: 该患者为78岁老年男性, 因腹痛入院";
        assert (evalOnChars(inputString, 19, 20, "historical", null));
    }


    @Test
    public void testTerminationNegated() {
        rules.clear();
        rules.add("但|both|termination|negated|10");
        rules.add("否　认|forward|trigger|negated|30");
        fc = new FastContext(rules, true);
        inputString = "患者否认药物滥用，酗酒史。";
        assert (evalOnChars(inputString, 9, 10, "negated", "否认"));
        inputString = "患者否认药物滥用，但承认酗酒史。";
        assert (evalOnChars(inputString, 12, 13, "negated", null));
    }

    @Test
    public void testTerminationNegated2() {
        rules.clear();
        rules.add("正常|backward|termination|negated|30");
        rules.add("未　扪　及|backward|trigger|negated|30");
        fc = new FastContext(rules, true);
        inputString = "心率正常";
        assert (evalOnChars(inputString, 0, 1, "negated", null));
        inputString = "心率正常，脉搏未扪及";
        assert (evalOnChars(inputString, 5, 6, "negated", "未扪及"));
    }


    @Test
    public void testTerminationHistorical() {
        rules.clear();
        rules.add("诉|forward|termination|historical|30");
        rules.add("病　史|backward|trigger|historical|30");
        fc = new FastContext(rules, true);
        inputString = "患者为一45岁男性，有高血压、糖尿病病史。";
        assert (evalOnChars(inputString, 15, 17, "historical", "病史"));
        inputString = "患者为一45岁男性，有高血压病史，诉胸痛2天。";
        assert (evalOnChars(inputString, 18, 19, "historical", null));
    }





    private boolean evalOnChars(String inputString, int conceptBegin, int conceptEnd, String contextType, String contextString) {
        ArrayList<Span> sent = new ArrayList<>();
        char[]chars=inputString.toCharArray();
        for (int i=0;i<chars.length;i++)
            sent.add(new Span(i,i+1,chars[i]+""));
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