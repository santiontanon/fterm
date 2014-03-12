/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Copyright (c) 2013, Santiago Ontañón All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution. Neither the name of
 * the IIIA-CSIC nor the names of its contributors may be used to endorse or promote products derived from this software
 * without specific prior written permission. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
  
 package ftl.base.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.List;

import ftl.base.core.FTKBase;
import ftl.base.core.FTRefinement;
import ftl.base.core.FeatureTerm;
import ftl.base.core.SetFeatureTerm;

/**
 * 
 * @author santi
 * 
 *         This class is used to collect subsumption examples
 */

class FTSRecord {
	FeatureTerm f1, f2;
	long time; // time with normal subsumption

	 public FTSRecord(FeatureTerm a_f1, FeatureTerm a_f2, long a_time) {
		f1 = a_f1;
		f2 = a_f2;
		time = a_time;
	}
}

/**
 * The Class FTSubsumptionRecord.
 */
public class FTSubsumptionRecord {

	/** The count. */
	static int count = 0;

	/** The bin max. */
	static int binMax = 10; // we will store 10 terms in each bin

	/** The positive. */
	static List<Pair<Integer, List<FTSRecord>>> positive = new LinkedList<Pair<Integer, List<FTSRecord>>>();

	/** The negative. */
	static List<Pair<Integer, List<FTSRecord>>> negative = new LinkedList<Pair<Integer, List<FTSRecord>>>();

	/**
	 * Register.
	 * 
	 * @param f1
	 *            the f1
	 * @param f2
	 *            the f2
	 * @param time
	 *            the time
	 * @param result
	 *            the result
	 */
	public static void register(FeatureTerm f1, FeatureTerm f2, long time, boolean result) {
		if (time > 0) {
			// System.out.println(time + " - " + result);
			List<FTSRecord> bin = getBin(time, result);

			if (bin.size() < binMax) {
				bin.add(new FTSRecord(f1, f2, time));

				// save test to summary file:
				try {
					FileWriter fstream = new FileWriter("subsumption-.txt", true);
					BufferedWriter out = new BufferedWriter(fstream);
					out.write(";; " + time + " " + result + "\n");
					out.write(f1.toStringNOOS());
					out.write("\n");
					out.write(f2.toStringNOOS());
					out.write("\n");
					out.flush();
					out.close();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
	}

	/**
	 * Dump summary.
	 */
	public static void dumpSummary() {
		System.out.println("With positive result:");
		for (Pair<Integer, List<FTSRecord>> bin : positive) {
			System.out.println("Bin " + bin.m_a + ": " + bin.m_b.size());
		}
		System.out.println("With negative result:");
		for (Pair<Integer, List<FTSRecord>> bin : negative) {
			System.out.println("Bin " + bin.m_a + ": " + bin.m_b.size());
		}
	}

	/**
	 * Dump statistics.
	 */
	public static void dumpStatistics() {
		for (Pair<Integer, List<FTSRecord>> bin : positive) {
			for (FTSRecord r : bin.m_b) {
                                System.out.println(FTRefinement.variables(r.f1).size() + ", " + FTRefinement.variables(r.f2).size() + ", " + maxSet(r.f1) + ", " + maxSet(r.f2) + ", " + totalSetSize(r.f1) + ", " + totalSetSize(r.f2) + ", " + r.time + ", true");
			}
		}
		for (Pair<Integer, List<FTSRecord>> bin : negative) {
			for (FTSRecord r : bin.m_b) {
				System.out.println(FTRefinement.variables(r.f1).size() + ", " + FTRefinement.variables(r.f2).size() + ", " + maxSet(r.f1) + ", " + maxSet(r.f2) + ", " + totalSetSize(r.f1) + ", " + totalSetSize(r.f2) + ", " + r.time + ", false");
			}
		}
	}

	/**
	 * Dump tests.
	 * 
	 * @param dm
	 *            the dm
	 */
	public static void dumpTests(FTKBase dm) {
		System.out.println(";; With positive result:");
		for (Pair<Integer, List<FTSRecord>> bin : positive) {
			System.out.println(";; Bin " + bin.m_a + ": " + bin.m_b.size());
			for (FTSRecord record : bin.m_b) {
				System.out.println(record.f1.toStringNOOS(dm));
				System.out.println(record.f2.toStringNOOS(dm));
			}
		}
		System.out.println(";; With negative result:");
		for (Pair<Integer, List<FTSRecord>> bin : negative) {
			System.out.println(";; Bin " + bin.m_a + ": " + bin.m_b.size());
			for (FTSRecord record : bin.m_b) {
				System.out.println(record.f1.toStringNOOS(dm));
				System.out.println(record.f2.toStringNOOS(dm));
			}
		}
	}

	/**
	 * Max set.
	 * 
	 * @param t
	 *            the t
	 * @return the int
	 */
	public static int maxSet(FeatureTerm t) {
		List<SetFeatureTerm> l = FTRefinement.sets(t);
		int maxSize = 0;

		for (SetFeatureTerm s : l) {
			if (s.getSetValues().size() > maxSize)
				maxSize = s.getSetValues().size();
		}

		return maxSize;
	}

	/**
	 * Total set size.
	 * 
	 * @param t
	 *            the t
	 * @return the int
	 */
	public static int totalSetSize(FeatureTerm t) {
		List<SetFeatureTerm> l = FTRefinement.sets(t);
		int size = 0;

		for (SetFeatureTerm s : l) {
			size += s.getSetValues().size();
		}

		return size;
	}

	/**
	 * Gets the bin.
	 * 
	 * @param time
	 *            the time
	 * @param result
	 *            the result
	 * @return the bin
	 */
	public static List<FTSRecord> getBin(long time, boolean result) {
		List<Pair<Integer, List<FTSRecord>>> list = null;
		int binID = (int) (time / 200);
		if (result)
			list = positive;
		else
			list = negative;

		for (Pair<Integer, List<FTSRecord>> bin : list) {
			if (bin.m_a == binID) {
				return bin.m_b;
			}
		}

		Pair<Integer, List<FTSRecord>> bin = new Pair<Integer, List<FTSRecord>>(binID, new LinkedList<FTSRecord>());
		list.add(bin);
		System.out.println("New bin created for " + binID);
		return bin.m_b;
	}
}
