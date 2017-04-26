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

package edu.utah.bmi.nlp.core;

import java.util.ArrayList;

/**
 * Created by
 *
 * @author Jianlin Shi on 4/24/17.
 */
public class SimpleTokenizer {
    public static ArrayList<Span> tokenizeOnWhitespaces(String sentence) {
        return tokenizeOnWhitespaces(sentence, 0);
    }

    public static ArrayList<Span> tokenizeOnWhitespaces(String sentence, int offset) {
        ArrayList<Span> tokens = new ArrayList<>();
//        0: punctuation or return, 1: letter, 2: digit,
        int type = 0;
        int tokenBegin = 0;
        StringBuilder tmp = new StringBuilder();
        for (int i = 0; i < sentence.length(); i++) {
            char thisChar = sentence.charAt(i);
            if (Character.isWhitespace(thisChar)) {
                if (type > 0) {
                    tokens.add(new Span(tokenBegin + offset, i + offset, tmp.toString()));
                    tmp.setLength(0);
                }
                type = 0;
            } else {
                if (type == 0) {
                    tokenBegin = i;
                    type = 1;
                }
                tmp.append(thisChar);
            }
        }
        if (type == 1) {
            tokens.add(new Span(tokenBegin + offset, sentence.length() + offset, sentence.substring(tokenBegin)));
        }
        return tokens;
    }
}
