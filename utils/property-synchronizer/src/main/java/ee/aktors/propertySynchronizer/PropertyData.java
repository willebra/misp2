/*
 * The MIT License
 * Copyright (c) 2020- Nordic Institute for Interoperability Solutions (NIIS)
 * Copyright (c) 2019 Estonian Information System Authority (RIA)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */
package ee.aktors.propertySynchronizer;

import java.util.ArrayList;

/**
 * Data from property file. May represent either property data or metadata.
 * @author karol.kartau
 */
public class PropertyData {

    private String name = "";
    private ArrayList<String> lines = new ArrayList<String>();
    private boolean isMetadata = false;

    /**
     * Construct object holding data about property with specified name and first line
     * @param name
     * @param firstLine
    */
    public PropertyData(String name, String firstLine){
        this.name = name;
        lines.add(firstLine);
        isMetadata = name.equals("");
    }

    /**
     * Construct object holding metadata with specified first line
     * @param firstLine
    */
    public PropertyData(String firstLine){
        this("", firstLine);
    }

    /**
     * Get property name. If this object holds metada, then returns blank
     * @return
    */
    public String getName(){
        return name;
    }

    /**
     * Get lines of data (each line is one line in property file)
     * @return
    */
    public ArrayList<String> getLines(){
        return lines;
    }

    /**
     * Returns whether this object holds metadata (is name blank)
     * @return
    */
    public boolean isMetadata(){
        return isMetadata;
    }
}
