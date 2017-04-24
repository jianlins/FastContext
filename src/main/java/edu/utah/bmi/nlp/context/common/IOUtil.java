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

import edu.utah.blulab.domainontology.DomainOntology;
import edu.utah.blulab.domainontology.LexicalItem;
import edu.utah.blulab.domainontology.Modifier;
import edu.utah.bmi.nlp.context.common.ContextValueSet.TriggerTypes;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * @author Jianlin_Shi on 7/28/15.
 */
public class IOUtil {
    private static int defaultWindowSize = 8;


    public static LinkedHashMap<Integer, ContextRule> convertListToRuleMap(ArrayList<String> ruleslist, String splittor) {
        LinkedHashMap<Integer, ContextRule> rules = new LinkedHashMap<>();
        for (int i = 1; i <= ruleslist.size(); i++) {
            String line = ruleslist.get(i - 1).trim();
            if (line.length() == 0 || line.charAt(0) == '#')
                continue;
            rules.put(i, convertStringToRule(line, splittor, i));
        }
        return rules;
    }

    public static ContextRule convertStringToRule(String str, String splittor, int id) {
        String[] record = str.split(splittor);
        String modifier = record[3];
//          regardless of TriggerTypes, when prioritize the matched rules only based on matched positions
        String determinant = record[1] + "_" + record[3];
        int windowSize = defaultWindowSize;
        if (record.length > 4)
            windowSize = Integer.parseInt(record[4]);
//          make sure the enum variables contain the value defined in this rule
        return new ContextRule(TriggerTypes.valueOf(record[1]), TriggerTypes.valueOf(record[2]), determinant, record[3],
                record[0], id, windowSize);
    }

    public static LinkedHashMap<Integer, ContextRule> readOwlModifiers(String owlFile) {
        LinkedHashMap<Integer, ContextRule> output = new LinkedHashMap();
        int id = 1;
        try {
            DomainOntology domain = new DomainOntology(owlFile, true);
            ArrayList<Modifier> modifierDictionary = domain.createModifierDictionary();
            for (Modifier modifier : modifierDictionary) {
                String modifierName = modifier.getModName();
                id = addLexicalItemsToRules(output, id, modifierName, TriggerTypes.trigger, modifier.getItems());
                for (Modifier pseudos : modifier.getPseudos()) {
                    id = addLexicalItemsToRules(output, id, modifierName, TriggerTypes.pseudo, pseudos.getItems());
                }
                for (Modifier terminations : modifier.getClosures()) {
                    id = addLexicalItemsToRules(output, id, modifierName, TriggerTypes.termination, terminations.getItems());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output;
    }

    private static int addLexicalItemsToRules(LinkedHashMap<Integer, ContextRule> rules, int id, String modifierName,
                                              TriggerTypes triggerType, ArrayList<LexicalItem> lexicalItems) {
        for (LexicalItem mitem : lexicalItems) {
            String term = mitem.getPrefTerm();
            String directionStr = mitem.getActionEn(true);
            int windowSize = mitem.getWindowSize();
            TriggerTypes direction;
            switch (directionStr.charAt(2)) {
                case 'o':
                    direction = TriggerTypes.forward;
                    break;
                case 'a':
                    direction = TriggerTypes.backward;
                    break;
                default:
//                   modifier ontology use "bidirectional"
                    direction = TriggerTypes.both;
            }
            rules.put(id, new ContextRule(direction, triggerType, direction + "_" + modifierName,
                    modifierName, term, id, windowSize));
            id++;
            id = addTermsToRules(rules, direction, triggerType, id, modifierName, windowSize, mitem.getSynonym());
            id = addTermsToRules(rules, direction, triggerType, id, modifierName, windowSize, mitem.getAbbreviation());
            id = addTermsToRules(rules, direction, triggerType, id, modifierName, windowSize, mitem.getSubjExp());
            id = addTermsToRules(rules, direction, triggerType, id, modifierName, windowSize, mitem.getMisspelling());
            mitem.getSynonym();

        }
        return id;
    }

    private static int addTermsToRules(LinkedHashMap<Integer, ContextRule> rules, TriggerTypes direction, TriggerTypes triggerType, int id, String modifierName, int windowSize, ArrayList<String> terms) {
        for (String term : terms) {
            rules.put(id, new ContextRule(direction, triggerType, direction + "_" + modifierName,
                    modifierName, term, id, windowSize));
            id++;
        }
        return id;
    }


    public static LinkedHashMap<Integer, ContextRule> readRuleFile(String ruleFile) {
        if (ruleFile.endsWith(".owl")) {
            return readOwlModifiers(ruleFile);
        } else {
            return readCSVRuleFile(ruleFile, "\\|");
        }

    }

    public static LinkedHashMap<Integer, ContextRule> readCSVRuleFile(String ruleFile, String splitter) {
        LinkedHashMap<Integer, ContextRule> rules = new LinkedHashMap<>();
        File file = new File(ruleFile);
        BufferedReader reader = null;
        int id = 0;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line = "";
            // read rules line by line to construct the regular expression
            while ((line = reader.readLine()) != null) {
                id++;
                line = line.trim();
                if (line.length() == 0 || line.charAt(0) == '#')
                    continue;
                rules.put(id, convertStringToRule(line, splitter, id));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rules;
    }


    public static ArrayList<String> readFileToStrings(String ruleFile) {
        ArrayList<String> output = new ArrayList<String>();
        File file = new File(ruleFile);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line = "";
            // read rules line by line to construct the regular expression
            while ((line = reader.readLine()) != null) {
                output.add(line.trim());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output;
    }

//    public static LinkedHashMap<Integer, ContextRule> readStringsToRules(ArrayList<String> ruleStrings, String splitter) {
//        LinkedHashMap<Integer, ContextRule> rules = new LinkedHashMap();
//        int id = 0;
//        for (String line : ruleStrings) {
//            line = line.trim();
//            if (line.length() > 0 && !line.startsWith("#")) {
//                String[] record = line.split(splitter);
//                String modifier = record[3];
////          regardless of TriggerTypes, when prioritize the matched rules only based on matched positions
//                String determinant = record[1] + "_" + record[3];
//                int windowSize = defaultWindowSize;
//                if (record.length > 4)
//                    windowSize = Integer.parseInt(record[4]);
////          save rule properties to a ContextRule object, and map it to an id number.
//                rules.put(id, new ContextRule(ContextValueSet.TriggerTypes.valueOf(record[1]), ContextValueSet.TriggerTypes.valueOf(record[2]), determinant, record[3],
//                        record[0], id, windowSize));
//
//            }
//            id++;
//        }
//        return rules;
//    }


    public static void writeFile(ArrayList<String> content, String ruleFile) {
        ArrayList<String> output = new ArrayList<String>();
        File file = new File(ruleFile);
        BufferedWriter reader = null;
        try {
            reader = new BufferedWriter(new FileWriter(file));

            // read rules line by line to construct the regular expression
            for (String line : content) {
                reader.write(line + "\n");
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
