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
import java.util.ResourceBundle;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

public class Main {
	private static final Logger logger = Logger.getLogger(Main.class);
	
	public static void main(String[] args) {		
		String sep = System.getProperty("line.separator");
		
		String sourceFilePath = "";
		String targetFilePath = "";
		String resultFilePath = "";
		String fileEncoding = "UTF-8";
		
		Options options = new Options();		
		options.addOption("h", "help", false, "Display this informative message about arguments");
		options.addOption("v", "version", false, "Display version information");
		options.addOption("s", "source", true, "Source properties file. Required");
		options.addOption("t", "target", true, "Target properties file. Required");
		options.addOption("r", "result", true, "Result properties file into which synchronized properties from target file are put. Required");
		options.addOption("e", "encoding", true, "Encoding of properties files (source, target and result). Default "+fileEncoding);
		
		try{
			CommandLineParser parser = new BasicParser();
			CommandLine line = parser.parse(options, args);
			
			if(line.hasOption("h")){
				HelpFormatter helpFormatter = new HelpFormatter();
				helpFormatter.printHelp("propertySynchronizer", options);
			}
			if(line.hasOption("v")){
				logger.info("Version: "+ResourceBundle.getBundle("config").getString("version"));
			}
			sourceFilePath = getRequiredOptionValue(line, "s");
			targetFilePath = getRequiredOptionValue(line, "t");
			resultFilePath = getRequiredOptionValue(line, "r");
			if(line.hasOption("e")){
				fileEncoding = line.getOptionValue("e");
			}
		}catch(ParseException e){
			logger.error(e);
			System.exit(1);
		}
		
		File sourceFile = new File(sourceFilePath);
		File targetFile = new File(targetFilePath);
		File resultFile = new File(resultFilePath).getAbsoluteFile();
		resultFile.getParentFile().mkdirs();
		
		if(sourceFile.exists()==false){
			logger.error("Source properties file missing: "+sep+sourceFile.getAbsolutePath());
			System.exit(1);
		}
		if(targetFile.exists()==false){
			logger.error("Target properties file missing: "+sep+targetFile.getAbsolutePath());
			System.exit(1);
		}
		
		try {
			new PropertySynchronizer().synchronize(sourceFile, targetFile, resultFile, fileEncoding);
		} catch (Exception e) {
			logger.error("Synchronization failed! ", e);
			System.exit(1);
		}
	}
	
	/**
	 * Get value of required option represented by shortOpt from commandLine
	 * @param line
	 * @param shortOpt
	 * @return
	 * @throws ParseException If commandLine does not have this option
	 */
	private static String getRequiredOptionValue(CommandLine commandLine, String shortOpt) throws ParseException{
		if(commandLine.hasOption(shortOpt)){
			return commandLine.getOptionValue(shortOpt);
		}else{
			throw new ParseException("Missing required option \""+shortOpt+"\"");
		}
	}

}
