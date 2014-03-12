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
  
 package ftl.argumentation.visualization;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import ftl.argumentation.core.ABUI;
import ftl.argumentation.core.AMAIL;
import ftl.argumentation.core.Argument;
import ftl.argumentation.core.ArgumentAcceptability;
import ftl.argumentation.core.ArgumentationBasedLearning;
import ftl.argumentation.core.ArgumentationState;
import ftl.argumentation.core.ArgumentationTree;
import ftl.argumentation.core.LaplaceArgumentAcceptability;
import ftl.argumentation.visualization.amail.panels.AgentPanel;
import ftl.argumentation.visualization.amail.panels.ArgumentationAgentPanel;
import ftl.argumentation.visualization.amail.panels.ArgumentationPanel;
import ftl.argumentation.visualization.amail.panels.ArgumentationPanelMultiScrolled;
import ftl.argumentation.visualization.amail.panels.TimeWindow;
import ftl.base.bridges.NOOSParser;
import ftl.base.core.BaseOntology;
import ftl.base.core.FTKBase;
import ftl.base.core.FeatureTerm;
import ftl.base.core.Ontology;
import ftl.base.core.Symbol;
import ftl.base.utils.FeatureTermException;
import ftl.base.utils.Pair;
import ftl.base.utils.RewindableInputStream;
import ftl.learning.core.Hypothesis;
import ftl.learning.core.Rule;
import ftl.learning.core.RuleHypothesis;
import ftl.learning.core.TrainingSetProperties;
import ftl.learning.core.TrainingSetUtils;
import ftl.learning.lazymethods.similarity.PropertiesDistance;

// TODO: Auto-generated Javadoc
/**
 * The Class AMAILVis.
 * 
 * @author santi
 */
