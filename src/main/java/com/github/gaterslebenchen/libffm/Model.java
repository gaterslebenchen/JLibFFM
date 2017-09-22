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

public class Model {
	// learning rate
	private float eta;
	// regularization parameter
	private float lambda;
	// iteration number
	private int iternumber;
	// Number of latent factors
	private int k;
	// instance-wise normalization
	private boolean normalization;
	// randomization training order of samples
	private boolean random;
	// stop at the iteration that achieves the best validation loss, must be
	// used with valid data set
	private boolean autostop;
	private int threadnumber;

	public Model(float eta, float lambda, int iternumber, int k, boolean normalization, boolean random,
			boolean autostop, int threadnumber) {
		super();
		this.eta = eta;
		this.lambda = lambda;
		this.iternumber = iternumber;
		this.k = k;
		this.normalization = normalization;
		this.random = random;
		this.autostop = autostop;
		this.threadnumber = threadnumber;
	}

	public float getEta() {
		return eta;
	}

	public void setEta(float eta) {
		this.eta = eta;
	}

	public float getLambda() {
		return lambda;
	}

	public void setLambda(float lambda) {
		this.lambda = lambda;
	}

	public int getIternumber() {
		return iternumber;
	}

	public void setIternumber(int iternumber) {
		this.iternumber = iternumber;
	}

	public int getK() {
		return k;
	}

	public void setK(int k) {
		this.k = k;
	}

	public boolean isNormalization() {
		return normalization;
	}

	public void setNormalization(boolean normalization) {
		this.normalization = normalization;
	}

	public boolean isRandom() {
		return random;
	}

	public void setRandom(boolean random) {
		this.random = random;
	}

	public boolean isAutostop() {
		return autostop;
	}

	public void setAutostop(boolean autostop) {
		this.autostop = autostop;
	}

	public int getThreadnumber() {
		return threadnumber;
	}

	public void setThreadnumber(int threadnumber) {
		this.threadnumber = threadnumber;
	}

	public String modelfilename(int it) {
		return "Model for Parameter[eta=" + eta + ", lambda=" + lambda + ", iternumber=" + iternumber + ", k=" + k
				+ ", normalization=" + normalization + ", random=" + random + ", Iteration=" + (it+1) + "]";
	}

}
