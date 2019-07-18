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
import edu.utah.bmi.nlp.core.DeterminantValueSet;
import edu.utah.bmi.nlp.core.TypeDefinition;
import edu.utah.bmi.nlp.fastcontext.FastContext;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author Jianlin_Shi on 7/28/15.
 */
public class IOUtil {
    private static int defaultWindowSize = 8;


    public static void readAgnosticRuleResource(String ruleFileOrString, String splitter, HashMap<Integer, ContextRule> rules,
                                                HashMap<String, TypeDefinition> conceptFeaturesMap,
                                                HashMap<String, String> featureDefaultValueMap,
                                                HashMap<String, String> valueFeatureNameMap) {
        int strLength = ruleFileOrString.trim().length();
        String testFileStr = ruleFileOrString.trim().substring(strLength - 4).toLowerCase();
        switch (testFileStr) {
            case ".tsv":
                readCSVFile(ruleFileOrString, "\t", rules, conceptFeaturesMap, featureDefaultValueMap, valueFeatureNameMap);
                break;
            case ".csv":
                readCSVFile(ruleFileOrString, ",", rules, conceptFeaturesMap, featureDefaultValueMap, valueFeatureNameMap);
                break;
            case ".txt":
                readCSVFile(ruleFileOrString, splitter, rules, conceptFeaturesMap, featureDefaultValueMap, valueFeatureNameMap);
                break;
            case "xlsx":
                readXLSXRuleFile(ruleFileOrString, rules, conceptFeaturesMap, featureDefaultValueMap, valueFeatureNameMap);
                break;
            case ".owl":
                readOwlFile(ruleFileOrString, rules, conceptFeaturesMap, featureDefaultValueMap, valueFeatureNameMap);
            default:
                readCSVString(ruleFileOrString, splitter, rules, conceptFeaturesMap, featureDefaultValueMap, valueFeatureNameMap);
                break;
        }
        if (conceptFeaturesMap.size() == 0) {
            readDefaultRules(rules, conceptFeaturesMap, featureDefaultValueMap, valueFeatureNameMap);
        }
    }

    private static void readDefaultRules(HashMap<Integer, ContextRule> rules,
                                         HashMap<String, TypeDefinition> conceptFeaturesMap,
                                         HashMap<String, String> featureDefaultValueMap,
                                         HashMap<String, String> valueFeatureNameMap) {
        String ruleStr = "@CONCEPT_FEATURES|Concept|Negation|Certainty|Temporality|Experiencer\n" +
                "@FEATURE_VALUES|Negation|affirm|negated\n" +
                "@FEATURE_VALUES|Certainty|certain|uncertain\n" +
                "@FEATURE_VALUES|Temporality|present|historical|hypothetical\n" +
                "@FEATURE_VALUES|Experiencer|patient|nonpatient";
        readCSVString(ruleStr, "|", rules, conceptFeaturesMap, featureDefaultValueMap, valueFeatureNameMap);
    }

    //TODO
    private static void readOwlFile(String ruleFileOrString, HashMap<Integer, ContextRule> rules,
                                    HashMap<String, TypeDefinition> conceptFeaturesMap,
                                    HashMap<String, String> featureDefaultValueMap,
                                    HashMap<String, String> valueFeatureNameMap) {
        //        int id = 1;
//        try {
//            DomainOntology domain = new DomainOntology(owlFile, true);
//            ArrayList<Modifier> modifierDictionary = domain.createModifierDictionary();
//            for (Modifier modifier : modifierDictionary) {
//                String modifierName = modifier.getModName();
//                id = addLexicalItemsToRules(output, id, modifierName, TriggerTypes.trigger, modifier.getItems());
//                for (Modifier pseudos : modifier.getPseudos()) {
//                    id = addLexicalItemsToRules(output, id, modifierName, TriggerTypes.pseudo, pseudos.getItems());
//                }
//                for (Modifier terminations : modifier.getClosures()) {
//                    id = addLexicalItemsToRules(output, id, modifierName, TriggerTypes.termination, terminations.getItems());
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }


//    private static int addLexicalItemsToRules(LinkedHashMap<Integer, ContextRule> rules, int id, String modifierName,
//                                              TriggerTypes triggerType, ArrayList<LexicalItem> lexicalItems) {
//        for (LexicalItem mitem : lexicalItems) {
//            String term = mitem.getPrefTerm();
//            String directionStr = mitem.getActionEn(true);
//            int windowSize = mitem.getWindowSize();
//            TriggerTypes direction;
//            switch (directionStr.charAt(2)) {
//                case 'o':
//                    direction = TriggerTypes.forward;
//                    break;
//                case 'a':
//                    direction = TriggerTypes.backward;
//                    break;
//                default:
////                   modifier ontology use "bidirectional"
//                    direction = TriggerTypes.both;
//            }
//            rules.put(id, new ContextRule(direction, triggerType, direction + "_" + modifierName,
//                    modifierName, term, id, windowSize));
//            id++;
//            id = addTermsToRules(rules, direction, triggerType, id, modifierName, windowSize, mitem.getSynonym());
//            id = addTermsToRules(rules, direction, triggerType, id, modifierName, windowSize, mitem.getAbbreviation());
//            id = addTermsToRules(rules, direction, triggerType, id, modifierName, windowSize, mitem.getSubjExp());
//            id = addTermsToRules(rules, direction, triggerType, id, modifierName, windowSize, mitem.getMisspelling());
//            mitem.getSynonym();
//
//        }
//        return id;
//    }

//    private static int addTermsToRules(LinkedHashMap<Integer, ContextRule> rules, TriggerTypes direction, TriggerTypes triggerType, int id, String modifierName, int windowSize, ArrayList<String> terms) {
//        for (String term : terms) {
//            rules.put(id, new ContextRule(direction, triggerType, direction + "_" + modifierName,
//                    modifierName, term, id, windowSize));
//            id++;
//        }
//        return id;
//    }


