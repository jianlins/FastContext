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
package edu.utah.bmi.nlp.context.common;

import edu.utah.bmi.nlp.core.Span;
import edu.utah.bmi.nlp.context.common.ContextValueSet.TriggerTypes;

/**
 * This is a class to store the span information of either Concept or ConText.
 * <p>
 * "id" is an optional field that can be used to store the rule id information, which is easy to be mapped to a score or rule name.
 * <p>
 *
 * @author Jianlin Shi on 6/24/15.
 */
public class ConTextSpan extends Span {
    public int winBegin, winEnd, ruleId;
    public TriggerTypes matchedDirection;

    public ConTextSpan(int begin, int end) {
        super(begin, end);
        initiate(-1, -1, -1);
    }

    public ConTextSpan(int begin, int end, int id) {
        super(begin, end);
        initiate(-1, -1, id);
    }

    public ConTextSpan(int begin, int end, int id, int winBegin, int winEnd) {
        super(begin, end);
        initiate(id, winBegin, winEnd);
    }

    protected void initiate(int winBegin, int winEnd, int ruleId) {
        this.ruleId = ruleId;
        this.winBegin = winBegin;
        this.winEnd = winEnd;
    }



    public String toString() {
        return "begin=" + begin + "\tend=" + end+ "\twinBegin=" + winBegin+ "\twinEnd=" + winEnd + "\truleId=" + ruleId;
    }


}