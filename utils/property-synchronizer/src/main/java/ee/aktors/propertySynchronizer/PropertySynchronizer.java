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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 * Synchronizes property files
 * @author karol.kartau
 */
public class PropertySynchronizer {
    private static final Logger logger = Logger.getLogger(PropertySynchronizer.class);

    /**
     * Synchronize targetFile with sourceFile and write result to resultFile.</br>
     * sourceFile and targetFile need to be in format defined in 
     * <a href="http://docs.oracle.com/cd/E23095_01/Platform.93/ATGProgGuide/html/s0204propertiesfileformat01.html">
     * 	http://docs.oracle.com/cd/E23095_01/Platform.93/ATGProgGuide/html/s0204propertiesfileformat01.html
     * </a>.</br>
     * Writes sourceFile to resultFile and replaces those properties in it which have properties with same name in targetFile with targetFile ones.</br>
     * Those properties from targetFile which do not have properties with same name in sourceFile will be placed to the end of resultFile
     * @param sourceFile
     * @param targetFile
     * @param resultFile
     * @param fileEncoding Encoding of files
     * @throws IOException If reading or writing some file has failed
     * @throws PropertySynchronizerException If sourceFile or targetFile structure is incorrect
    */
    public void synchronize(File sourceFile, File targetFile, File resultFile, String fileEncoding) throws IOException, PropertySynchronizerException {
        ArrayList<PropertyData> sourceDatas = readDatasFromPropertyFile(sourceFile, fileEncoding, true);
        ArrayList<PropertyData> targetDatas = readDatasFromPropertyFile(targetFile, fileEncoding, false);

        LinkedHashMap<String, PropertyData> targetDataMap = new LinkedHashMap<String, PropertyData>();
        for (PropertyData targetData:targetDatas) {
            targetDataMap.put(targetData.getName(), targetData);
        }

        int addedPropertiesCount = 0;
        int leftRemainingPropertiesCount = 0;
        int leftOverPropertiesCount = 0;

        ArrayList<String> resultLines = new ArrayList<String>();

        for (PropertyData sourceData:sourceDatas) {
            if (sourceData.isMetadata()) {
                resultLines.addAll(sourceData.getLines());
            } else {
                PropertyData targetData = targetDataMap.get(sourceData.getName());
                if (targetData == null) {
                    resultLines.addAll(sourceData.getLines());
                    addedPropertiesCount++;
                } else {
                    resultLines.addAll(targetData.getLines());
                    targetDataMap.remove(targetData.getName());
                    leftRemainingPropertiesCount++;
                }
            }
        }

        if (targetDataMap.size() != 0) {//If at least one left over property remains, then print blank line before them
            resultLines.add("");
        }
        for (Map.Entry<String, PropertyData> entry : targetDataMap.entrySet()) {
            resultLines.addAll(entry.getValue().getLines());
        }
        leftOverPropertiesCount = targetDataMap.entrySet().size();

        FileUtils.writeLines(resultFile, fileEncoding, resultLines);

        logger.info("Synchronization succeeded. Added "+addedPropertiesCount+" properties from source file, " +
            "left remaining " + leftRemainingPropertiesCount + " properties from target file and left over " +
            leftOverPropertiesCount + " properties from target file");
    }

    /**
     * Read propertyDatas from propertyFile
     * @param propertyFile
     * @param fileEncoding - Encoding of propertyFile
     * @param includeMetadata - Whether to include in result those propertyDatas, which are metadata
     * @return
     * @throws IOException - If reading propertyFile failed
     * @throws PropertySynchronizerException - If propertyFile structure is incorrect
    */
    private ArrayList<PropertyData> readDatasFromPropertyFile(File propertyFile, String fileEncoding, boolean includeMetadata) throws IOException, PropertySynchronizerException {
        ArrayList<PropertyData> datas = new ArrayList<PropertyData>();
        ArrayList<String> lines = (ArrayList<String>) FileUtils.readLines(propertyFile, fileEncoding);

        boolean isPreviousLineContinuous = false;
        for (String line : lines) {
            if (isPreviousLineContinuous == false) {
                if (isLineMetadata(line)) {
                    if (includeMetadata) {
                        datas.add(new PropertyData(line));
                    }
                } else {
                    datas.add(new PropertyData(getPropertyNameFromLine(line), line));
                    isPreviousLineContinuous = isLineContinuous(line);
                }
            } else {
                datas.get(datas.size()-1).getLines().add(line);
                isPreviousLineContinuous = isLineContinuous(line);
            }
        }

        return datas;
    }

    /**
     * Returns whether line is metadata.<br/>
     * Expects that previous line is not continuous.<br/>
     * Line is metadata, if previous line is not continuous and line is comment (begins with "#" or "!") or blank
     * @param line
     * @return
    */
    private boolean isLineMetadata(String line) {
        String trimmedLine = line.trim();
        if (trimmedLine.equals("")) {
            return true;
        }
        char firstChar = trimmedLine.charAt(0);
        return (firstChar == '#' || firstChar == '!');		
    }

    /**
     * Get property name from line.<br/>
     * Expects that previous line is not continuous and this line is not metadata.<br/>
     * Searches for first occurrence of "=" or ":" and takes trimmed string preceding it
     * @param line
     * @return
     * @throws PropertySynchronizerException If line does not contain property name or it is blank
    */
    private String getPropertyNameFromLine(String line) throws PropertySynchronizerException {
        int propertyNameEndIndex = -1;
        for (int i = 0; i < line.length(); i++) {
            char currentChar = line.charAt(i);
            if (currentChar == '=' || currentChar == ':') {
                propertyNameEndIndex = i;
                break;
            }
        }
        if (propertyNameEndIndex == -1) {
            throw new PropertySynchronizerException("Line \"" + line + "\" does not contain property name");
        }
        String propertyName = line.substring(0, propertyNameEndIndex).trim();
        if (propertyName.equals("")) {
            throw new PropertySynchronizerException("Line \"" + line + "\" contains blank property name");
        }
        return propertyName;
    }

    /**
     * Returns whether line is continuous (continues to next line).<br/>
     * Expects that line is not a metadata line.<br/>
     * Line which is not metadata, is continuous if it has odd number of backslashes at the end
     * @param line
     * @return
    */
    private boolean isLineContinuous(String line) {
        boolean isContinuous = false;
        for (int i = line.length() - 1; i >= 0; i--) {
            char currentChar = line.charAt(i);

            //Single slash, but escaped for Java
            if (currentChar == '\\') {
                isContinuous = (isContinuous==false);
            } else {
                break;
            }
        }
        return isContinuous;
    }
}
