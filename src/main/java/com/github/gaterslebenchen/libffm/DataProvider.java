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
package com.github.gaterslebenchen.libffm;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import com.github.gaterslebenchen.libffm.tools.JlibffmRuntimeException;
import com.github.gaterslebenchen.libffm.tools.Util;

public class DataProvider {
	// Number of data points
	private int instancenumber = 0;
	// Number of non-zero elements
	private int nodenumber = 0;
	// the filed data array
	private int[] fieldarr = null;
	// the feature data array
	private int[] featurearr = null;
	// the value array
	private float[] valuearr = null;
	// The label array
	private float[] targetarr = null;
	// Used to store the data in a compressed sparse row (CSR) format
	private long[] P = null;

	// Number of features
	private int n;
	// Number of fields
	private int m;
	// Precomputed scaling factor to make the 2-norm of each instance to be 1
	private float[] R;

	private boolean normal;

	private String filepath;

	public float[] getR() {
		return R;
	}

	public int getInstancenumber() {
		return instancenumber;
	}

	public void setInstancenumber(int instancenumber) {
		this.instancenumber = instancenumber;
	}

	public int getNodenumber() {
		return nodenumber;
	}

	public void setNodenumber(int nodenumber) {
		this.nodenumber = nodenumber;
	}

	public int[] getFieldarr() {
		return fieldarr;
	}

	public void setFieldarr(int[] fieldarr) {
		this.fieldarr = fieldarr;
	}

	public int[] getFeaturearr() {
		return featurearr;
	}

	public void setFeaturearr(int[] featurearr) {
		this.featurearr = featurearr;
	}

	public float[] getValuearr() {
		return valuearr;
	}

	public void setValuearr(float[] valuearr) {
		this.valuearr = valuearr;
	}

	public float[] getTargetarr() {
		return targetarr;
	}

	public void setTargetarr(float[] targetarr) {
		this.targetarr = targetarr;
	}

	public long[] getP() {
		return P;
	}

	public int getN() {
		return n;
	}

	public void setN(int n) {
		this.n = n;
	}

	public int getM() {
		return m;
	}

	public void setM(int m) {
		this.m = m;
	}

	public DataProvider(String filepath) {
		this.filepath = filepath;
	}

	public void loadData() {
		BufferedReader br = null;
		try {
			String temp = null;
			br = new BufferedReader(new InputStreamReader(new FileInputStream(filepath)));
			while ((temp = br.readLine()) != null) {
				String[] strs = Util.split(temp, " ");
				instancenumber++;
				nodenumber += (strs.length - 1);
			}

			fieldarr = new int[nodenumber];
			featurearr = new int[nodenumber];
			valuearr = new float[nodenumber];
			targetarr = new float[instancenumber];
			P = new long[instancenumber];

			br.close();

			br = new BufferedReader(new InputStreamReader(new FileInputStream(filepath)));
			int i = 0;
			int p = 0;
			while ((temp = br.readLine()) != null) {
				String[] strs = Util.split(temp, " ");
				targetarr[i] = (Integer.parseInt(strs[0]) > 0) ? 1.f : -1.f;

				for (int j = 1; j < strs.length; j++) {
					String[] subFields = Util.split(strs[j], ":");
					fieldarr[p] = Integer.parseInt(subFields[0]);
					featurearr[p] = Integer.parseInt(subFields[1]);
					valuearr[p] = Float.parseFloat(subFields[2]);

					m = Math.max(m, fieldarr[i] + 1);
					n = Math.max(n, featurearr[i] + 1);

					p++;
				}
				
				P[i] = Util.int2long(p - strs.length + 1, strs.length - 1);
				i++;
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
	}

	public void normalize(boolean normal) {
		// Precomputed scaling factor to make the 2-norm of each instance to be 1
		this.normal = normal;
		R = new float[getInstancenumber()];
		if (normal) {
			for (int i = 0; i < getInstancenumber(); i++) {
				double norm = 0;
				for (int p = Util.longa(getP()[i]); p < (Util.longa(getP()[i])+Util.longb(getP()[i])); p++) {
					norm += getValuearr()[p] * getValuearr()[p];
				}
				R[i] = (float) (1.f / norm);
			}
		} else {
			Arrays.fill(R, 1.f);
		}
	}

	public void shuffle() {
		for (int i = 0; i < getInstancenumber(); i++) {
			int r = (int) (Math.random() * (i + 1));
			float swaptarget = targetarr[r];
			long swapp = P[r];

			targetarr[r] = targetarr[i];
			targetarr[i] = swaptarget;

			P[r] = P[i];
			P[i] = swapp;

			if (normal) {
				float swapr = R[r];
				R[r] = R[i];
				R[i] = swapr;
			}
		}
	}
}
