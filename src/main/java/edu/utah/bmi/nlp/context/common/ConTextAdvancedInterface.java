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

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * <p>
 * This is an extended ConText interface class, which defines two additional interface methods that reserve the evidence Span information for Assertions.
 * This interface is optional. Because for most of use cases, storing evidence information is not necessary.
 * </p>
 *
 * @author Jianlin Shi on 6/24/15.
 */
public interface ConTextAdvancedInterface extends ConTextBasicInterface {

    /**
     * This interface method has the same input as processContext in ConTextInterface, but return a LinkedHashMap with Assertions as the keys,
     * and Spans as the values.
     *
     * @param tokens               A list of tokens in an ArrayList of String format
     * @param conceptStartPosition The start position of concept in the token ArrayList (start from 0)
     * @param conceptEndPosition   The end position of a concept in the token ArrayList
     * @param windowsize           The window size that need to be consider for the match (FastContext doesn't use window size
     *                             here, instead it evaluate the window boundary defined in each matched rule--more flexible.
     * @return Matched context rules in LinkedHashMap format, where the key is the rule name, and the key
     * is the matched span
     */
    LinkedHashMap<String, ConTextSpan> processContextWEvidence(ArrayList<String> tokens, int conceptStartPosition,
                                                               int conceptEndPosition, int windowsize);

    /**
     * This interface method has the same input as processContext in ConTextInterface, but return a LinkedHashMap with Assertions as the keys,
     * and Spans as the values.
     *
     * @param tokens               A list of tokens  in an ArrayList of Span  format
     * @param conceptStartPosition The start position of concept in the token ArrayList (start from 0)
     * @param conceptEndPosition   The end position of a concept in the token ArrayList
     * @param windowsize           The window size that need to be consider for the match (FastContext doesn't use window size
     *                             here, instead it evaluate the window boundary defined in each matched rule--more flexible.
     * @param text                 The text string of which the spans' offsets come from
     * @return Matched context rules in LinkedHashMap format, where the key is the rule name, and the key
     * is the matched span
     */
    LinkedHashMap<String, ConTextSpan> processContextWEvidence(ArrayList<Span> tokens, int conceptStartPosition,
                                                               int conceptEndPosition, String text, int windowsize);

}
