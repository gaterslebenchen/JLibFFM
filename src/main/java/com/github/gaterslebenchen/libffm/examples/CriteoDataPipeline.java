/*
 * JLibFFM
 *
 * Copyright (c) 2017, Jinbo Chen(gaterslebenchen@gmail.com)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the docume
 *    ntation and/or other materials provided with the distribution.
 *  - Neither the name of the <ORGANIZATION> nor the names of its contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUD
 * ING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN N
 * O EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR C
 * ONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR P
 * ROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 *  TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBI
 *  LITY OF SUCH DAMAGE.
 */
package com.github.gaterslebenchen.libffm.examples;

import java.io.File;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.github.gaterslebenchen.libffm.tools.CmdLine;
import com.github.gaterslebenchen.libffm.tools.StackTraceUtil;

public class CriteoDataPipeline {
	private static final Logger logger = Logger.getLogger(CriteoDataPipeline.class);
	private static String tmpfolder = null;
	private static String session = null;

	public static void main(String[] args) {
		// Txt2Csv->Count->PrepareData->Format
		CmdLine cmdline = new CmdLine(args);
		System.out.println("----------------------------------------------------------------------------");
		System.out.println("Data transform and Feature engineering for Criteo data set");
		System.out.println("----------------------------------------------------------------------------");

		String param_input = cmdline.registerParameter("i", "set the input file path");
		String param_output = cmdline.registerParameter("o", "set the output file path");
		String param_thread = cmdline.registerParameter("s", "set number of threads (default 1)");
		String param_tmp = cmdline.registerParameter("t", "set the temporary Files path(default is current directory)");
		String param_format = cmdline.registerParameter("f",
				"use  `-f tr' for training data and `-f te' for test data");
		String param_help = cmdline.registerParameter("help", "print parameters");
		if (cmdline.hasParameter(param_help) || (args.length <= 1) || !cmdline.hasParameter(param_input)
				|| !cmdline.hasParameter(param_output) || !cmdline.hasParameter(param_format)) {
			cmdline.printHelp();
			return;
		}
		cmdline.checkParameters();
		String infomessage = "A serious error has occurred. For more detail information please refer to the log file:"+(System.getProperty("user.dir")  + File.separator + "jlibffm.log");
		session = UUID.randomUUID().toString();
		tmpfolder = cmdline.getValue(param_tmp, System.getProperty("user.dir"));
		try {
			String[] runparameters = { cmdline.getValue(param_format), cmdline.getValue(param_input),
					tmpfolder + File.separator + session + ".csv" };
			Txt2Csv.main(runparameters);
		} catch (final Exception e) {
			logger.error(StackTraceUtil.getStackTrace(e));
			System.err.println("can't format the inputfile to CSV format.");
			System.err.println(infomessage);
			cleanup(tmpfolder);
			System.exit(-1);
		}

		try {
			String[] runparameters = { tmpfolder + File.separator + session + ".csv",
					tmpfolder + File.separator + "fc.trva.t10.txt" };
			Count.main(runparameters);
		} catch (final Exception e) {
			logger.error(StackTraceUtil.getStackTrace(e));
			System.err.println("can't create "+tmpfolder + File.separator + "fc.trva.t10.txt");
			System.err.println(infomessage);
			cleanup(tmpfolder);
			System.exit(-1);
		}
		
		try {
			String[] runparameters = { cmdline.getValue(param_thread,"1"), tmpfolder + File.separator + session + ".csv", tmpfolder,
					tmpfolder + File.separator + session + ".csv.nn" };
			PrepareData.main(runparameters);
		} catch (final Exception e) {
			logger.error(StackTraceUtil.getStackTrace(e));
			System.err.println("can't generate the output file.");
			System.err.println(infomessage);
			cleanup(tmpfolder);
			System.exit(-1);
		}
		
		try {
			String[] runparameters = { tmpfolder + File.separator + session + ".csv.nn",
					cmdline.getValue(param_output) };
			Format.main(runparameters);
		} catch (final Exception e) {
			logger.error(StackTraceUtil.getStackTrace(e));
			System.err.println("can't generate the final output file.");
			System.err.println(infomessage);
			cleanup(tmpfolder);
			System.exit(-1);
		}
		
		cleanup(tmpfolder);
	}

	private static void cleanup(final String foldername) {
		File tempfile = new File(foldername + File.separator + session + ".csv");
		if (tempfile.exists()) {
			tempfile.delete();
		}
		
		tempfile = new File(foldername + File.separator + session + ".csv.nn");
		if (tempfile.exists()) {
			tempfile.delete();
		}

		tempfile = new File(foldername + File.separator + "fc.trva.t10.txt");
		if (tempfile.exists()) {
			tempfile.delete();
		}
	}

}
