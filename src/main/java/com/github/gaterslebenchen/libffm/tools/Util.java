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
package com.github.gaterslebenchen.libffm.tools;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Util {
	public static long int2long(int a, int b) {
		return (long) a << 32 | b & 0xFFFFFFFFL;
	}

	public static int longa(long c) {
		return (int) (c >> 32);
	}

	public static int longb(long c) {
		return (int) c;
	}

	public static int hashstr(String str, int nr_bins) {
		try {
			long value = (new BigInteger(toHexString(str), 16)).longValue();
			value = Math.abs(value) % (nr_bins - 1);
			return (int) value + 1;
		} catch (Exception e) {
			throw new JlibffmRuntimeException(e);
		}
	}

	private static String toHexString(String str) throws Exception {
		StringBuffer hexString = new StringBuffer();
		byte[] bytesOfMessage = str.getBytes("UTF-8");
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] hash = md.digest(bytesOfMessage);

		for (int i = 0; i < hash.length; i++) {
			if ((0xff & hash[i]) < 0x10) {
				hexString.append("0" + Integer.toHexString((0xFF & hash[i])));
			} else {
				hexString.append(Integer.toHexString(0xFF & hash[i]));
			}
		}
		return hexString.toString();
	}
	
	public static void fillinmap(Map<String,Integer> map,String key)
	{
		if(map.containsKey(key))
		{
			map.put(key, map.get(key) + 1);
		}
		else
		{
			map.put(key, 1);
		}
	}

	public static String[] gen_feats(String[] arr) {
		String[] feats = new String[13 + 26];
		int idx = 0;
		for (int i = 2; i <= 14; i++) {
			String field = "I" + (i - 1);
			String value = arr[i];
			String key = field + "-";
			if (!value.equals("")) {
				int ivalue = Integer.parseInt(value);
				if (ivalue > 2) {
					ivalue = (int) (Math.log(ivalue * 1d) * Math.log(ivalue * 1d));
					key = key + ivalue;
				} else {
					value = "SP" + value;
					key = key + value;
				}
			}
			feats[idx++] = key;
		}

		for (int i = 15; i <= 40; i++) {
			String field = "C" + (i - 14);
			String value = arr[i];
			String key = field + "-" + value;
			feats[idx++] = key;
		}

		return feats;
	}

	public static Set<String> read_freqent_feats(int threshold, String filename) {
		Set<String> set = new HashSet<String>();
		BufferedReader br = null;
		try {
			String temp = null;
			boolean ishead = true;
			br = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
			while ((temp = br.readLine()) != null) {
				if (ishead) {
					ishead = false;
					// ignore the head line
				} else {
					String[] arr = Util.split(temp, ",");
					if (Integer.parseInt(arr[4]) >= threshold) {
						set.add(arr[0] + "-" + arr[1]);
					}
				}
			}

		} catch (Exception e) {
			throw new JlibffmRuntimeException(e);
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (Exception e) {
				// ignore
			}
		}
		return set;
	}

	public static String[] split(String s, String delimiter) {
		if (s == null) {
			return null;
		}
		int delimiterLength;
		int stringLength = s.length();
		if (delimiter == null || (delimiterLength = delimiter.length()) == 0) {
			return new String[] { s };
		}

		// a two pass solution is used because a one pass solution would
		// require the possible resizing and copying of memory structures
		// In the worst case it would have to be resized n times with each
		// resize having a O(n) copy leading to an O(n^2) algorithm.

		int count;
		int start;
		int end;

		// Scan s and count the tokens.
		count = 0;
		start = 0;
		while ((end = s.indexOf(delimiter, start)) != -1) {
			count++;
			start = end + delimiterLength;
		}
		count++;

		// allocate an array to return the tokens,
		// we now know how big it should be
		String[] result = new String[count];

		// Scan s again, but this time pick out the tokens
		count = 0;
		start = 0;
		while ((end = s.indexOf(delimiter, start)) != -1) {
			result[count] = (s.substring(start, end));
			count++;
			start = end + delimiterLength;
		}
		end = stringLength;
		result[count] = s.substring(start, end);

		return (result);
	}

	public static long estimateFilelines(String path) throws Exception {
		BufferedReader br = null;
		long filelines = 0L;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
			while (br.readLine() != null) {
				filelines++;
			}
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				br.close();
			} catch (Exception e) {
				// ignore
			}
		}

		return filelines;
	}
}
