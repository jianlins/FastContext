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

/**
 *
 * This class stores the span information of the evidence support the corresponding Determinants

 * The field width is used to prioritize the wider rulesMap. Instead of using width, you can implement your own scores to
 * prioritize the rulesMap.
 *
 * @author Jianlin Shi
 *
 */
public class Span implements Comparable<Span>{

    public int begin, end,width,ruleId;
    public String text;
    public double score=0d;
    public Span(int begin, int end) {
        this.begin = begin;
        this.end = end;
        this.width=end-begin+1;
    }
    public Span(int begin, int end, String text) {
        this.begin = begin;
        this.end = end;
        this.width=end-begin+1;
        this.text=text;
    }

    public Span(int begin, int end, int ruleId){
        this.begin = begin;
        this.end = end;
        this.ruleId=ruleId;
    }

    public Span(int begin, int end, int ruleId, double score){
        this.begin = begin;
        this.end = end;
        this.ruleId=ruleId;
        this.score=score;
    }
    @Override
    public int compareTo(Span o) {
        if(o==null)
            return -1;
        return  begin-o.begin;
    }

}
