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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import ftl.base.core.FTKBase;
import ftl.base.core.FeatureTerm;
import ftl.base.core.FloatFeatureTerm;
import ftl.base.core.IntegerFeatureTerm;
import ftl.base.core.Ontology;
import ftl.base.core.Path;
import ftl.base.core.SetFeatureTerm;
import ftl.base.core.Sort;
import ftl.base.core.Symbol;
import ftl.base.utils.FeatureTermException;
import ftl.base.utils.Pair;

// TODO: Auto-generated Javadoc
/**
 * The Class NOOSToWeka.
 */
public class NOOSToWeka {

	/**
	 * The Class ConversionRecord.
	 */
	public static class ConversionRecord {

		/** The all cases. */
		private List<FeatureTerm> allCases;

		/** The all weka cases. */
		private Instances allWekaCases;

		/**
		 * This array stores the mapping from weka solutions to FeatureTerms for the last time that the method
		 * 'toInstances' was called.
		 * */
		private FeatureTerm[] solutionMapping = null;

		/** The problems to cases. */
		public HashMap<FeatureTerm, FeatureTerm> problemsToCases;

		/**
		 * Gets the all weka cases.
		 * 
		 * @return the all weka cases
		 */
		public Instances getAllWekaCases() {
			return allWekaCases;
		}

		/**
		 * Sets the all weka cases.
		 * 
		 * @param allWekaCases
		 *            the new all weka cases
		 */
		public void setAllWekaCases(Instances allWekaCases) {
			this.allWekaCases = allWekaCases;
		}

		/**
		 * Gets the all cases.
		 * 
		 * @return the all cases
		 */
		public List<FeatureTerm> getAllCases() {
			return allCases;
		}

		/**
		 * Sets the all cases.
		 * 
		 * @param allCases
		 *            the new all cases
		 */
		public void setAllCases(List<FeatureTerm> allCases) {
			this.allCases = allCases;
		}

		/**
		 * Gets the solution mapping.
		 * 
		 * @return the solution mapping
		 */
		public FeatureTerm[] getSolutionMapping() {
			return solutionMapping;
		}

		/**
		 * Sets the solution mapping.
		 * 
		 * @param solutionMapping
		 *            the new solution mapping
		 */
		public void setSolutionMapping(FeatureTerm[] solutionMapping) {
			this.solutionMapping = solutionMapping;
		}
	}

	/** The s_conversion records. */
	static List<ConversionRecord> s_conversionRecords = new LinkedList<ConversionRecord>();

