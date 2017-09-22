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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class Txt2Csv {

	public static void main(String[] args) throws Exception{
		String usage = "usage: com.github.gaterslebenchen.libffm.examples.Txt2Csv {tr|te} input output";
		String header = null;
		int idx = 0;
		if(args.length!=3)
		{
			System.out.println(usage);
			System.exit(-1);
		}
		if("tr".equals(args[0]))
		{
			header = "Id,Label,I1,I2,I3,I4,I5,I6,I7,I8,I9,I10,I11,I12,I13,C1,C2,C3,C4,C5,C6,C7,C8,C9,C10,C11,C12,C13,C14,C15,C16,C17,C18,C19,C20,C21,C22,C23,C24,C25,C26";
			idx = 10000000;
		}
		else if("te".equals(args[0]))
		{
			header = "Id,I1,I2,I3,I4,I5,I6,I7,I8,I9,I10,I11,I12,I13,C1,C2,C3,C4,C5,C6,C7,C8,C9,C10,C11,C12,C13,C14,C15,C16,C17,C18,C19,C20,C21,C22,C23,C24,C25,C26"; 
			idx = 60000000;
		}
		else
		{
			System.out.println(usage);
			System.exit(-1);
		}
		
		PrintWriter writer = new PrintWriter(new BufferedOutputStream(new FileOutputStream(args[2])));
		
		writer.append(header);
        writer.println();
        
        BufferedReader br = null;
        try
        {
        	String temp = null;
			br = new BufferedReader(new InputStreamReader(new FileInputStream(args[1])));
			while ((temp = br.readLine()) != null) {
				writer.append(Integer.toString(idx) + ',' + temp.replace('\t', ','));
		        writer.println();
		        idx++;
			}
        }
        catch(Exception e)
		{
			throw e;
		}
		finally
		{
			try
			{
				if(br!=null)
				{
					br.close();
				}
				if(writer!=null)
				{
					writer.flush();
					writer.close();
				}
			}
			catch(Exception e)
			{
				//ignore
			}
		}
	}

}
