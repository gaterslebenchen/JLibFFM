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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.github.gaterslebenchen.libffm.tools.CmdLine;
import com.github.gaterslebenchen.libffm.tools.StackTraceUtil;
import com.github.gaterslebenchen.libffm.tools.Util;

public class Main {
	private static final Logger logger = Logger.getLogger(Main.class);
	// number of features
	public int n;
	// number of fields
	public int m;
	// number of latent factors
	public int k;
	// Dynamic pointer to access values in the model
	public float[] W;
	public boolean normalization;

	public static void main(String[] args) {
		CmdLine cmdline = new CmdLine(args);

		System.out.println("----------------------------------------------------------------------------");
		System.out.println("JLibFFM");
		System.out.println(
				"A Java implementation of LIBFFM: A Library for Field-aware Factorization Machines(https://www.csie.ntu.edu.tw/~cjlin/libffm/)");
		System.out.println("Author: Jinbo Chen, gaterslebenchen@gmail.com");
		System.out.println("----------------------------------------------------------------------------");

		String param_eta = cmdline.registerParameter("e", "set learning rate (default 0.2)");
		String param_num_iter = cmdline.registerParameter("t", "set number of iterations (default 15)");
		String param_dim = cmdline.registerParameter("k", "set number of latent factors (default 4)");
		String param_lambda = cmdline.registerParameter("l", "set regularization parameter (default 0.00002)");
		String param_thread = cmdline.registerParameter("s", "set number of threads (default 1)");
		String param_train = cmdline.registerParameter("i", "set path to the training set");
		String param_valid = cmdline.registerParameter("p", "set path to the validation set");
		String param_random = cmdline.registerParameter("r", "By default we do data shuffling, you can use  `-r false' to disable this function.");
		String param_normal = cmdline.registerParameter("n", "By default we do instance-wise normalization. That is, we normalize the 2-norm of each instance to 1. You can use  `-n false' to disable this function.");
		String param_autostop = cmdline.registerParameter("a", "stop at the iteration that achieves the best validation loss (must be used with -p).");
		String param_help = cmdline.registerParameter("help", "this screen");
		if (cmdline.hasParameter(param_help) || (args.length <= 1) || !cmdline.hasParameter(param_train)) {
			cmdline.printHelp();
			return;
		}
		cmdline.checkParameters();
		
		// set learing parameter
		Model model = new Model(cmdline.getValue(param_eta,0.2f), cmdline.getValue(param_lambda,0.00002f), cmdline.getValue(param_num_iter,15),
				cmdline.getValue(param_dim,4), cmdline.getValue(param_normal,true), cmdline.getValue(param_random,true),
				cmdline.getValue(param_autostop,false), cmdline.getValue(param_thread,1));
		// load train dataset and valid dataset
		DataProvider traindata = new DataProvider(cmdline.getValue(param_train));
		DataProvider validdata = null;
		traindata.loadData();
		if(cmdline.hasParameter(param_valid))
		{
			validdata = new DataProvider(cmdline.getValue(param_valid));
			validdata.loadData();
		}
		
		// train model
		Main learn = new Main();
		learn.train(traindata, validdata, model);
	}

	private void init(int feature_num, int field_num, Model model) {
		n = feature_num;
		m = field_num;
		k = model.getK();
		normalization = model.isNormalization();
		W = new float[n * m * k * 2];

		float coef = (float) (1.0d / Math.sqrt(k));
		Random random = new Random();

		int position = 0;
		for (int j = 0; j < n; j++) {
			for (int f = 0; f < m; f++) {
				for (int d = 0; d < k; d++) {
					W[position] = coef * random.nextFloat();
					position += 1;
				}
				for (int d = this.k; d < 2 * this.k; d++) {
					W[position] = 1.f;
					position += 1;
				}
			}
		}
	}

	public float[] predict(DataProvider testdata, Model model) {
		testdata.normalize(model.isNormalization());
		float[] resutarr = new float[testdata.getInstancenumber()];
		float[] R_test = testdata.getR();
		for (int i = 0; i < testdata.getInstancenumber(); i++) {
			float r = R_test[i];
			resutarr[i] = wTx(testdata, i, r, 0.f, 0.f, 0.f, false);
		}
		return resutarr;
	}

	public void train(DataProvider traindata, DataProvider validdata, Model model) {
		init(traindata.getN(), traindata.getM(), model);
		traindata.normalize(model.isNormalization());

		if (validdata != null) {
			validdata.normalize(model.isNormalization());
		}
		double previousloss = Double.MAX_VALUE;

		for (int iter = 0; iter < model.getIternumber(); iter++) {
			if (model.isRandom()) {
				traindata.shuffle();
			}

			double tr_loss = splitTask(traindata, model);
			tr_loss /= traindata.getInstancenumber();
			System.out.printf("iter: %2d, tr_loss: %.5f", iter + 1, tr_loss);

			if (validdata != null && validdata.getInstancenumber() != 0) {
				double va_loss = 0d;
				for (int i = 0; i < validdata.getInstancenumber(); i++) {
					float y = validdata.getTargetarr()[i];
					float r = validdata.getR()[i];
					float t = wTx(validdata, i, r, 0.f, 0.f, 0.f, false);
					float expnyt = (float) Math.exp(-y * t);
					va_loss += Math.log(1 + expnyt);
				}
				va_loss /= validdata.getInstancenumber();
				System.out.printf(", va_loss: %.5f", va_loss);
				if (va_loss > previousloss && model.isAutostop()) {
					System.out.println();
					System.exit(0);
				}

				if (model.isAutostop()) {
					previousloss = va_loss;
					try {
						saveModel(model.modelfilename(iter));
					} catch (IOException e) {
						logger.error(StackTraceUtil.getStackTrace(e));
					}
				}
			}

			System.out.println();
		}
	}

