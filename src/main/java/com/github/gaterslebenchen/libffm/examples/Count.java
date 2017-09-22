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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.gaterslebenchen.libffm.tools.Util;

public class Count {

	public static void main(String[] args) throws Exception {
		String usage = "usage: com.github.gaterslebenchen.libffm.examples.Count input output";
		if (args.length != 2) {
			System.out.println(usage);
			System.exit(-1);
		}

		PrintWriter writer = new PrintWriter(new BufferedOutputStream(new FileOutputStream(args[1])));
		
		Map<String,Integer> negmap = new HashMap<String,Integer>();
		Map<String,Integer> posmap = new HashMap<String,Integer>();
		Map<String,Integer> totalmap = new HashMap<String,Integer>();
		
		BufferedReader br = null;
		try {
			String temp = null;
			boolean ishead = true;
			br = new BufferedReader(new InputStreamReader(new FileInputStream(args[0])));
			while ((temp = br.readLine()) != null) {
				if (ishead) {
					ishead = false;
					//ignore the head line
				}
				else
				{
					String[] arr = Util.split(temp, ",");
					for(int i=1;i<=26;i++)
					{
						String field = "C"+i;
						String value = arr[i+14];
						if("0".equals(arr[1]))
						{
							Util.fillinmap(negmap,field+','+value);
						}
						else
						{
							Util.fillinmap(posmap,field+','+value);
						}
						Util.fillinmap(totalmap,field+','+value);
					}
				}
				
			}
			
			writer.append("Field,Value,Neg,Pos,Total,Ratio");
	        writer.println();
	        
	        List<Map.Entry<String,Integer>> idlist = new ArrayList<Map.Entry<String,Integer>>(totalmap.entrySet());
			Collections.sort(idlist, new Comparator<Map.Entry<String, Integer>>() {    
	            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {    
	                if (o2.getValue() > o1.getValue())
	                {
	                	return 1;
	                }
	                else if(o2.getValue() < o1.getValue())
	                {
	                	return -1;
	                }
	                else 
	                {
	                	return o2.getKey().compareTo(o1.getKey());
	                }
	            }    
	        });  
			
			DecimalFormat df = new DecimalFormat("#.00000");
			for(Map.Entry<String,Integer> e : idlist) { 
				if(e.getValue()<10)
				{
					continue;
				}				
				writer.append(e.getKey() + ","+(negmap.containsKey(e.getKey()) ? negmap.get(e.getKey()) : "0")+","+(posmap.containsKey(e.getKey()) ? posmap.get(e.getKey()):"0")+","+e.getValue()+"," + df.format((1.0d * (posmap.containsKey(e.getKey()) ? posmap.get(e.getKey()) : 0d))/e.getValue()));
		        writer.println();
			}
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				if (br != null) {
					br.close();
				}
				if (writer != null) {
					writer.flush();
					writer.close();
				}
			} catch (Exception e) {
				// ignore
			}
		}
	}
}
