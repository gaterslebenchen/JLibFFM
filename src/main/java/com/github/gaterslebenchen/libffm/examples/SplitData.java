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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.Random;

public class SplitData {
	public static void main(String[] args) throws Exception {
		String usage = "usage: com.github.gaterslebenchen.libffm.examples.SplitData inputfile outputfilefolder filehasheader";
		if (args.length != 3) {
			System.out.println(usage);
			System.exit(-1);
		}
		Random ra = new Random();

		BufferedWriter trainwrite = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(args[1] + File.separator + "tr_std.csv")));

		BufferedWriter validwrite = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(args[1] + File.separator + "va_std.csv")));

		Reader fr = new FileReader(args[0]);
		BufferedReader br = new BufferedReader(fr);
		String header = null;
		String line = null;
		while (br.ready()) {
			line = br.readLine();
			if("true".equals(args[2]) && header==null)
			{
				header = line;
				
				trainwrite.write(line);
				trainwrite.newLine();
				
				validwrite.write(line);
				validwrite.newLine();
				
				continue;
			}
			double dvalue = ra.nextDouble();
			if (dvalue > 0.8) {
				validwrite.write(line);
				validwrite.newLine();
			}  else {
				trainwrite.write(line);
				trainwrite.newLine();
			}

		}
		br.close();
		fr.close();
		
		trainwrite.flush();
		trainwrite.close();

		validwrite.flush();
		validwrite.close();
	}
}
