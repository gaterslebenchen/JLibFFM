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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.log4j.Logger;

import com.github.gaterslebenchen.libffm.tools.StackTraceUtil;
import com.github.gaterslebenchen.libffm.tools.Util;

public class PrepareData {
	private static final Logger logger = Logger.getLogger(PrepareData.class);
	private static int nr_bins = 1000000;
	private static int threshold = 10;
	private static Set<String> frequent_feats = null;

	public static void main(String[] args) throws Exception {
		String usage = "usage: com.github.gaterslebenchen.libffm.examples.PrepareData threadnumber inputfile tmpfilefolder outputfile";
		if (args.length != 4) {
			System.out.println(usage);
			System.exit(-1);
		}
		int threadnumber = Integer.parseInt(args[0]);
		String filepath = args[1];
		String tmpfilefolder = args[2];
		String outputpath = args[3];
		long filelines = Util.estimateFilelines(filepath);
		frequent_feats = Util.read_freqent_feats(threshold, tmpfilefolder + File.separator + "fc.trva.t10.txt");
		splitTask(new File(filepath), filelines, threadnumber, tmpfilefolder, outputpath);
	}

	private static void splitTask(File file, long filelines, int threadnumber, String tmpfilefolder, String outputpath) throws IOException {
		long singleline = filelines / threadnumber;
		if (singleline * threadnumber < filelines) {
			singleline++;
		}

		ExecutorService trainexecutorService = Executors.newFixedThreadPool(threadnumber);
		List<Future<String>> resultList = new ArrayList<Future<String>>();
		try {
			for (long i = 0; i < threadnumber; i++) {
				Future<String> future = trainexecutorService.submit(new FormatWorker(file, i * singleline,
						(i + 1) * singleline, tmpfilefolder, new Long(i).intValue()));
				resultList.add(future);
			}
			List<String> filelist = new ArrayList<String>();
			for (Future<String> fs : resultList) {
				try {
					filelist.add(fs.get());
				} catch (InterruptedException e) {
					logger.error(StackTraceUtil.getStackTrace(e));
				} catch (ExecutionException e) {
					logger.error(StackTraceUtil.getStackTrace(e));
				}
			}
			Path outFile = Paths.get(outputpath);
			try(FileChannel out=FileChannel.open(outFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
			      for(int ix=0, n=filelist.size()-1; ix<n; ix++) {
			        Path inFile=Paths.get(filelist.get(ix));
			     
			        try(FileChannel in=FileChannel.open(inFile, StandardOpenOption.READ)) {
			          for(long p=0, l=in.size(); p<l; )
			            p+=in.transferTo(p, l-p, out);
			        }
			      }
			}
			for(String filepath:filelist)
			{
				File tmpfile = new File(filepath);
				if(tmpfile.exists())
				{
					tmpfile.delete();
				}
			}
		} catch (Exception e) {
			logger.error(StackTraceUtil.getStackTrace(e));
			System.err.println("For more detailed information please refer to " + System.getProperty("user.dir")
					+ File.separator + "jlibffm.log.");
			System.exit(-1);
		} finally {
			trainexecutorService.shutdown();
		}
	}

	static class FormatWorker implements Callable<String> {
		private File file;
		private long startline;
		private long endline;
		private long currentfileline = 0;
		private String outputfolder;
		private int idx;

		public FormatWorker(File file, long startline, long endline, String outputfolder, int idx) {
			this.file = file;
			this.startline = startline;
			this.endline = endline;
			this.outputfolder = outputfolder;
			this.idx = idx;
		}

		public String call() {
			BufferedReader br = null;
			PrintWriter writer = null;
			try {
				writer = new PrintWriter(
						new BufferedOutputStream(new FileOutputStream(outputfolder + File.separator + idx + ".tmpcsv")));
				br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
				String temp = null;
				while ((temp = br.readLine()) != null) {
					currentfileline++;
					if (currentfileline > startline && currentfileline <= endline) {
						if(idx==0 && currentfileline==1)
						{
							//ignore head line
							continue;
						}
						String[] strs = Util.split(temp, ",");
						List<ImmutablePair<Integer, String>> pairlist = new ArrayList<ImmutablePair<Integer, String>>();
						for (String feat : Util.gen_feats(strs)) {
							String field = Util.split(feat, "-")[0];
							Character type = field.charAt(0);
							int fieldidx = Integer.parseInt(field.substring(1));
							String featval = feat;

							if (type == 'C' && !frequent_feats.contains(feat)) {
								featval = field + "less";
							}
							if (type == 'C') {
								fieldidx += 13;
							}
							pairlist.add(new ImmutablePair<Integer, String>(fieldidx, featval));
						}

						List<ImmutablePair<Integer, Integer>> hashpairlist = new ArrayList<ImmutablePair<Integer, Integer>>();

						for (ImmutablePair<Integer, String> pair : pairlist) {
							hashpairlist.add(new ImmutablePair<Integer, Integer>(pair.getLeft(), Util.hashstr(pair.getRight(), nr_bins)));
						}

						hashpairlist.sort(new Comparator<ImmutablePair<Integer, Integer>>() {
							@Override
							public int compare(ImmutablePair<Integer, Integer> o1, ImmutablePair<Integer, Integer> o2) {
								if (o1.getLeft() > o2.getLeft()) {
									return 1;
								} else if (o1.getLeft().equals(o2.getLeft())) {
									return 0;
								} else {
									return -1;
								}
							}
						});

						List<Integer> featslist = new ArrayList<Integer>();
						for (ImmutablePair<Integer, Integer> pair : hashpairlist) {
							featslist.add(pair.getRight());
						}

						writer.append(strs[1] + " "
								+ featslist.stream().map(Object::toString).collect(Collectors.joining(" ")).toString());
						writer.println();
					} else if (currentfileline > endline) {
						break;
					}
				}
			} catch (Exception ex) {
				logger.fatal(StackTraceUtil.getStackTrace(ex));
			} finally {
				try {
					if (br != null) {
						br.close();
					}

					if (writer != null) {
						writer.flush();
						writer.close();
					}
				} catch (Exception ex) {
					// ignore
				}
			}
			return outputfolder + File.separator + idx + ".tmpcsv";
		}
	}
}