	/**
	 * This method converts a set of examples to the WEKA classes. The set of examples has to have been converted first
	 * to a propositional representation using the fterms.learning.propositional.Conversor class
	 * 
	 * @param examples
	 *            the feature terms list examples
	 * @param dp
	 *            the dp
	 * @param sp
	 *            the sp
	 * @param dm
	 *            the dm
	 * @param o
	 *            the o
	 * @return the conversion record
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public static ConversionRecord toInstances(List<FeatureTerm> examples, Path dp, Path sp, FTKBase dm, Ontology o) throws FeatureTermException {
		String name = null;
		FastVector attributes = null;
		Instances dataSet = null;
		long start_t, end_t;
		Sort descriptionSort = null;
		Sort exampleSort = null;
		Sort solutionSort = null;
		List<Pair<Path, Sort>> exampleFeatures = new LinkedList<Pair<Path, Sort>>();
		Sort integerSort = o.getSort("integer");
		Sort floatSort = o.getSort("float");
		attributes = new FastVector();

		ConversionRecord record = new ConversionRecord();

		start_t = System.currentTimeMillis();

		// Data set name and sort:
		{
			for (FeatureTerm e : examples) {
				FeatureTerm d = e.readPath(dp);
				FeatureTerm s = e.readPath(sp);
				descriptionSort = d.getSort();
				exampleSort = e.getSort();
				solutionSort = s.getSort();
				name = descriptionSort.get() + "-Instances";
				break;
			}
		}

		// Compute the set of attributes:
		{
			int index = 0;

			exampleFeatures.add(new Pair<Path, Sort>(sp, solutionSort));
			for (Symbol fname : descriptionSort.getFeatures()) {
				Path fpath = new Path(dp);
				fpath.features.add(fname);
				exampleFeatures.add(new Pair<Path, Sort>(fpath, descriptionSort.featureSort(fname)));
			}

			// System.out.println("Example sort " + descriptionSort.get() + " with " + exampleFeatures + " features");
			for (Pair<Path, Sort> feature : exampleFeatures) {
				Attribute a;
				String featureName = feature.m_a.toString();
				Sort featureSort = feature.m_b;

				// The solution class cannot be numeric, so the first attributed (index = 0) is treated specially:
				if (index != 0 && (integerSort.subsumes(featureSort) || floatSort.subsumes(featureSort))) {
					a = new Attribute(featureName);
					// System.out.println("Numeric feature: " + featureName);
				} else {
					FastVector possibleValues = new FastVector();
					List<FeatureTerm> allSolutionValues = null;

					if (index == 0)
						allSolutionValues = new LinkedList<FeatureTerm>();

					// Find the possible values:
					{
						for (FeatureTerm example : examples) {
							FeatureTerm v = example.readPath(feature.m_a);
							while (v != null && v instanceof SetFeatureTerm) {
								SetFeatureTerm sv = (SetFeatureTerm) v;
								if (!sv.getSetValues().isEmpty()) {
									v = sv.getSetValues().get(0);
								} else {
									v = null;
								}
							}
							if (v != null) {
								String sv = v.toStringNOOS(dm);
								sv = sv.replaceAll(" ", "-");
								if (!possibleValues.contains(sv)) {
									possibleValues.addElement(sv);
									if (index == 0)
										allSolutionValues.add(v);
								}
							}
						}
					}

					if (index == 0) {
						record.setSolutionMapping(new FeatureTerm[possibleValues.size()]);
						for (int i = 0; i < allSolutionValues.size(); i++) {
							record.getSolutionMapping()[i] = allSolutionValues.get(i);
						}
					}

					a = new Attribute(featureName, possibleValues);
					// System.out.println("Symbolic feature: " + featureName);
					// System.out.print("Possible values: ");
					// for(int i=0;i<possibleValues.size();i++) System.out.print("'" + possibleValues.elementAt(i) +
					// "' ");
					// System.out.println("");
				}

				// System.out.println(a.type());

				attributes.addElement(a);
				index++;
			}
		}

		dataSet = new Instances(name, attributes, examples.size());
		dataSet.setClassIndex(0);

		for (FeatureTerm example : examples) {
			FeatureTerm description = example.readPath(dp);
			FeatureTerm solution = example.readPath(sp);

			Instance i = new Instance(attributes.size());

			for (Pair<Path, Sort> feature : exampleFeatures) {
				int index = exampleFeatures.indexOf(feature);
				String featureName = feature.m_a.toString();
				Sort featureSort = feature.m_b;

				FeatureTerm v = example.readPath(feature.m_a);
				while (v != null && v instanceof SetFeatureTerm) {
					SetFeatureTerm sv = (SetFeatureTerm) v;
					if (!sv.getSetValues().isEmpty()) {
						v = sv.getSetValues().get(0);
					} else {
						v = null;
					}
				}
				if (v != null) {
					// The solution class cannot be numeric, so the first attributed (index = 0) is treated specially:
					if (index != 0 && v instanceof IntegerFeatureTerm) {
						i.setValue((Attribute) (attributes.elementAt(index)), (double) (((IntegerFeatureTerm) v).getValue()));
					} else if (index != 0 && v instanceof FloatFeatureTerm) {
						i.setValue((Attribute) (attributes.elementAt(index)), (double) (((FloatFeatureTerm) v).getValue()));
					} else {
						String sv = v.toStringNOOS(dm);
						sv = sv.replaceAll(" ", "-");
						i.setValue((Attribute) (attributes.elementAt(index)), sv);
					} // if
				}
			}
			dataSet.add(i);
		}

		record.setAllCases(examples);
		record.setAllWekaCases(dataSet);

		record.problemsToCases = new HashMap<FeatureTerm, FeatureTerm>();
		for (FeatureTerm c : record.getAllCases()) {
			FeatureTerm d = c.readPath(dp);
			record.problemsToCases.put(d, c);
		}

		end_t = System.currentTimeMillis();

		// System.out.println(dataSet);
		// System.out.println("NOOSToWeka.toInstances took " + (end_t-start_t));

		return record;
	}

	/**
	 * Translate subset.
	 * 
	 * @param subset
	 *            the subset
	 * @param originalSet
	 *            the original set
	 * @param completeTranslation
	 *            the complete translation
	 * @return the instances
	 */
	public static Instances translateSubset(List<FeatureTerm> subset, List<FeatureTerm> originalSet, Instances completeTranslation) {
		Instances wekaSubset = new Instances(completeTranslation, subset.size());

		for (FeatureTerm example : subset) {
			int i = originalSet.indexOf(example);
			wekaSubset.add(completeTranslation.instance(i));
		}

		// System.out.println(wekaSubset);

		return wekaSubset;
	}

	/**
	 * Translate instance.
	 * 
	 * @param example
	 *            the example
	 * @param originalSet
	 *            the original set
	 * @param completeTranslation
	 *            the complete translation
	 * @return the instance
	 */
	public static Instance translateInstance(FeatureTerm example, List<FeatureTerm> originalSet, Instances completeTranslation) {
		return completeTranslation.instance(originalSet.indexOf(example));
	}

}