	private double splitTask(DataProvider dataset, Model model) {
		double totalloss = 0d;
		int singletask = dataset.getInstancenumber() / model.getThreadnumber();
		if (singletask * model.getThreadnumber() < dataset.getInstancenumber()) {
			singletask++;
		}

		ExecutorService trainexecutorService = Executors.newFixedThreadPool(model.getThreadnumber());
		List<Future<Double>> resultList = new ArrayList<Future<Double>>();
		try {
			for (int i = 0; i < model.getThreadnumber(); i++) {
				Future<Double> future = trainexecutorService
						.submit(new TrainWorker(dataset, i * singletask, (i + 1) * singletask, model));
				resultList.add(future);
			}

			for (Future<Double> fs : resultList) {
				try {
					totalloss += fs.get();
				} catch (InterruptedException e) {
					logger.error(StackTraceUtil.getStackTrace(e));
				} catch (ExecutionException e) {
					logger.error(StackTraceUtil.getStackTrace(e));
				}
			}
		} catch (Exception e) {
			logger.error(StackTraceUtil.getStackTrace(e));
			System.exit(-1);
		} finally {
			trainexecutorService.shutdown();
		}
		return totalloss;
	}

	class TrainWorker implements Callable<Double> {
		private DataProvider dataset;
		private int startidx;
		private int endidx;
		private Model model;
		private double localloss = 0d;

		public TrainWorker(DataProvider dataset, int startidx, int endidx, Model model) {
			this.dataset = dataset;
			this.startidx = startidx;
			this.endidx = endidx;
			this.model = model;
		}

		public Double call() {
			try {
				if (endidx > dataset.getInstancenumber()) {
					endidx = dataset.getInstancenumber();
				}
				for (int ii = startidx; ii < endidx; ii++) {
					float y = dataset.getTargetarr()[ii];
					float r = dataset.getR()[ii];
					float t = wTx(dataset, ii, r, 0.f, 0.f, 0.f, false);
					float expnyt = (float) Math.exp(-y * t);
					localloss += Math.log(1 + expnyt);
					float kappa = -y * expnyt / (1 + expnyt);

					wTx(dataset, ii, r, kappa, model.getEta(), model.getLambda(), true);

				}
			} catch (Exception ex) {
				logger.fatal(StackTraceUtil.getStackTrace(ex));
			}

			return localloss;
		}
	}

	private float wTx(DataProvider prob, int i, float r, float kappa, float eta, float lambda, boolean do_update) {
		int start = Util.longa(prob.getP()[i]);
		int end = start + Util.longb(prob.getP()[i]);
		float t = 0.f;
		int align0 = k * 2;
		int align1 = m * align0;
		for (int N1 = start; N1 < end; N1++) {
			int j1 = prob.getFeaturearr()[N1];
			int f1 = prob.getFieldarr()[N1];
			float v1 = prob.getValuearr()[N1];
			if (j1 >= n || f1 >= m)
				continue;

			for (int N2 = N1 + 1; N2 < end; N2++) {
				int j2 = prob.getFeaturearr()[N2];
				int f2 = prob.getFieldarr()[N2];
				float v2 = prob.getValuearr()[N2];
				if (j2 >= n || f2 >= m)
					continue;

				int w1_index = j1 * align1 + f2 * align0;
				int w2_index = j2 * align1 + f1 * align0;
				float v = 2.f * v1 * v2 * r;

				if (do_update) {
					int wg1_index = w1_index + k;
					int wg2_index = w2_index + k;
					float kappav = kappa * v;
					for (int d = 0; d < k; d++) {
						float g1 = lambda * W[w1_index + d] + kappav * W[w2_index + d];
						float g2 = lambda * W[w2_index + d] + kappav * W[w1_index + d];

						float wg1 = W[wg1_index + d] + g1 * g1;
						float wg2 = W[wg2_index + d] + g2 * g2;

						W[w1_index + d] = W[w1_index + d] - eta / (float) (Math.sqrt(wg1)) * g1;
						W[w2_index + d] = W[w2_index + d] - eta / (float) (Math.sqrt(wg2)) * g2;

						W[wg1_index + d] = wg1;
						W[wg2_index + d] = wg2;
					}
				} else {
					for (int d = 0; d < k; d++) {
						t += W[w1_index + d] * W[w2_index + d] * v;
					}
				}
			}
		}
		return t;
	}

	public void saveModel(String path) throws IOException {
		DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(path)));
		dataOutputStream.writeInt(n);
		dataOutputStream.writeInt(m);
		dataOutputStream.writeInt(k);
		dataOutputStream.writeBoolean(normalization);

		int align0 = k * 2;
		int align1 = m * k * 2;
		for (int j = 0; j < n; j++) {
			for (int f = 0; f < m; f++) {
				for (int d = 0; d < k; d++) {
					dataOutputStream.writeFloat(W[j * align1 + f * align0 + d]);
				}
			}
		}

		dataOutputStream.close();
	}

	public Main loadModel(String path) throws IOException {
		Main model = new Main();
		DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(path)));
		model.n = dis.readInt();
		model.m = dis.readInt();
		model.k = dis.readInt();
		model.normalization = dis.readBoolean();
		model.W = new float[model.n * model.m * model.k * 2];
		int align0 = model.k * 2;
		int align1 = model.m * model.k * 2;
		for (int j = 0; j < model.n; j++) {
			for (int f = 0; f < model.m; f++) {
				for (int d = 0; d < model.k; d++) {
					model.W[j * align1 + f * align0 + d] = dis.readFloat();
				}
			}
		}
		dis.close();
		return model;
	}
}
