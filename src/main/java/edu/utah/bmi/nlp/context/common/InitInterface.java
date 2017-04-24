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

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Enable this common interface for different implementation evaluation
 * @author Jianlin Shi on 8/23/16.
 */
public interface InitInterface {
    void initiate(String ruleFile);

    void initiate(ArrayList<String> rules, boolean toLowerCase);

    void initiate(LinkedHashMap<Integer, ContextRule> rules, boolean toLowerCase);
}