    public static void readXLSXRuleFile(String xlsxFileName, HashMap<Integer, ContextRule> rules,
                                        HashMap<String, TypeDefinition> conceptFeaturesMap,
                                        HashMap<String, String> featureDefaultValueMap,
                                        HashMap<String, String> valueFeatureNameMap) {
        try {
            FileInputStream inputStream = new FileInputStream(new File(xlsxFileName));
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet firstSheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = firstSheet.iterator();
            int id = 0;
            while (iterator.hasNext()) {
                Row nextRow = iterator.next();
                Iterator<Cell> cellIterator = nextRow.cellIterator();
                ArrayList<String> cells = new ArrayList<>();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    switch (cell.getCellTypeEnum()) {
                        case NUMERIC:
                            cells.add(cell.getNumericCellValue() + "");
                            break;
                        default:
                            cells.add(cell.getStringCellValue());
                            break;
                    }
                }
                if (cells.size() > 0 && cells.get(0).trim().length() > 0)
                    parseCells(cells, id, rules, conceptFeaturesMap, featureDefaultValueMap, valueFeatureNameMap);
                id++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void readCSVFile(String csvFileName, String splitter, HashMap<Integer, ContextRule> rules,
                                   HashMap<String, TypeDefinition> conceptFeaturesMap,
                                   HashMap<String, String> featureDefaultValueMap,
                                   HashMap<String, String> valueFeatureNameMap) {
        CSVFormat csvFormat = getCSVFormat(splitter);
        try {
            Iterable<CSVRecord> recordsIterator = CSVParser.parse(new File(csvFileName), StandardCharsets.UTF_8, csvFormat);
            readCSV(recordsIterator, rules, conceptFeaturesMap, featureDefaultValueMap, valueFeatureNameMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void readCSVString(String csvString, String splitter, HashMap<Integer, ContextRule> rules,
                                     HashMap<String, TypeDefinition> conceptFeaturesMap,
                                     HashMap<String, String> featureDefaultValueMap,
                                     HashMap<String, String> valueFeatureNameMap) {
        CSVFormat csvFormat = getCSVFormat(splitter);
        try {
            Iterable<CSVRecord> recordsIterator = CSVParser.parse(csvString, csvFormat);
            readCSV(recordsIterator, rules, conceptFeaturesMap, featureDefaultValueMap, valueFeatureNameMap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void readStringList(ArrayList<String> ruleStringList, String splitter, HashMap<Integer, ContextRule> rules,
                                      HashMap<String, TypeDefinition> conceptFeaturesMap,
                                      HashMap<String, String> featureDefaultValueMap,
                                      HashMap<String, String> valueFeatureNameMap) {
        CSVFormat csvFormat = getCSVFormat(splitter);
        for (int i = 0; i < ruleStringList.size(); i++) {
            String line = ruleStringList.get(i);
            if (line.startsWith("#") || line.trim().length() == 0) {
                continue;
            }
            try {
                CSVRecord record = CSVParser.parse(line, csvFormat).iterator().next();
                ArrayList<String> cells = new ArrayList<>();
                for (String cell : record) {
                    cells.add(cell);
                }
                parseCells(cells, i, rules, conceptFeaturesMap, featureDefaultValueMap, valueFeatureNameMap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void readCSV(Iterable<CSVRecord> recordsIterator, HashMap<Integer, ContextRule> rules,
                                HashMap<String, TypeDefinition> conceptFeaturesMap,
                                HashMap<String, String> featureDefaultValueMap,
                                HashMap<String, String> valueFeatureNameMap) {
        int id = 0;
        for (CSVRecord record : recordsIterator) {
            ArrayList<String> cells = new ArrayList<>();
            for (String cell : record) {
                cells.add(cell);
            }
            parseCells(cells, id, rules, conceptFeaturesMap, featureDefaultValueMap, valueFeatureNameMap);
            id++;
        }
    }


//		@CONCEPT_FEATURES	ConceptName	Feature1Name	Feature2Name	Feature3Name
//		@FEATURE_VALUES		DefaultValue	Value1	Value2

    private static void parseCells(ArrayList<String> cells, int id, HashMap<Integer, ContextRule> rules,
                                   HashMap<String, TypeDefinition> conceptFeaturesMap,
                                   HashMap<String, String> featureDefaultValueMap,
                                   HashMap<String, String> valueFeatureNameMap) {
        if (cells.get(0).startsWith("#") || cells.get(0).startsWith("\"#") || cells.get(0).trim().length() == 0)
            return;
        if (cells.get(0).length() < 2 && cells.size() > 3) {
            String ruleString = cells.get(0);
            String direction = cells.get(1);
            String triggerType = cells.get(2);
            String modifier = cells.get(3);
            String determinant = cells.get(1) + "_" + cells.get(3);
            int windowSize = defaultWindowSize;
            if (cells.size() > 4)
                windowSize = (int) Double.parseDouble(cells.get(4));

            rules.put(id, new ContextRule(TriggerTypes.valueOf(direction), TriggerTypes.valueOf(triggerType), determinant, modifier,
                    ruleString, id, windowSize));
            return;
        }
        switch (cells.get(0).substring(0, 2)) {
            case "@C":
            case "&C":
//                @CONCEPT_FEATURES
                if (cells.size() < 3) {
                    System.err.println("Rule format error: " + cells);
                } else {
                    String conceptName = DeterminantValueSet.checkNameSpace(cells.get(1));
                    List<String> features = cells.subList(2, cells.size());
                    if (!conceptFeaturesMap.containsKey(conceptName)) {
                        conceptFeaturesMap.put(conceptName, new TypeDefinition(conceptName, "", new ArrayList<>()));
                    }
                    LinkedHashMap<String, String> featureValues = conceptFeaturesMap.get(conceptName).getFeatureValuePairs();
                    for (String feature : features) {
                        if (feature.trim().length() > 0)
                            featureValues.put(feature, "");
                    }
                }
                break;
            case "@F":
            case "&F":
//                @FEATURE_VALUES
                if (cells.size() < 2) {
                    System.err.println("Rule format error: " + cells);
                } else if (cells.size() == 2) {
                    String featureName = cells.get(1);
                    featureDefaultValueMap.put(featureName, "");
                } else {
                    String featureName = cells.get(1);
                    String defaultValue = cells.get(2).trim();
                    featureDefaultValueMap.put(featureName, defaultValue);
                    valueFeatureNameMap.put(defaultValue, featureName);
                    if (cells.size() > 2) {
                        for (String value : cells.subList(2, cells.size())) {
                            valueFeatureNameMap.put(value, featureName);
                        }
                    }
                }
                break;
            default:
                String ruleString = cells.get(0);
                String direction = cells.get(1);
                String triggerType = cells.get(2);
                String modifier = cells.get(3);
                String determinant = cells.get(1) + "_" + cells.get(3);
                int windowSize = defaultWindowSize;
                if (cells.size() > 4)
                    windowSize = (int) Double.parseDouble(cells.get(4));

                if (valueFeatureNameMap.size() == 0 || valueFeatureNameMap.containsKey(modifier)) {
                    rules.put(id, new ContextRule(TriggerTypes.valueOf(direction), TriggerTypes.valueOf(triggerType), determinant, modifier,
                            ruleString, id, windowSize));
                } else {
                    FastContext.logger.finest("Rule " + id + " " + cells + " has the modifier value not defined in the setting, skip this rule.");
                }
                break;
//                TODO need some key-value pair completeness check for the maps
        }
    }


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

    private static CSVFormat getCSVFormat(String splitter) {
        CSVFormat csvFormat = CSVFormat.DEFAULT;
        if (splitter != null) {
            switch (splitter) {
                case "\t":
                    csvFormat = CSVFormat.TDF;
                case ",":
                    csvFormat = CSVFormat.DEFAULT;
                default:
                    csvFormat = CSVFormat.newFormat(splitter.charAt(0));
            }
        }
        return csvFormat;
    }
}
