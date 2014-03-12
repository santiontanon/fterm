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
  
 package ftl.learning.core;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import ftl.base.core.FTKBase;
import ftl.base.core.FTRefinement;
import ftl.base.core.FeatureTerm;
import ftl.base.core.Path;
import ftl.base.core.SetFeatureTerm;
import ftl.base.core.Sort;
import ftl.base.utils.FeatureTermException;

// TODO: Auto-generated Javadoc
/**
 * The Class TrainingSetProperties.
 * 
 * @author santi
 */
public class TrainingSetProperties {

	/** The solution_path. */
	public Path description_path = null, solution_path = null;

	/** The problem_sort. */
	public Sort problem_sort = null;

	/** The name. */
	public String name = null;

	/** The cases. */
	public List<FeatureTerm> cases = null;

	/**
	 * Different solutions.
	 * 
	 * @return the list
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public List<FeatureTerm> differentSolutions() throws FeatureTermException {
		List<FeatureTerm> l = new LinkedList<FeatureTerm>();
		for (FeatureTerm c : cases) {
			FeatureTerm s = c.readPath(solution_path);
			if (!l.contains(s))
				l.add(s);
		}

		return l;
	}

	/**
	 * Gets the case by name.
	 * 
	 * @param name
	 *            the name
	 * @return the case by name
	 */
	public FeatureTerm getCaseByName(String name) {
		for (FeatureTerm c : cases) {
			if (c.getName().equals(name))
				return c;
		}
		return null;
	}

	/**
	 * Prints the statistics.
	 * 
	 * @param dm
	 *            the dm
	 */
	public double printStatistics(FTKBase dm) {
		System.out.println("Data set name: " + name);
		System.out.println(cases.size() + " cases");
		System.out.println("Description path:" + description_path);
		System.out.println("Solution path:" + solution_path);

		HashMap<String, List<FeatureTerm>> solutions = new HashMap<String, List<FeatureTerm>>();

		for (FeatureTerm c : cases) {
			try {
				FeatureTerm s = c.readPath(solution_path);
				String ss = s.toStringNOOS(dm);
				List<FeatureTerm> l = solutions.get(ss);
				if (l == null) {
					l = new LinkedList<FeatureTerm>();
					solutions.put(ss, l);
				}
				l.add(c);
			} catch (FeatureTermException ex) {
				ex.printStackTrace();
			}
		}

		// solutions:
		System.out.println(solutions.keySet().size() + " solution classes");
		for (String ss : solutions.keySet()) {
			System.out.println(ss + " : " + solutions.get(ss).size());
		}

		// size of the terms:
		{
			int min_size = 0, max_size = 0;
			double avg_size = 0;
			int min_set = 0, max_set = 0;
			double avg_set = 0;
			int min_set_size = 0, max_set_size = 0;
			double avg_set_size = 0;
			boolean first = true;

			for (FeatureTerm c : cases) {
				int size = FTRefinement.variables(c).size();
				List<SetFeatureTerm> sets = FTRefinement.sets(c);
				int n_sets = sets.size();
				int minss = -1, maxss = -1;
				double avgss = 0;
				for (SetFeatureTerm s : sets) {
					if (minss == -1 || s.getSetValues().size() < minss)
						minss = s.getSetValues().size();
					if (maxss == -1 || s.getSetValues().size() > maxss)
						maxss = s.getSetValues().size();
					avgss += s.getSetValues().size();
				}
				avgss /= sets.size();

				System.out.println(c.getName() + " size: " + size + " , sets: " + minss + " - " + maxss);

				if (first) {
					min_size = max_size = size;
					min_set = max_set = n_sets;
					min_set_size = minss;
					max_set_size = maxss;
					first = false;
				} else {
					if (size < min_size)
						min_size = size;
					if (size > max_size)
						max_size = size;
					if (n_sets < min_set)
						min_set = n_sets;
					if (n_sets > max_set)
						max_set = n_sets;
					if (minss < min_set_size)
						min_set_size = minss;
					if (maxss > max_set_size)
						max_set_size = maxss;
				}
				avg_size += size;
				avg_set += n_sets;
				avg_set_size += avgss;
			}
			avg_size /= cases.size();
			avg_set /= cases.size();
			avg_set_size /= cases.size();
			System.out.println("Size in variables: [" + min_size + "," + max_size + "] (" + avg_size + ")");
			System.out.println("number of sets: [" + min_set + "," + max_set + "] (" + avg_set + ")");
			System.out.println("size of sets: [" + min_set_size + "," + max_set_size + "] (" + avg_set_size + ")");
			
			return avg_set;
		}
	}
}
