/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.language.gherkin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GherkinVariables {


    private final static Pattern exampleInstancePattern = Pattern.compile("\\|\\ *([a-zA-Z0-9 ]+)");

    private ArrayList<String> columns = new ArrayList<>();

    public Map<String, ArrayList<String>> variables = new HashMap<>();

    public void processHeaderLine(String headerLine) {
        ArrayList<String>elements = splitExampleData(headerLine);
        for(String element : elements) {
            this.variables.put(element.trim(), new ArrayList<String>());
            this.columns.add(element.trim());
        }
    }

    public void processDataLine(String dataLine) {
        ArrayList<String>elements = splitExampleData(dataLine);
        int column = 0;
        for(String element : elements) {
            //get the key for this column
            String key = this.columns.get(column);
            this.variables.get(key).add(element.trim());
            column++;
        }
    }

    private ArrayList<String> splitExampleData(String line) {
        ArrayList<String> bits = new ArrayList<String>();
        Matcher exampleInstanceMatcher = exampleInstancePattern.matcher(line);
        while(exampleInstanceMatcher.find()) {
            bits.add(exampleInstanceMatcher.group(1));
        }
        return bits;
    }

    // TODO: Explain why this isn't private ?
    public Map<String,ArrayList<String>> getVariables(){
        return this.variables;
    }

    /**
     * @return The count of data 'rows' which were parsed/set into this GherkinVariables object.
     * For example: A table with 1 row of content causes a result of 1 to be returned.
     */
    public int getNumberOfInstances() {
        try {
            return variables.entrySet().iterator().next().getValue().size();
        }catch(NoSuchElementException nsee) {
            return 0;
        }
    }

    // TODO: Explain why these are not Map<String,String> as everything else is typed this way
    /**
     * Gets the map of variable instances. ie: A 'row' of data values, which can be accessed
     * from the map using the column header in the Gherkin table.
     * @param instance The instance number. zero-based. Instance 0 corresponds to the first line of the 
     * Gherkin table data.
     * @return A map, such that the key is the trimmed-string of the column header in the Gherkin table, and 
     * that the object is the value of that table item.
     * @throws IndexOutOfBoundsException when the instance parameter specified exceeds the number of data rows in the 
     * Gherkin table (where the first row has an index of 0).
     */
    public Map<String,Object> getVariableInstance(int instance) {
        HashMap<String,Object> result = new HashMap<>();
        for(String key : this.variables.keySet()) {
            result.put(key, this.variables.get(key).get(instance));
        }
        return result;
    }

    public Map<String,Object> getVariablesOriginal(){
        HashMap<String, Object> result = new HashMap<>();
        for(String s : getVariables().keySet()) {
            result.put(s, getVariables().get(s));
        }
        return result;
    }
}