public class AMAILVis {

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String args[]) {
		try {
			boolean CREDULOUS = true;
			int NAGENTS = 2;
			// int NAGENTS = 3;
			int CB = TrainingSetUtils.DEMOSPONGIAE_280_DATASET;
			// String inputFileName = "AMAILVis-Example-" + NAGENTS + "Agents.txt";
			String inputFileName = null;
			String outputFileName = "AMAILVis-Example-" + NAGENTS + "Agents.tmp.txt";
			
			// Load a data set:
			Ontology base_ontology = new BaseOntology();
			Ontology o = new Ontology();
			FTKBase dm = new FTKBase();
			FTKBase case_base = new FTKBase();
			o.uses(base_ontology);
			case_base.uses(dm);
			dm.create_boolean_objects(o);
			
			// Create an argumentation panel:
			createArgumentationPanel(CREDULOUS, NAGENTS, CB, inputFileName, outputFileName, o, dm, case_base);
			

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates the argumentation panel.
	 * 
	 * @param CREDULOUS
	 *            the cREDULOUS
	 * @param NAGENTS
	 *            the nAGENTS
	 * @param CB
	 *            the cB
	 * @param outputFileName
	 *            the output file name
	 * @param inputFile
	 *            the input file
	 * @param tw
	 *            the tw
	 * @param o
	 *            the o
	 * @param dm
	 *            the dm
	 * @param case_base
	 *            the case_base
	 * @param ts
	 *            the ts
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws Exception
	 *             the exception
	 * @throws UnsupportedEncodingException
	 *             the unsupported encoding exception
	 */
	private static void createArgumentationPanel(boolean CREDULOUS, int NAGENTS, int CB, String inputFileName, String outputFileName, 
			Ontology o, FTKBase dm, FTKBase case_base) throws FeatureTermException, IOException, Exception,
			UnsupportedEncodingException {
		
		TrainingSetProperties ts = TrainingSetUtils.loadTrainingSet(CB, o, dm, case_base);
		TimeWindow tw = new TimeWindow("AMAIL Visualization", 20);
		
		BufferedReader inputFile = null;
		
		if (inputFileName != null) {
			inputFile = new BufferedReader(new FileReader(inputFileName));
		}
		
		if (inputFile != null) {
			String line = inputFile.readLine();
			CB = Integer.parseInt(line);
		}
		
		// This list contains all the arguments being sent by all agents,
		// to have nice small IDs to show in screen
		HashMap<Argument, String> argumentIDs = new HashMap<Argument, String>();
		List<List<FeatureTerm>> case_bases = new LinkedList<List<FeatureTerm>>();
		List<FeatureTerm> allSolutions = Hypothesis.differentSolutions(ts.cases, ts.solution_path);
		FeatureTerm solution = null;
		ArgumentationState lastState = null;
		ArgumentationPanel lastPanel = null;

		if (inputFile != null) {
			String line = inputFile.readLine();
			for (FeatureTerm s : allSolutions) {
				System.out.println("Solution: " + s.toStringNOOS(dm));
				if (s.toStringNOOS(dm).equals(line)) {
					solution = s;
				}
			}
			if (solution == null) {
				System.err.println("Can't recover the solution from the file!!!");
				solution = allSolutions.get(0);
			}

			NAGENTS = Integer.parseInt(inputFile.readLine());
			for (int n = 0; n < NAGENTS; n++) {
				int nc = Integer.parseInt(inputFile.readLine());
				case_bases.add(new LinkedList<FeatureTerm>());
				for (int i = 0; i < nc; i++) {
					String cn = inputFile.readLine();
					List<FeatureTerm> l = new LinkedList<FeatureTerm>();
					l.addAll(case_base.searchFT(new Symbol(cn)));
					if (l.size() == 1) {
						case_bases.get(n).add(l.get(0));
					} else {
						System.err.println("Can't find case " + cn);
					}
				}
			}
		} else {
			case_bases = ftl.learning.core.TrainingSetUtils.splitTrainingSet(ts.cases, NAGENTS, ts.description_path, ts.solution_path, dm, 0.0, 0.0);

			solution = allSolutions.get(0);
		}

		List<ArgumentationBasedLearning> l_l = new LinkedList<ArgumentationBasedLearning>();
		List<RuleHypothesis> hypotheses = new LinkedList<RuleHypothesis>();
		float ACCEPTABILITY_THRESHOLD = 0.8f;
		List<ArgumentAcceptability> aaIndividuall = new LinkedList<ArgumentAcceptability>();
		for (int i = 0; i < NAGENTS; i++) {
			l_l.add(new ABUI());
			aaIndividuall.add(new LaplaceArgumentAcceptability(case_bases.get(i), ts.solution_path, ts.description_path, ACCEPTABILITY_THRESHOLD));
		}

		if (inputFile != null) {
			String ftString;
			String line;
			for (int n = 0; n < NAGENTS; n++) {
				int nh = Integer.parseInt(inputFile.readLine());
				RuleHypothesis h = new RuleHypothesis();
				for (int i = 0; i < nh; i++) {
					ftString = "";
					line = inputFile.readLine(); // <
					line = inputFile.readLine(); // <
					do {
						ftString += line + "\n";
						line = inputFile.readLine();
					} while (!line.equals(">"));
					FeatureTerm c = NOOSParser.parse(new RewindableInputStream(new ByteArrayInputStream(ftString.getBytes("UTF-8"))), case_base, o);

					h.addRule(new Rule(c, solution, 0, 0));
				}
				hypotheses.add(h);
			}
		} else {
			for (int n = 0; n < NAGENTS; n++) {
				hypotheses.add(((ABUI) l_l.get(n)).learnConceptABUI(case_bases.get(n), solution, new LinkedList<Argument>(), aaIndividuall.get(n),
						ts.description_path, ts.solution_path, o, dm));
			}
		}

		int step = 0;

		{
			File outputFile = new File(outputFileName);
			FileWriter fw = new FileWriter(outputFile);
			fw.write("" + CB + "\n");
			fw.write(solution.toStringNOOS(dm) + "\n");
			fw.write(NAGENTS + "\n");
			for (int n = 0; n < NAGENTS; n++) {
				fw.write(case_bases.get(n).size() + "\n");
				for (FeatureTerm c : case_bases.get(n)) {
					fw.write(c.getName().get() + "\n");
				}
			}
			for (int n = 0; n < NAGENTS; n++) {
				fw.write(hypotheses.get(n).getRules().size() + "\n");
				for (Rule r : hypotheses.get(n).getRules()) {
					fw.write("<\n" + r.pattern.toStringNOOS(dm) + "\n>\n");
				}
			}
			fw.close();
		}

		// Create a global panel for positioning the cases in the case-base:
		String name = ts.name + "-" + ts.cases.size();
		AgentPanel globalAgent = new AgentPanel(name, ts.cases, ts.solution_path, ts.description_path, dm, new PropertiesDistance(ts.cases, dm, o,
				ts.description_path, true), true, false, true);

		AMAIL argumentation = new AMAIL(hypotheses, solution, case_bases, aaIndividuall, l_l, CREDULOUS, ts.description_path, ts.solution_path, o, dm);
		// Add initial state:
		{
			List<AgentPanel> apl = new LinkedList<AgentPanel>();
			List<ArgumentAcceptability> aal = new LinkedList<ArgumentAcceptability>();
			for (int n = 0; n < NAGENTS; n++) {
				AgentPanel ap = new AgentPanel(argumentation.a_l.get(n).m_name, argumentation.a_l.get(n).m_examples, ts.solution_path, ts.description_path, dm,
						null, false, false, true);
				ap.setParent(globalAgent);
				apl.add(ap);
				aal.add(new LaplaceArgumentAcceptability(argumentation.a_l.get(n).m_examples, ts.solution_path, ts.description_path, ACCEPTABILITY_THRESHOLD));
			}
			ArgumentationPanel ap = new ArgumentationPanelMultiScrolled(apl, aal, dm, ts.solution_path, ts.description_path, argumentIDs);
			ArgumentationState as = argumentation.state;
			for (ArgumentationTree at : as.getTrees())
				ap.addTree(at.clone(), at.getRoot().m_agent, false);
			for (ArgumentationTree at : as.getRetractedTrees())
				ap.addTree(at.clone(), at.getRoot().m_agent, true);
			List<Pair<String, Object>> messages = new LinkedList<Pair<String, Object>>();
			// for(ArgumentationTree at:as.getTrees()) {
			// messages.add(new Pair<String,Object>(at.getRoot().m_agent,at.getRoot()));
			// }
			ap.m_messages = messages;
			tw.addStep("t" + step, ap);
			lastState = new ArgumentationState(as);
			lastPanel = ap;
			step++;
		}

		while (argumentation.moreRoundsP()) {
			argumentation.round(true);
			// Add final state:
			{
				List<AgentPanel> apl = new LinkedList<AgentPanel>();
				List<ArgumentAcceptability> aal = new LinkedList<ArgumentAcceptability>();
				for (int n = 0; n < NAGENTS; n++) {
					ArgumentationAgentPanel ap = new ArgumentationAgentPanel(argumentation.a_l.get(n).m_name, argumentation.a_l.get(n).m_examples,
							ts.solution_path, ts.description_path, dm, null, false, false, null);
					ap.setParent(globalAgent);
					apl.add(ap);
					aal.add(new LaplaceArgumentAcceptability(argumentation.a_l.get(n).m_examples, ts.solution_path, ts.description_path,
							ACCEPTABILITY_THRESHOLD));
				}
				ArgumentationPanel ap = new ArgumentationPanelMultiScrolled(apl, aal, dm, ts.solution_path, ts.description_path, argumentIDs);
				for (int n = 0; n < NAGENTS; n++) {
					((ArgumentationAgentPanel) (apl.get(n))).m_parent = ap;
				}
				ap.token = argumentation.getPreviousAgent();
				ArgumentationState as = argumentation.state;
				for (ArgumentationTree at : as.getTrees())
					ap.addTree(at.clone(), at.getRoot().m_agent, false);
				for (ArgumentationTree at : as.getRetractedTrees())
					ap.addTree(at.clone(), at.getRoot().m_agent, true);
				// Find messages:
				List<Pair<String, Object>> messages = new LinkedList<Pair<String, Object>>();
				messages.addAll(findMessages(lastState, as));
				messages.addAll(findMessages(lastPanel, ap));
				ap.m_messages = messages;
				tw.addStep("t" + step, ap);
				lastState = new ArgumentationState(as);
				lastPanel = ap;
				step++;
			}
		}
		List<RuleHypothesis> hAMAIL = argumentation.result();
		tw.repaint();
	}

	/**
	 * Find messages.
	 * 
	 * @param s0
	 *            the s0
	 * @param s1
	 *            the s1
	 * @return the list
	 */
	public static List<Pair<String, Object>> findMessages(ArgumentationState s0, ArgumentationState s1) {
		List<Pair<String, Object>> messages = new LinkedList<Pair<String, Object>>();

		List<ArgumentationTree> lt0 = s0.getAllTrees();
		List<ArgumentationTree> lt1 = s1.getAllTrees();

		// Look for new arguments and hypothesis:
		for (ArgumentationTree at1 : lt1) {
			List<Argument> l0 = null;
			List<Argument> l1 = at1.getAllArguments();
			for (ArgumentationTree at0 : lt0) {
				if (at1.getRoot() == at0.getRoot()) {
					l0 = at0.getAllArguments();
					break;
				}
			}

			if (l0 == null) {
				// new hypothesis:
				System.out.println("findArgumentMessags: new hypothesis found!!");
				messages.add(new Pair<String, Object>(at1.getRoot().m_agent, at1.getRoot()));
			} else {
				if (l1.size() > l0.size()) {
					// There is a new argument!
					for (Argument a : l1) {
						if (!l0.contains(a)) {
							// New attack:
							messages.add(new Pair<String, Object>(a.m_agent, a));
						}
					}
				}
			}
		}

		return messages;
	}

	/**
	 * Find messages.
	 * 
	 * @param p0
	 *            the p0
	 * @param p1
	 *            the p1
	 * @return the list
	 */
	public static List<Pair<String, Object>> findMessages(ArgumentationPanel p0, ArgumentationPanel p1) {
		List<Pair<String, Object>> messages = new LinkedList<Pair<String, Object>>();

		// find new examples:
		for (int n = 0; n < p0.apl.size(); n++) {
			for (FeatureTerm e : p1.apl.get(n).m_cases) {
				if (!p0.apl.get(n).m_cases.contains(e)) {
					// new case:
					messages.add(new Pair<String, Object>("other", e));
				}
			}
		}

		return messages;
	}

}
