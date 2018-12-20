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
/**
 *
 * Note:
 *
 * Because FastContext does not implement real regular expression. Rather, it implements a few regular expression capability instead.
 * 1. use \w+ to represent a word (corresponding to any element in the input ArrayList of String or Span
 * 2. use "&gt; number" (there is a whitespace between them) to represent any digit greater than the given "number"
 * As a results, you will need consider input digit as single token annotation/word, which is annotated differently in some parsers.
 * Please referring to the example rule file: "conf/context.txt"
 *
 *
 * @author Jianlin Shi
 */
package edu.utah.bmi.nlp.fastcontext;


