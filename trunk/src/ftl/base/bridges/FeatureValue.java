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
  
 package ftl.base.bridges;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import ftl.base.core.FTKBase;
import ftl.base.core.FTRefinement;
import ftl.base.core.FeatureTerm;
import ftl.base.core.Ontology;
import ftl.base.core.Path;
import ftl.base.core.SetFeatureTerm;
import ftl.base.core.Sort;
import ftl.base.core.Symbol;
import ftl.base.core.TermFeatureTerm;
import ftl.base.utils.FeatureTermException;
import ftl.base.utils.Pair;

public class FeatureValue {
	private static Sort problem_sort = null;
	private static Sort description_sort = null;
	private static Sort solution_sort = null;
	private static Sort pes;
	private static Sort pds;

	/**
	 * To feature value.
	 * 
	 * @param examples
	 *            the examples
	 * @param sp
	 *            the sp
	 * @param dp
	 *            the dp
	 * @param o
	 *            the o
	 * @param dm
	 *            the dm
	 * @param targetOntology
	 *            the target ontology
	 * @param fillMissing
	 *            the fill missing
	 * @return the list
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public static List<FeatureTerm> toFeatureValue(Collection<FeatureTerm> examples, Path sp, Path dp, Ontology o, FTKBase dm, Ontology targetOntology,
			boolean fillMissing) throws FeatureTermException {
		List<FeatureTerm> propositionalExamples = new LinkedList<FeatureTerm>();
		List<Path> paths = new LinkedList<Path>();
		FeatureTerm unknown = null;

		getAllPaths(examples, sp, dp, dm, paths);
		removeSubPaths(paths);
		createNewSort(examples, dp, targetOntology, paths);
		unknown = convertExamples(examples, sp, dp, dm, targetOntology, fillMissing, propositionalExamples, paths, unknown, pes, pds);
		return propositionalExamples;
	}

	/**
	 * Create a new sort in the target ontology with the new flatteened example
	 * 
	 * @param examples
	 * @param dp
	 * @param targetOntology
	 * @param paths
	 * @throws FeatureTermException
	 */
	private static void createNewSort(Collection<FeatureTerm> examples, Path dp, Ontology targetOntology, List<Path> paths) throws FeatureTermException {
		{
			pes = new Sort("Propositional" + problem_sort.get(), null, targetOntology);
			pds = new Sort("Propositional" + description_sort.get(), null, targetOntology);

			pes.setDataType(Sort.DATATYPE_FEATURETERM);
			pds.setDataType(Sort.DATATYPE_FEATURETERM);

			pes.addFeature("description", pds.get(), null, targetOntology, true);
			pes.addFeature("solution", solution_sort.get(), null, targetOntology, true);

			for (Path p : paths) {
				Sort fs = null;

				for (FeatureTerm example : examples) {
					FeatureTerm d = example.readPath(dp);
					FeatureTerm v = d.readPath(p);

					if (v != null) {
						if (v instanceof SetFeatureTerm) {
							for (FeatureTerm v2 : ((SetFeatureTerm) v).getSetValues()) {
								Sort s = v2.getSort();
								if (fs == null) {
									fs = s;
								} else {
									fs = fs.Antiunification(s);
								}
							}
						} else {
							Sort s = v.getSort();
							if (fs == null) {
								fs = s;
							} else {
								fs = fs.Antiunification(s);
							}
						}
					} // if
				}

				pds.addFeature(p.toString(), fs.get(), null, targetOntology, true);
			}

			System.out.println("Sorts created:");
			System.out.println(pes.getDescription());
			System.out.println(pds.getDescription());
		}
	}

	/**
	 * Get all paths
	 * 
	 * @param examples
	 * @param sp
	 * @param dp
	 * @param dm
	 * @param paths
	 * @throws FeatureTermException
	 */
	private static void getAllPaths(Collection<FeatureTerm> examples, Path sp, Path dp, FTKBase dm, List<Path> paths) throws FeatureTermException {
		{
			for (FeatureTerm example : examples) {
				FeatureTerm description = example.readPath(dp);
				FeatureTerm solution = example.readPath(sp);
				// System.out.println(example.toStringNOOS(dm));
				{
					Sort es = example.getSort();
					Sort ds = description.getSort();
					Sort ss = solution.getSort();

					if (problem_sort == null) {
						problem_sort = es;
						description_sort = ds;
						solution_sort = ss;
					} else {
						problem_sort = problem_sort.Antiunification(es);
						description_sort = description_sort.Antiunification(ds);
						solution_sort = solution_sort.Antiunification(ss);
					}
				}

				for (Pair<FeatureTerm, Path> pair : FTRefinement.variablesWithPaths(description, dm)) {
					if (!paths.contains(pair.m_b)) {
						paths.add(pair.m_b);
						// } else {
						// System.out.println(pair.m_b + " not added");
					}
				}
				// break;
			}
		}
	}

	/**
	 * Convert all the examples to target ontology
	 * 
	 * @param examples
	 * @param sp
	 * @param dp
	 * @param dm
	 * @param targetOntology
	 * @param fillMissing
	 * @param propositionalExamples
	 * @param paths
	 * @param unknown
	 * @param pes
	 * @param pds
	 * @return
	 * @throws FeatureTermException
	 */
	private static FeatureTerm convertExamples(Collection<FeatureTerm> examples, Path sp, Path dp, FTKBase dm, Ontology targetOntology, boolean fillMissing,
			List<FeatureTerm> propositionalExamples, List<Path> paths, FeatureTerm unknown, Sort pes, Sort pds) throws FeatureTermException {
		for (FeatureTerm example : examples) {
			FeatureTerm d = example.readPath(dp);
			FeatureTerm s = example.readPath(sp);

			TermFeatureTerm pd, pe;

			if (example.getName() != null) {
				pe = new TermFeatureTerm("propositional" + example.getName().get(), pes);
			} else {
				pe = new TermFeatureTerm((String) null, pes);
			}

			if (d.getName() != null) {
				pd = new TermFeatureTerm("propositional" + d.getName().get(), pds);
			} else {
				pd = new TermFeatureTerm((String) null, pds);
			}

			for (Path p : paths) {
				FeatureTerm v = d.readPath(p);
				if (v != null) {
					pd.defineFeatureValue(new Symbol(p.toString()), v);
				} else {
					if (fillMissing) {
						if (unknown == null) {
							unknown = new TermFeatureTerm("unknown", targetOntology.getSort("any"));
							dm.addFT(unknown);
						}
						pd.defineFeatureValue(new Symbol(p.toString()), unknown);
					}
				}
			}

			pe.defineFeatureValue(new Symbol("description"), pd);
			pe.defineFeatureValue(new Symbol("solution"), s);

			propositionalExamples.add(pe);
		}
		return unknown;
	}

	/**
	 * Filter by removing paths that are subpaths of others (we only want the leaves)
	 * 
	 * @param paths
	 */
	private static void removeSubPaths(List<Path> paths) {
		boolean subPath = false;
		List<Path> to_delete = new LinkedList<Path>();

		for (Path p1 : paths) {
			for (Path p2 : paths) {
				if (p1 != p2) {
					if (p1.size() < p2.size()) {
						subPath = true;
						for (int i = 0; i < p1.size(); i++) {
							if (!p1.features.get(i).equals(p2.features.get(i))) {
								subPath = false;
								break;
							}
						}
						if (subPath)
							to_delete.add(p1);
					}
				}
			}
		}

		paths.removeAll(to_delete);

		System.out.println("Total number of paths after filtering: " + paths.size());
		for (Path p : paths) {
			System.out.println(p);
		}
	}
}
