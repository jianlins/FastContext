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
package edu.utah.bmi.context.common;

import edu.utah.bmi.nlp.Span;

import java.util.ArrayList;

/**
 * This is a ConText interface class, which defines the common methods that applicable for child classes.
 * In this way, all child classes can be plugged into same code with minimum modification.
 * <p/>
 * Drafted by Jianlin Shi on 6/24/15.
 */
public interface ConTextBasicInterface {

//    these two temporary interfaces are used to evaluate
//    void initiate(String ruleFile);
//    void initiate(ArrayList<String> rules);
    /**
     * This interface method assume that the input has been tokenized.  "conceptStartPosition" and "conceptEndPosition" is relative position within ArrayList,
     * which is easier to be called. Setting windowsize in every run allows adjusting window size on rule bases.
     *
     * @param tokens
     * @param conceptStartPosition
     * @param conceptEndPosition
     * @param windowsize
     * @return
     */
    ArrayList<String> processContext(ArrayList<String> tokens, int conceptStartPosition, int conceptEndPosition, int windowsize);

    /**
     * This interface method assume that the input has been tokenized as a Span ArrayList.  "conceptStartPosition" and "conceptEndPosition" is relative,
     * while Spans is assumed to be absolute offsets. Extended class can override this default by directly implementation. Setting windowsize in every
     * run allows adjusting window size on rule bases.
     *
     * @param tokens
     * @param conceptStartPosition
     * @param conceptEndPosition
     * @param text
     * @param windowsize
     * @return
     */
    ArrayList<String> processContext(ArrayList<Span> tokens, int conceptStartPosition, int conceptEndPosition, String text, int windowsize);

    /**
     * This interface method is to back compatible with previous implementations.  "conceptBegin" and "conceptEnd" is absolute offset within sentence string.
     * Setting windowsize in every run allows adjusting window size on rule bases.
     *
     * @param sentence
     * @param conceptBegin
     * @param conceptEnd
     * @param windowsize
     * @return
     */
    ArrayList<String> processContext(String sentence, int conceptBegin, int conceptEnd, int windowsize);
}
