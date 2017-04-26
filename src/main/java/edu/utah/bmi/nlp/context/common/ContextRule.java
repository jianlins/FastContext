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

import edu.utah.bmi.nlp.context.common.ContextValueSet.TriggerTypes;

/**
 * @author Jianlin Shi
 *         Created on 8/24/15.
 */
public class ContextRule {


    public TriggerTypes direction, triggerType;
    public String modifier;
    public String determinant;
    public String rule;
    public int id;
    public int windowSize;

    public ContextRule(TriggerTypes direction, TriggerTypes triggerType, String determinant, String modifier, String rule, int id, int windowSize) {
        this.direction = direction;
        this.triggerType = triggerType;
        this.modifier = modifier;
        this.determinant = determinant;
        this.rule = rule;
        this.id = id;
        this.windowSize = windowSize;
    }

    public String toString() {
        return "ContextRule " + id + ": " + rule + "|" + direction + "|" + triggerType + "|" + modifier + "|" + windowSize;
    }

    public ContextRule clone() {
        return new ContextRule(direction, triggerType, determinant, modifier, rule, id, windowSize);
    }

    public TriggerTypes getDirection() {
        return direction;
    }

    public void setDirection(TriggerTypes direction) {
        this.direction = direction;
        this.determinant=this.direction + "_" + this.modifier;
    }

    public TriggerTypes getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(TriggerTypes triggerType) {
        this.triggerType = triggerType;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public String getDeterminant() {
        return determinant;
    }

    public void setDeterminant(String determinant) {
        this.determinant = determinant;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(int windowSize) {
        this.windowSize = windowSize;
    }
}
