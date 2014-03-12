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

package ftl.demo;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.TextAttribute;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.ToolTipManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import javax.swing.text.PlainDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import ftl.argumentation.visualization.AMAILVis;
import ftl.base.bridges.ClausalForm;
import ftl.base.bridges.Clause;
import ftl.base.bridges.DisintegrationToWEKA;
import ftl.base.bridges.HornClauses;
import ftl.base.bridges.LaTeX;
import ftl.base.bridges.NOOSParser;
import ftl.base.bridges.NOOSToWeka;
import ftl.base.bridges.NOOSToWeka.ConversionRecord;
import ftl.base.core.BaseOntology;
import ftl.base.core.Disintegration;
import ftl.base.core.FTAntiunification;
import ftl.base.core.FTKBase;
import ftl.base.core.FTUnification;
import ftl.base.core.FeatureTerm;
import ftl.base.core.Ontology;
import ftl.base.core.Path;
import ftl.base.core.amalgam.Amalgam;
import ftl.base.core.amalgam.AmalgamEvaluationFunction;
import ftl.base.core.amalgam.AmalgamResult;
import ftl.base.core.amalgam.FTEFCompactness;
import ftl.base.utils.FTNameComparator;
import ftl.base.utils.FeatureTermException;
import ftl.base.utils.RewindableInputStream;
import ftl.base.visualization.CBVisualizer;
import ftl.base.visualization.CloseMacAction;
import ftl.base.visualization.FTVisualizer;
import ftl.base.visualization.PropertiesVisualizer;
import ftl.demo.xmlDemo.Demo;
import ftl.demo.xmlDemo.DemoFileList;
import ftl.demo.xmlDemo.DemoTest;
import ftl.learning.core.Hypothesis;
import ftl.learning.core.Prediction;
import ftl.learning.core.Rule;
import ftl.learning.core.RuleHypothesis;
import ftl.learning.core.TrainingSetProperties;
import ftl.learning.core.TrainingSetUtils;
import ftl.learning.inductivemethods.CN2;
import ftl.learning.inductivemethods.ID3;
import ftl.learning.inductivemethods.J48;
import ftl.learning.inductivemethods.PropertiesCN2;
import ftl.learning.inductivemethods.PropertiesTree;
import ftl.learning.inductivemethods.RefinementCN2;
import ftl.learning.inductivemethods.RefinementHYDRA;
import ftl.learning.inductivemethods.RefinementINDIE;
import ftl.learning.lazymethods.KNN;
import ftl.learning.lazymethods.KNNCSA;
import ftl.learning.lazymethods.LID;
import ftl.learning.lazymethods.RefinementLID;
import ftl.learning.lazymethods.similarity.AUDistance;
import ftl.learning.lazymethods.similarity.Distance;
import ftl.learning.lazymethods.similarity.KashimaKernel;
import ftl.learning.lazymethods.similarity.KashimaKernelDAGs;
import ftl.learning.lazymethods.similarity.KashimaKernelDAGsWithRoot;
import ftl.learning.lazymethods.similarity.KashimaKernelSparse;
import ftl.learning.lazymethods.similarity.KashimaKernelWithRoot;
import ftl.learning.lazymethods.similarity.PropertiesDistance;
import ftl.learning.lazymethods.similarity.RIBL;
import ftl.learning.lazymethods.similarity.SHAUD;
import ftl.learning.lazymethods.similarity.SHAUD2;
import ftl.learning.lazymethods.similarity.WeightedPropertiesDistance;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import java.awt.Toolkit;
import javax.swing.SwingConstants;
import javax.swing.ImageIcon;
import javax.swing.JTextPane;
import java.awt.SystemColor;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;

// TODO: Auto-generated Javadoc
/**
 * The Class FTLGUI.
 */

public class FTLGUI extends JFrame {
	// Log4j
	/** The Constant log. */
	private final static Logger log = Logger.getLogger(FTLGUI.class);

	/**
	 * The Enum bridges.
	 */
	public enum bridges {

		/** The NOO sto la tex. */
		NOOStoLaTex,
		/** The NOO sto clausal form. */
		NOOStoClausalForm,
		/** The Disintegration to weka. */
		DisintegrationToWEKA,
		/** The Horn clauses. */
		HornClauses;
	}

	/**
	 * The Enum distanceMethods.
	 */
	public enum distanceMethods {

		/** The AU distance. */
		AUDistance,
		/** The Kashima kernel. */
		KashimaKernel,
		/** The Kashima kernel da gs. */
		KashimaKernelDAGs,
		/** The Kashima kernel da gs with root. */
		KashimaKernelDAGsWithRoot,
		/** The Kashima kernel sparse. */
		KashimaKernelSparse,
		/** The Kashima kernel with root. */
		KashimaKernelWithRoot,
		/** The Properties distance. */
		PropertiesDistance,
		/** The RIBL. */
		RIBL,
		/** The SHAUD. */
		SHAUD,
		/** The SHAU d2. */
		SHAUD2,
		/** The Weighted properties distance. */
		WeightedPropertiesDistance
	}

	/** The dec. */
	DecimalFormat dec = new DecimalFormat("###.##");

	/**
	 * The Class Filter.
	 */
	private class Filter extends DocumentFilter {

		/** The Constant PROMPT. */
		private static final String PROMPT = "> ";

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.text.DocumentFilter#insertString(javax.swing.text.DocumentFilter.FilterBypass, int,
		 * java.lang.String, javax.swing.text.AttributeSet)
		 */
		@Override
		public void insertString(final FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
			if (string == null) {
				return;
			} else {
				Document doc = fb.getDocument();
				Element root = doc.getDefaultRootElement();
				int count = root.getElementCount();
				int index = root.getElementIndex(offset);
				Element cur = root.getElement(index);
				int promptPosition = cur.getStartOffset() + PROMPT.length();

				if (index == count - 1 && offset - promptPosition >= 0) {
					super.insertString(fb, offset, string, attr);
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.text.DocumentFilter#remove(javax.swing.text.DocumentFilter.FilterBypass, int, int)
		 */
		@Override
		public void remove(final FilterBypass fb, int offset, int length) throws BadLocationException {
			Document doc = fb.getDocument();
			Element root = doc.getDefaultRootElement();
			int count = root.getElementCount();
			int index = root.getElementIndex(offset);
			Element cur = root.getElement(index);
			int promptPosition = cur.getStartOffset() + PROMPT.length();

			if (index == count - 1 && offset - promptPosition >= 0) {
				super.remove(fb, offset, length);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.text.DocumentFilter#replace(javax.swing.text.DocumentFilter.FilterBypass, int, int,
		 * java.lang.String, javax.swing.text.AttributeSet)
		 */
		@Override
		public void replace(final FilterBypass fb, final int offset, final int length, String text, final AttributeSet attrs)
				throws BadLocationException {

			Document doc = fb.getDocument();
			Element root = doc.getDefaultRootElement();
			int count = root.getElementCount();
			int index = root.getElementIndex(offset);
			Element cur = root.getElement(index);
			int promptPosition = cur.getStartOffset() + PROMPT.length();

			if (index == count - 1 && offset - promptPosition >= 0) {
				if (text.equals("\n")) {
					text = "\n" + PROMPT;
				}
				super.replace(fb, offset, length, text, attrs);
			}
		}

	}

	/**
	 * The Enum heuristics.
	 */
	public enum heuristics {

		/** The HEURISTI c_ gain. */
		HEURISTIC_GAIN,
		/** The HEURISTI c_ rldm. */
		HEURISTIC_RLDM,
		/** The HEURISTI c_ entropy. */
		HEURISTIC_ENTROPY
	}

	/**
	 * The Enum inductiveMethods.
	 */
	public enum inductiveMethods {

		/** The C n2. */
		CN2,
		/** The I d3. */
		ID3,
		/** The J48. */
		J48,
		/** The Properties c n2. */
		PropertiesCN2,
		/** The Properties tree. */
		PropertiesTree,
		/** The Refinement c n2. */
		RefinementCN2,
		/** The Refinement hydra. */
		RefinementHydra,
		/** The Refinement indie. */
		RefinementINDIE;
	}

	/**
	 * The Enum lazyMethods.
	 */
	public enum lazyMethods {

		/** The KNN. */
		KNN,
		/** The KNNCSA. */
		KNNCSA,
		/** The LID. */
		LID,
		/** The Refinement lid. */
		RefinementLID;
	}

	/**
	 * The Enum selectionMode.
	 */
	public enum selectionMode {

		/** The SELEC t_ maximum. */
		SELECT_MAXIMUM,
		/** The SELEC t_ random. */
		SELECT_RANDOM,
		/** The SELEC t_ rando m_ ponderated. */
		SELECT_RANDOM_PONDERATED,
		/** The SELEC t_ minimum. */
		SELECT_MINIMUM
	}

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -629685983428356047L;

	/** The Constant titleFont. */
	private static final Font titleFont = new Font("Verdana", Font.BOLD, 14);

	/**
	 * Gets the console area.
	 * 
	 * @return the console area
	 */
	public static JTextArea getConsoleArea() {
		return consoleArea;
	}

	/**
	 * Initialize default dm.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	private static void initializeDefaultDM(String NOOSpath) throws IOException, FeatureTermException {
		resetOntology();
		dm.importNOOS(NOOSpath + "/family-ontology.noos", o);
		dm.importNOOS(NOOSpath + "/family-dm.noos", o);
		dm.importNOOS(NOOSpath + "/zoology-ontology.noos", o);
		dm.importNOOS(NOOSpath + "/zoology-dm.noos", o);
		dm.importNOOS(NOOSpath + "/sponge-ontology.noos", o);
		dm.importNOOS(NOOSpath + "/sponge-dm.noos", o);
		dm.importNOOS(NOOSpath + "/trains-ontology.noos", o);
		dm.importNOOS(NOOSpath + "/trains-dm.noos", o);
		// if (treeDemos != null){
		// TrainingSetUtils.loadTrainingSet(ontologyList.get(treeDemos.getSelectionPath().getPathComponent(1).toString()),
		// o, dm, case_base);
		// }
	}

	/**
	 * Launch the application.
	 * 
	 * @param args
	 *            the arguments
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void main(String[] args) throws FeatureTermException, IOException {
		PropertyConfigurator.configure("Resources/config/log4j.properties");
		resetOntology();
		initializeDefaultDM("NOOS");

		log.info("Execution started " + new Date());
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FTLGUI frame = new FTLGUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
					writeConsole("Error: " + e);
					log.error("Error: " + e);
				}
			}
		});
	}

	/**
	 * Reset ontology.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	private static void resetOntology() throws FeatureTermException {
		dm = new FTKBase();
		case_base = new FTKBase();
		case_base.uses(dm);
		Ontology base_ontology = new BaseOntology();
		o.uses(base_ontology);
		FTKBase base_domain_model = new FTKBase();

		base_domain_model.create_boolean_objects(base_ontology);
	}

	/**
	 * Sets the console area.
	 * 
	 * @param consoleArea
	 *            the new console area
	 */
	public static void setConsoleArea(JTextArea consoleArea) {
		FTLGUI.consoleArea = consoleArea;
	}

	/**
	 * Write console.
	 * 
	 * @param s
	 *            the s
	 */
	public static void writeConsole(String s) {
		try {
			getConsoleArea().setCaretPosition(getConsoleArea().getText().length());
			getConsoleArea().append(s + "\n> ");
			getConsoleArea().setCaretPosition(getConsoleArea().getText().length());
			return;
		} catch (Exception e) {
			getConsoleArea().setCaretPosition(getConsoleArea().getText().length());
			getConsoleArea().append(e + "\n> ");
			getConsoleArea().setCaretPosition(getConsoleArea().getText().length());
			e.printStackTrace();
			writeConsole("Error: " + e);
			log.error("Error: " + e);
		}
	}

	/** The content pane. */
	private JPanel generalContentPane;

	/** The panel ft. */
	private JPanel generalPanelFT;

	/** The ontology list. */
	private static TreeMap<String, Integer> ontologyList;

	/** The dm. */
	static FTKBase dm = new FTKBase();

	/** The o. */
	static Ontology o = new Ontology();

	/** The case_base. */
	static FTKBase case_base = new FTKBase();

	/** The panel_4. */
	public JPanel panelGeneralCB;

	/** The terms. */
	List<FeatureTerm> terms = new ArrayList<FeatureTerm>();

	/** The text area_3. */
	private static JTextArea consoleArea;

	/** The pan. */
	private CBVisualizer pan;

	/** The tree_1. */
	private JTree treeTests;

	/** The demo list tree. */
	private JTree operationsDemoListTree;

	/** The scroll pane_6. */
	private JScrollPane operationsDatasetTest;

	/** The c demo. */
	private Demo cDemo;

	/** The panels op tab. */
	private JPanel panelsOpTab;

	/** The panel demo f t1. */
	private JPanel panelDemoFT1;

	/** The panel demo f t2. */
	private JPanel panelDemoFT2;

	/** The result count. */
	private JTextField resultCount;

	/** The result list. */
	protected List<FeatureTerm> resultList;

	/** The result index. */
	private int resultIndex;

	/** The split pane1 ft. */
	private JPanel splitPane1FT;

	/** The panel ft result. */
	private JPanel operationsPanelFTResult;

	/** The btn previous. */
	private JButton btnPrevious;

	/** The btn next. */
	private JButton btnNext;

	/** The inductive slider value. */
	private JFormattedTextField inductiveSliderValue;

	/* Needed for Evaluate in inductive methods */
	/** The generated hypothesis. */
	protected Hypothesis generatedHypothesis;

	/** The test set cases. */
	protected ArrayList<FeatureTerm> testSetCases;

	/** The ts sol. */
	protected Path tsSol;

	/** The training set cases. */
	protected List<FeatureTerm> trainingSetCases;

	/** The ts. */
	protected TrainingSetProperties ts;

	/** The lazy slider value. */
	private JTextField lazySliderValue;

	/** The lazy test set panel. */
	public JPanel lazyTestSetPanel;

	/** The l methods list. */
	private JList lMethodsList;

	/** The k value. */
	private JTextField kValue;

	/** The gbc_btn lazy evaluate. */
	private GridBagConstraints gbc_btnLazyEvaluate;

	/** The lazy min precedents. */
	private JTextField lazyMinPrecedents;

	/** The distance combo box. */
	private JComboBox distanceComboBox;

	/** The lazy heuristic. */
	private JComboBox lazyHeuristic;

	/** The lazy selection. */
	private JComboBox lazySelection;

	/** The lazy chck box generalize. */
	private JCheckBox lazyChckBoxGeneralize;

	/** The lazy classify panel. */
	private JPanel lazyClassifyPanel;

	/** The lazy test set cases. */
	private ArrayList<FeatureTerm> lazyTestSetCases;

	/** The lazy prediction. */
	private Prediction lazyPrediction = null;

	/** The btn lazy evaluate. */
	private JButton btnLazyEvaluate;

	/** The term pane. */
	public JScrollPane termPane;

	/** The class rules. */
	private JPanel classRules;

	/** The rule index. */
	private JFormattedTextField ruleIndex;

	/** The rule hypothesis. */
	private ArrayList<RuleHypothesis> ruleHypothesis = new ArrayList<RuleHypothesis>();

	/** The inductive rules. */
	private JPanel inductiveRules;

	/** The rules list. */
	protected ArrayList<FeatureTerm> rulesList = new ArrayList<FeatureTerm>();

	/** The rule total. */
	private JFormattedTextField ruleTotal;

	/** The inductive control panel. */
	private JPanel inductiveControlPanel;

	/** The rule previous. */
	private JButton rulePrevious;

	/** The rule next. */
	private JButton ruleNext;

	/** The console. */
	private JScrollPane console;

	/** The inductive classes scroll. */
	private JScrollPane inductiveClassesScroll;

	/** The operations run btn. */
	private JButton operationsRunBtn;

	/** The tree demos. */
	private static JTree treeDemos;

	/** The operations demo f t1. */
	protected FeatureTerm operationsDemoFT1;

	/** The operations demo f t2. */
	protected FeatureTerm operationsDemoFT2;

	/** The actual ts. */
	protected String actualTs;

	/**
	 * Create the main frame.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public FTLGUI() throws Exception {
		this.setName("FTL Suite");
		setMinimumSize(new Dimension(800, 600));
		setMaximumSize(new Dimension(2560, 1440));
		setTitle("FTL Suite");
		generalSetUpOntologyList();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1600, 1024);
		generalContentPane = new JPanel();
		generalContentPane.setMaximumSize(new Dimension(2560, 1440));
		generalContentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(generalContentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 1259, 0 };
		gbl_contentPane.rowHeights = new int[] { 892, 0 };
		gbl_contentPane.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_contentPane.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		generalContentPane.setLayout(gbl_contentPane);

		JSplitPane splitPane_1 = new JSplitPane();
		splitPane_1.setResizeWeight(0.5);
		splitPane_1.setOneTouchExpandable(true);
		splitPane_1.setBorder(null);
		splitPane_1.setOrientation(JSplitPane.VERTICAL_SPLIT);
		GridBagConstraints gbc_splitPane_1 = new GridBagConstraints();
		gbc_splitPane_1.fill = GridBagConstraints.BOTH;
		gbc_splitPane_1.gridx = 0;
		gbc_splitPane_1.gridy = 0;
		generalContentPane.add(splitPane_1, gbc_splitPane_1);

		final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBorder(null);
		splitPane_1.setLeftComponent(tabbedPane);

		JPanel General = new JPanel();
		tabbedPane.addTab("General", null, General, null);
		GridBagLayout gbl_General = new GridBagLayout();
		gbl_General.columnWidths = new int[] { 293, 436, 0 };
		gbl_General.rowHeights = new int[] { 300, 0 };
		gbl_General.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_General.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		General.setLayout(gbl_General);

		JList generalOntologyList = createPanelGeneral(General);

		/*
		 * Create each front panel of the application
		 */

		createPanelOperations(tabbedPane);
		createPanelBridges(tabbedPane, General, generalOntologyList);
		createPanelInductive(tabbedPane);
		createPanelLazy(tabbedPane);
		createPanelMore(tabbedPane);

		JPanel About = new JPanel();
		tabbedPane.addTab("About FTL", null, About, null);
		GridBagLayout gbl_About = new GridBagLayout();
		gbl_About.columnWidths = new int[] { 400, 0, 0 };
		gbl_About.rowHeights = new int[] { 124, 0 };
		gbl_About.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_About.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		About.setLayout(gbl_About);

		MouseListener l = new MouseAdapter() {
			Font original;

			@Override
			public void mouseEntered(MouseEvent e) {
				original = e.getComponent().getFont();
				Map attributes = original.getAttributes();
				attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
				e.getComponent().setFont(original.deriveFont(attributes));
			}

			@Override
			public void mouseExited(MouseEvent e) {
				e.getComponent().setFont(original);
			}

		};

		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.insets = new Insets(0, 0, 0, 5);
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		About.add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0, 0 };
		gbl_panel.rowHeights = new int[] { 120, 0, 1, 0 };
		gbl_panel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 1.0, 1.0, 1.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		JLabel lblLogo_2 = new JLabel("");
		GridBagConstraints gbc_lblLogo_2 = new GridBagConstraints();
		gbc_lblLogo_2.insets = new Insets(0, 0, 5, 0);
		gbc_lblLogo_2.gridx = 0;
		gbc_lblLogo_2.gridy = 0;
		panel.add(lblLogo_2, gbc_lblLogo_2);
		lblLogo_2.setIcon(new ImageIcon(FTLGUI.class.getResource("/ftl/resources/img/ftl-logo_small.png")));

		JLabel lblIiia = new JLabel("");
		lblIiia.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		lblIiia.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				openWebpage(setUrl("http://www.iiia.csic.es/"));
			}
		});
		lblIiia.setIcon(new ImageIcon(FTLGUI.class.getResource("/ftl/resources/img/logoiiia-gran_small.png")));
		GridBagConstraints gbc_lblIiia = new GridBagConstraints();
		gbc_lblIiia.insets = new Insets(0, 0, 5, 0);
		gbc_lblIiia.gridx = 0;
		gbc_lblIiia.gridy = 1;
		panel.add(lblIiia, gbc_lblIiia);

		JLabel lblLogocsic = new JLabel("");
		lblLogocsic.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		lblLogocsic.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				openWebpage(setUrl("http://www.csic.es/"));
			}
		});
		lblLogocsic.setIcon(new ImageIcon(FTLGUI.class.getResource("/ftl/resources/img/CSIC_logo.jpeg")));
		GridBagConstraints gbc_lblLogocsic = new GridBagConstraints();
		gbc_lblLogocsic.gridx = 0;
		gbc_lblLogocsic.gridy = 2;
		panel.add(lblLogocsic, gbc_lblLogocsic);

		JPanel panel_1 = new JPanel();
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 1;
		gbc_panel_1.gridy = 0;
		About.add(panel_1, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[] { 0, 449, 0 };
		gbl_panel_1.rowHeights = new int[] { 124, 153, 1, 0 };
		gbl_panel_1.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_panel_1.rowWeights = new double[] { 1.0, 1.0, 1.0, Double.MIN_VALUE };
		panel_1.setLayout(gbl_panel_1);

		JPanel panel_4 = new JPanel();
		GridBagConstraints gbc_panel_4 = new GridBagConstraints();
		gbc_panel_4.fill = GridBagConstraints.BOTH;
		gbc_panel_4.insets = new Insets(0, 0, 5, 0);
		gbc_panel_4.gridx = 1;
		gbc_panel_4.gridy = 0;
		panel_1.add(panel_4, gbc_panel_4);
		GridBagLayout gbl_panel_4 = new GridBagLayout();
		gbl_panel_4.columnWidths = new int[] { 449, 0 };
		gbl_panel_4.rowHeights = new int[] { 0, 124, 0 };
		gbl_panel_4.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panel_4.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		panel_4.setLayout(gbl_panel_4);

		JLabel lblWhat = new JLabel("The FTL Suite");
		GridBagConstraints gbc_lblWhat = new GridBagConstraints();
		gbc_lblWhat.insets = new Insets(0, 0, 5, 0);
		gbc_lblWhat.gridx = 0;
		gbc_lblWhat.gridy = 0;
		panel_4.add(lblWhat, gbc_lblWhat);
		lblWhat.setFont(titleFont);

		JTextPane txtpnFtermIsA = new JTextPane();
		txtpnFtermIsA.setContentType("text/html");
		txtpnFtermIsA.setBackground(SystemColor.window);

		// center text
		StyledDocument doc = txtpnFtermIsA.getStyledDocument();
		SimpleAttributeSet center = new SimpleAttributeSet();
		StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
		doc.setParagraphAttributes(0, doc.getLength(), center, false);
		//

		HyperlinkListener l1 = new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (HyperlinkEvent.EventType.ACTIVATED == e.getEventType()) {
					openWebpage(e.getURL());
				}

			}
		};

		GridBagConstraints gbc_txtpnFtermIsA = new GridBagConstraints();
		gbc_txtpnFtermIsA.fill = GridBagConstraints.BOTH;
		gbc_txtpnFtermIsA.gridx = 0;
		gbc_txtpnFtermIsA.gridy = 1;
		panel_4.add(txtpnFtermIsA, gbc_txtpnFtermIsA);
		txtpnFtermIsA.setEditable(false);
		String fontfamily = txtpnFtermIsA.getFont().getFamily();
		txtpnFtermIsA
				.setText("<body style=\"font-family: "
						+ fontfamily
						+ "\"<br /> The FTL Suite is a platform for reasoning and learning upon the Feature Term (FTerm) formalism. FTL Suite supports the creation of ontologies, and the basic operations for reasoning are subsumption, anti-unification and unification. "
						+ "Moreover, the novel operation of amalgamating (or blending) terms is included, as well a novel similarity measures for complex situations represented as FTerms. <br /><br />\r\r"
						+ "The core of the FTL Suite is a collection of refinement operators which can be used as the basis of many  algorithms for reasoning and learning. "
						+ "The FTL Suite includes a number of implemented methods for learning using inductive concept learning approaches and CBR (lazy learning) approaches. "
						+ "These methods include classical techniques, classical techniques re-implemented upon refinement operators, and new techniques developed by the authors. "
						+ "The FTL Suite implements several classical and new similarity measures (implemented upon refinement operators) for complex objets represented as FTerms. "
						+ "<br /><br />\r\rImplemented techniques include similarity measures, FOIL (HIDRA), LID, INDIE, ABUI, AMAIL, and there is also a package to perform argumentation using FTerms. "
						+ "Translators from terms to other formats such as Horn Clauses, or even LaTeX figures are also available.\r<br /><br />\rFinally, a collection of datasets represented as feature terms is included (most from the UCI machine learning repository),"
						+ " the user can select a specific dataset and use a learning method on it (either inductive concept learning methods or case-based lazy learning techniques). However, be aware the not all methods are applicable to all datasets when their requirements do not match.");

		JPanel panel_3 = new JPanel();
		GridBagConstraints gbc_panel_3 = new GridBagConstraints();
		gbc_panel_3.insets = new Insets(0, 0, 5, 0);
		gbc_panel_3.fill = GridBagConstraints.BOTH;
		gbc_panel_3.gridx = 1;
		gbc_panel_3.gridy = 1;
		panel_1.add(panel_3, gbc_panel_3);
		GridBagLayout gbl_panel_3 = new GridBagLayout();
		gbl_panel_3.columnWidths = new int[] { 0, 0 };
		gbl_panel_3.rowHeights = new int[] { 0, 0, 0 };
		gbl_panel_3.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panel_3.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		panel_3.setLayout(gbl_panel_3);

		JLabel lblLibrary = new JLabel("Authorship and Ownership");
		lblLibrary.setFont(titleFont);
		GridBagConstraints gbc_lblLibrary = new GridBagConstraints();
		gbc_lblLibrary.insets = new Insets(0, 0, 5, 0);
		gbc_lblLibrary.gridx = 0;
		gbc_lblLibrary.gridy = 0;
		panel_3.add(lblLibrary, gbc_lblLibrary);

		final JTextPane txtpnTheCoreLibraries = new JTextPane();
		txtpnTheCoreLibraries.setEditable(false);
		txtpnTheCoreLibraries.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
		txtpnTheCoreLibraries.setContentType("text/html");
		txtpnTheCoreLibraries.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {

			}
		});
		txtpnTheCoreLibraries.setBackground(SystemColor.window);
		txtpnTheCoreLibraries
				.setText("<body style=\"font-family: Lucida Grande\"<br /> The core libraries and the impemented methods have been authored by Santigo Ontañón, in collaboration with Enric Plaza; the interface has been authored by Carlos López de Toro, in collaboration with Enric Plaza. <br />\rThe FTL Suite has been developed by the authors at Barcelona’s Artificial Intelligence Research Institute <a href=\"http://www.iiia.csic.es\">IIIA-CSIC.</a><br />\rThe FTL Suite is distributed as open source under the Code license <a href=\"http://opensource.org/licenses/BSD-3-Clause\">New BSD License.</a>\r");
		GridBagConstraints gbc_txtpnTheCoreLibraries = new GridBagConstraints();
		gbc_txtpnTheCoreLibraries.fill = GridBagConstraints.BOTH;
		gbc_txtpnTheCoreLibraries.gridx = 0;
		gbc_txtpnTheCoreLibraries.gridy = 1;
		panel_3.add(txtpnTheCoreLibraries, gbc_txtpnTheCoreLibraries);
		addHyperlinkToTextPane(txtpnTheCoreLibraries, l1);

		JPanel panel_5 = new JPanel();
		GridBagConstraints gbc_panel_5 = new GridBagConstraints();
		gbc_panel_5.fill = GridBagConstraints.BOTH;
		gbc_panel_5.gridx = 1;
		gbc_panel_5.gridy = 2;
		panel_1.add(panel_5, gbc_panel_5);
		GridBagLayout gbl_panel_5 = new GridBagLayout();
		gbl_panel_5.columnWidths = new int[] { 591, 0 };
		gbl_panel_5.rowHeights = new int[] { 0, 16, 0 };
		gbl_panel_5.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panel_5.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		panel_5.setLayout(gbl_panel_5);

		JLabel lblHow = new JLabel("Acknowledgments");
		GridBagConstraints gbc_lblHow = new GridBagConstraints();
		gbc_lblHow.insets = new Insets(0, 0, 5, 0);
		gbc_lblHow.gridx = 0;
		gbc_lblHow.gridy = 0;
		panel_5.add(lblHow, gbc_lblHow);
		lblHow.setFont(titleFont);

		final JTextPane txtpnTheFtlSuite = new JTextPane();
		txtpnTheFtlSuite.setEditable(false);
		txtpnTheFtlSuite.setContentType("text/html");

		txtpnTheFtlSuite.setBackground(SystemColor.window);
		String text = new String(
				"<body style=\"font-family: "
						+ fontfamily
						+ "\"<br /> The FTL Suite development has been partially funded by two projects:<br />"
						+ "<ol>"
						+ "<li><a href=\"http://www.iiia.csic.es/node/789\">MID-CBR.</a> An Integrative Framework for Developing Case-based Systems (TIN2006-15140-C03-01), and</li>"
						+ "<li><a href=\"http://www.iiia.csic.es/node/3421\">Next-CBR.</a> Evolving CBR for multi-source experience and knowledge-rich applications (MICIN TIN2009-13692-C03-01)</li>"
						+ "</ol>"
						+ "The FTL Suite is an (re)evolution of the NOOS representation language designed by Josep-Lluís Arcos and Enric Plaza.<br />");
		txtpnTheFtlSuite.setText(text);
		GridBagConstraints gbc_txtpnTheFtlSuite = new GridBagConstraints();
		gbc_txtpnTheFtlSuite.fill = GridBagConstraints.BOTH;
		gbc_txtpnTheFtlSuite.gridx = 0;
		gbc_txtpnTheFtlSuite.gridy = 1;
		panel_5.add(txtpnTheFtlSuite, gbc_txtpnTheFtlSuite);

		addHyperlinkToTextPane(txtpnTheFtlSuite, l1);
		createPanelConsole(splitPane_1);

	}

	/**
	 * Adds the hyperlink to text pane.
	 *
	 * @param txtpnTheFtlSuite the txtpn the ftl suite
	 * @param l1 the l1
	 */
	private void addHyperlinkToTextPane(final JTextPane txtpnTheFtlSuite, HyperlinkListener l1) {
		ToolTipManager.sharedInstance().registerComponent(txtpnTheFtlSuite);
		txtpnTheFtlSuite.addHyperlinkListener(l1);
	}

	/**
	 * Bridge list listener.
	 * 
	 * @param tabbedPane
	 *            the tabbed pane
	 * @param General
	 *            the general
	 * @param generalOntologyList
	 *            the general ontology list
	 * @param bridgeList
	 *            the bridge list
	 * @param bridgeViewTerm
	 *            the bridge view term
	 */
	private void bridgeListListener(final JTabbedPane tabbedPane, final JPanel General, final JList generalOntologyList,
			final JList bridgeList, final JButton bridgeViewTerm) {
		bridgeList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				bridgeViewTerm.setEnabled(false);
				System.out.println(bridgeList.getSelectedValue());
				if (bridgeList.getSelectedValue().toString().equals("DisintegrationToWEKA")) {
					String disvalue = (String) generalOntologyList.getSelectedValue() + " will be disintegrated";
					if (generalOntologyList.getSelectedValue() == null)
						disvalue = "You have to select some Ontology on GENERAL tab";
					String message = "Select the Ontology that you want to disintegrate in GENERAL tab. INPUT text will be ignored. \nThe operation will take a while to load \n"
							+ disvalue;
					writeConsole(message);
					log.info(message);
					log.info(message);
					JOptionPane.showMessageDialog(new JFrame(), message, "Error", JOptionPane.ERROR_MESSAGE);
					tabbedPane.setSelectedComponent(General);
				}
			}
		});
	}

	/**
	 * Bridge run listener.
	 * 
	 * @param generalOntologyList
	 *            the general ontology list
	 * @param bridgeList
	 *            the bridge list
	 * @param splitPane_3
	 *            the split pane_3
	 * @param bridgeInput
	 *            the bridge input
	 * @param bridgeResult
	 *            the bridge result
	 * @param bridgeRun
	 *            the bridge run
	 * @param bridgeViewTerm
	 *            the bridge view term
	 */
	private void bridgeRunListener(final JList generalOntologyList, final JList bridgeList, final JSplitPane splitPane_3,
			final JTextArea bridgeInput, final JTextArea bridgeResult, JButton bridgeRun, final JButton bridgeViewTerm) {
		bridgeRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				FeatureTerm ft = null;
				String input = null;
				input = bridgeInput.getText();
				input = input.replace('"', ' ');
				input = input.replace('+', ' ');
				input = input.trim();
				bridgeInput.setText(input);

				if (bridgeList.getSelectedIndex() == -1) {
					String message = "Please select a bridge";
					JOptionPane.showMessageDialog(new JFrame(), message, "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (input.isEmpty()) {
					if (!bridgeList.getSelectedValue().toString().equals("DisintegrationToWEKA")) {
						String message = "Empty box!";
						JOptionPane.showMessageDialog(new JFrame(), message, "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}

				try {
					if (input != null) {
						bridgeViewTerm.setEnabled(true);
						switch ((bridges) bridgeList.getSelectedValue()) {
						case NOOStoLaTex:
							ft = NOOSParser.parse(new RewindableInputStream(new ByteArrayInputStream(input.getBytes("UTF-8"))), case_base,
									o);
							bridgeResult.setText(LaTeX.toLaTeXTerm(ft, dm, true));
							break;

						case NOOStoClausalForm:
							ft = NOOSParser.parse(new RewindableInputStream(new ByteArrayInputStream(input.getBytes("UTF-8"))), case_base,
									o);
							bridgeResult.setText(ClausalForm.toLaTeXTerm(ft, dm, true));
							break;

						case DisintegrationToWEKA:
							try {

								if (generalOntologyList.getSelectedValue() != null) {
									FTLGUI.this.setEnabled(false);
									setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
									bridgeInput.setText((String) generalOntologyList.getSelectedValue() + "");
									bridgeInput.revalidate();
									splitPane_3.revalidate();
									FTLGUI.this.setEnabled(true);
									bridgeResult.setText(DisintegrationToWEKA.disintegrate(ontologyList.get(generalOntologyList
											.getSelectedValue())));

								}
								setCursor(null);
							} catch (Exception e) {
								e.printStackTrace();
								writeConsole("Error: " + e);
								log.error("Error: " + e);
							}
							break;

						case HornClauses:
							ft = NOOSParser.parse(new RewindableInputStream(new ByteArrayInputStream(input.getBytes("UTF-8"))), case_base,
									o);
							List<Clause> hornList = HornClauses.toClauses(ft, dm, "");
							bridgeResult.setText(hornList.toString());
							break;

						default:
							bridgeViewTerm.setEnabled(false);
							bridgeResult.setText("Please select another option");
							break;
						}
					}
				} catch (UnsupportedEncodingException e) {
					bridgeError(bridgeInput, e);
					return;
				} catch (IOException e) {
					bridgeError(bridgeInput, e);
					return;
				} catch (FeatureTermException e) {
					bridgeError(bridgeInput, e);
					return;
				} catch (Exception e) {
					bridgeError(bridgeInput, e);
					return;
				}
			}

			/**
			 * @param bridgeInput
			 * @param e
			 */
			private void bridgeError(final JTextArea bridgeInput, Exception e) {
				bridgeViewTerm.setEnabled(false);
				String message = "The input is not correct";
				JOptionPane.showMessageDialog(new JFrame(), message, "Error", JOptionPane.ERROR_MESSAGE);
				bridgeInput.requestFocus();
				e.printStackTrace();
				writeConsole("Error: " + e);
				log.error("Error: " + e);
			}
		});
	}

	/**
	 * Bridge view term listener.
	 * 
	 * @param bridgeInput
	 *            the bridge input
	 * @param bridgeViewTerm
	 *            the bridge view term
	 */
	private void bridgeViewTermListener(final JTextArea bridgeInput, final JButton bridgeViewTerm) {
		bridgeViewTerm.addMouseListener(new MouseAdapter() {
			/**
			 * @param bridgeInput
			 * @param e
			 */
			private void bridgeError(final JTextArea bridgeInput, Exception e) {
				bridgeViewTerm.setEnabled(false);
				String message = "The input is not correct";
				JOptionPane.showMessageDialog(new JFrame(), message, "Error", JOptionPane.ERROR_MESSAGE);
				bridgeInput.requestFocus();
				e.printStackTrace();
				writeConsole("Error: " + e);
				log.error("Error: " + e);
			}

			@Override
			public void mouseClicked(MouseEvent arg0) {
				FeatureTerm ft = null;
				String input = null;
				input = bridgeInput.getText();
				input = input.replace('"', ' ');
				input = input.replace('+', ' ');
				input = input.trim();
				bridgeInput.setText(input);

				try {
					ft = NOOSParser.parse(new RewindableInputStream(new ByteArrayInputStream(input.getBytes("UTF-8"))), case_base, o);
					JFrame frame;
					frame = FTVisualizer.newWindow("FTVisualizer demo", 640, 480, ft, dm, true, true);
					frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					CloseMacAction.addMacCloseBinding(frame);

					frame.setVisible(true);

				} catch (UnsupportedEncodingException e) {
					bridgeError(bridgeInput, e);
					e.printStackTrace();
					writeConsole("Error: " + e);
					log.error("Error: " + e);
					return;

				} catch (IOException e) {
					bridgeError(bridgeInput, e);
					e.printStackTrace();
					writeConsole("Error: " + e);
					log.error("Error: " + e);
					return;
				} catch (FeatureTermException e) {
					bridgeError(bridgeInput, e);
					e.printStackTrace();
					writeConsole("Error: " + e);
					log.error("Error: " + e);
					return;
				}

			}
		});
	}

	/**
	 * Btn lazy evaluate listener.
	 */
	private void btnLazyEvaluateListener() {
		btnLazyEvaluate.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (btnLazyEvaluate.isEnabled()) {

					int trueSols = 0;
					FeatureTerm sol = null;
					// recorrer
					for (FeatureTerm f : lazyTestSetCases) {
						try {
							sol = f.readPath(tsSol);
							Prediction pre = lazyPrediction;

							if (pre.solutions.size() == 1 && pre.solutions.get(0).equivalents(sol)) {
								trueSols++;
							}
						} catch (FeatureTermException e1) {
							log.error("Error: " + e1);
							e1.printStackTrace();
						} catch (Exception e1) {
							log.error("Error: " + e1);
							e1.printStackTrace();
						}
					}
					String message = "Porcentaje casos correctos: " + (float) ((float) trueSols / (float) lazyTestSetCases.size()) * 100
							+ "%";
					writeConsole(message);
					log.info(message);
					log.info(message);
					JOptionPane.showMessageDialog(new JFrame(), message, "Result", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
	}

	/**
	 * Btn next listener.
	 */
	private void btnNextListener() {
		btnNext.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (btnNext.isEnabled()) {
					resultIndex++;
					// Load new FT
					try {
						loadFT(resultList.get(resultIndex), operationsPanelFTResult);
					} catch (FeatureTermException e) {
						e.printStackTrace();
						writeConsole("Error: " + e);
						log.error("Error: " + e);
					}
					resultCount.setText(resultIndex + 1 + " of " + resultList.size());
					// Buttons activation

					btnNext.setEnabled(resultList.size() > resultIndex + 1);
					btnPrevious.setEnabled(resultIndex > 0);
					btnNext.revalidate();
					btnNext.setVisible(true);
				}
			}
		});
	}

	/**
	 * Btn previous listener.
	 */
	private void btnPreviousListener() {
		btnPrevious.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (btnPrevious.isEnabled()) {
					resultIndex--;
					// Load new FT
					try {
						loadFT(resultList.get(resultIndex), operationsPanelFTResult);
					} catch (FeatureTermException er) {
						log.error("Error: " + er);
						er.printStackTrace();
					}
					resultCount.setText(resultIndex + 1 + " of " + resultList.size());
					// Buttons activation

					btnNext.setEnabled(resultList.size() > resultIndex + 1);
					btnPrevious.setEnabled(resultIndex > 0);
					btnNext.revalidate();
					btnNext.setVisible(true);
				}
			}
		});
	}

	/**
	 * Console area listener.
	 */
	private void consoleAreaListener() {
		getConsoleArea().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				try {
					consoleKeyPressed(arg0);
				} catch (FeatureTermException e) {
					e.printStackTrace();
					writeConsole("Error: " + e);
					log.error("Error: " + e);
				} catch (BadLocationException e) {
					e.printStackTrace();
					writeConsole("Error: " + e);
					log.error("Error: " + e);
				}
			}
		});
	}

	/**
	 * Console key pressed.
	 * 
	 * @param arg0
	 *            the arg0
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws BadLocationException
	 *             the bad location exception
	 */
	private void consoleKeyPressed(KeyEvent arg0) throws FeatureTermException, BadLocationException {
		// Intro pressed
		if (arg0.getKeyCode() == 10 && !getConsoleArea().getText().isEmpty()
				&& ((getConsoleArea().getLineCount() - 1) == getConsoleArea().getLineOfOffset(getConsoleArea().getCaretPosition()))) {

			getConsoleArea().setCaretPosition(getConsoleArea().getText().length());
			String text = getConsoleArea().getText();
			int totalLines = getConsoleArea().getLineCount();
			int start = getConsoleArea().getLineStartOffset(totalLines - 1);
			int end = getConsoleArea().getLineEndOffset(totalLines - 1);
			String line = text.substring(start + 2, end);
			processConsoleInput(line, getConsoleArea());

		}
	}

	/**
	 * Creates the panel bridges.
	 * 
	 * @param tabbedPane
	 *            the tabbed pane
	 * @param General
	 *            the general
	 * @param generalOntologyList
	 *            the general ontology list
	 */
	private void createPanelBridges(final JTabbedPane tabbedPane, final JPanel General, final JList generalOntologyList) {
		JPanel Bridges = new JPanel();
		tabbedPane.addTab("Bridges", null, Bridges, null);
		GridBagLayout gbl_Bridges = new GridBagLayout();
		gbl_Bridges.columnWidths = new int[] { 301, 1002, 0 };
		gbl_Bridges.rowHeights = new int[] { 559, 0 };
		gbl_Bridges.columnWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
		gbl_Bridges.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		Bridges.setLayout(gbl_Bridges);

		JPanel panel_6 = new JPanel();
		panel_6.setBorder(null);
		GridBagConstraints gbc_panel_6 = new GridBagConstraints();
		gbc_panel_6.fill = GridBagConstraints.BOTH;
		gbc_panel_6.insets = new Insets(0, 0, 0, 5);
		gbc_panel_6.gridx = 0;
		gbc_panel_6.gridy = 0;
		Bridges.add(panel_6, gbc_panel_6);
		GridBagLayout gbl_panel_6 = new GridBagLayout();
		gbl_panel_6.columnWidths = new int[] { 182, 0 };
		gbl_panel_6.rowHeights = new int[] { 328, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panel_6.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panel_6.rowWeights = new double[] { 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panel_6.setLayout(gbl_panel_6);

		final JList bridgeList = new JList();

		bridgeList.setFont(new Font("Lucida Grande", Font.PLAIN, 14));
		bridgeList.setListData(bridges.values());
		bridgeList.setBorder(new TitledBorder(null, "Avaliable Operations", TitledBorder.LEADING, TitledBorder.TOP, titleFont, null));
		GridBagConstraints gbc_inductiveClassesList = new GridBagConstraints();
		gbc_inductiveClassesList.gridheight = 11;
		gbc_inductiveClassesList.insets = new Insets(0, 0, 5, 0);
		gbc_inductiveClassesList.fill = GridBagConstraints.BOTH;
		gbc_inductiveClassesList.gridx = 0;
		gbc_inductiveClassesList.gridy = 0;
		panel_6.add(bridgeList, gbc_inductiveClassesList);
		bridgeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		final JSplitPane splitPane_3 = new JSplitPane();
		splitPane_3.setResizeWeight(0.5);
		GridBagConstraints gbc_splitPane_3 = new GridBagConstraints();
		gbc_splitPane_3.fill = GridBagConstraints.BOTH;
		gbc_splitPane_3.gridx = 1;
		gbc_splitPane_3.gridy = 0;
		Bridges.add(splitPane_3, gbc_splitPane_3);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBorder(new TitledBorder(null, "Input Data", TitledBorder.LEADING, TitledBorder.TOP, titleFont, null));
		splitPane_3.setLeftComponent(scrollPane);

		final JTextArea bridgeInput = new JTextArea();
		scrollPane.setViewportView(bridgeInput);

		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setToolTipText("The result of the operation will be shown here");
		scrollPane_1.setBorder(new TitledBorder(null, "Result", TitledBorder.LEADING, TitledBorder.TOP, titleFont, null));
		splitPane_3.setRightComponent(scrollPane_1);

		final JTextArea bridgeResult = new JTextArea();
		bridgeResult.setEditable(false);
		scrollPane_1.setViewportView(bridgeResult);

		JButton bridgeRun = new JButton("Apply Operation");
		GridBagConstraints gbc_bridgeRun = new GridBagConstraints();
		gbc_bridgeRun.insets = new Insets(0, 0, 5, 0);
		gbc_bridgeRun.fill = GridBagConstraints.HORIZONTAL;
		gbc_bridgeRun.gridx = 0;
		gbc_bridgeRun.gridy = 11;
		panel_6.add(bridgeRun, gbc_bridgeRun);

		final JButton bridgeViewTerm = new JButton("View Feature Term");

		bridgeViewTerm.setEnabled(false);
		GridBagConstraints gbc_bridgeViewTerm = new GridBagConstraints();
		gbc_bridgeViewTerm.fill = GridBagConstraints.HORIZONTAL;
		gbc_bridgeViewTerm.gridx = 0;
		gbc_bridgeViewTerm.gridy = 12;
		panel_6.add(bridgeViewTerm, gbc_bridgeViewTerm);

		bridgeListListener(tabbedPane, General, generalOntologyList, bridgeList, bridgeViewTerm);
		bridgeRunListener(generalOntologyList, bridgeList, splitPane_3, bridgeInput, bridgeResult, bridgeRun, bridgeViewTerm);
		bridgeViewTermListener(bridgeInput, bridgeViewTerm);
	}

	/**
	 * Creates the console.
	 * 
	 * @param splitPane_1
	 *            the split pane_1
	 */
	private void createPanelConsole(JSplitPane splitPane_1) {
		console = new JScrollPane();
		console.setMinimumSize(new Dimension(23, 120));
		splitPane_1.setRightComponent(console);

		setConsoleArea(new JTextArea());
		getConsoleArea().setMinimumSize(new Dimension(0, 200));
		getConsoleArea().setText("> ");
		getConsoleArea().setWrapStyleWord(true);
		getConsoleArea().setToolTipText("Type help");

		getConsoleArea().setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		console.setViewportView(getConsoleArea());
		getConsoleArea().setLineWrap(true);
		consoleAreaListener();
		Filter filter = new Filter();
		((AbstractDocument) getConsoleArea().getDocument()).setDocumentFilter(filter);
	}

	/**
	 * Creates the panel general.
	 * 
	 * @param General
	 *            the general
	 * @return the j list
	 */
	private JList createPanelGeneral(JPanel General) {
		JScrollPane scrollPane_3 = new JScrollPane();
		scrollPane_3.setMaximumSize(new Dimension(290, 1200));
		GridBagConstraints gbc_scrollPane_3 = new GridBagConstraints();
		gbc_scrollPane_3.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_3.insets = new Insets(0, 0, 0, 5);
		gbc_scrollPane_3.gridx = 0;
		gbc_scrollPane_3.gridy = 0;
		General.add(scrollPane_3, gbc_scrollPane_3);
		scrollPane_3.setViewportBorder(new TitledBorder(null, "Ontology", TitledBorder.LEADING, TitledBorder.TOP, titleFont, null));

		JList generalOntologyList = new JList(ontologyList.keySet().toArray());
		scrollPane_3.setViewportView(generalOntologyList);

		JSplitPane splitPane = new JSplitPane();
		GridBagConstraints gbc_splitPane = new GridBagConstraints();
		gbc_splitPane.fill = GridBagConstraints.BOTH;
		gbc_splitPane.gridx = 1;
		gbc_splitPane.gridy = 0;
		General.add(splitPane, gbc_splitPane);
		splitPane.setResizeWeight(0.5);
		splitPane.setAlignmentY(Component.CENTER_ALIGNMENT);
		splitPane.setAlignmentX(Component.CENTER_ALIGNMENT);

		panelGeneralCB = new JPanel();
		panelGeneralCB.setMaximumSize(new Dimension(2000, 1000));
		splitPane.setLeftComponent(panelGeneralCB);
		panelGeneralCB.setToolTipText("-Select case to view\n-Press double-click to adjust zoom");
		panelGeneralCB.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Case Base", TitledBorder.LEADING,
				TitledBorder.TOP, titleFont, new Color(0, 0, 0)));

		// CBVisualizer pan = caseBasePanel(panel_4);
		panelGeneralCB.setLayout(new java.awt.BorderLayout());

		generalPanelFT = new JPanel();
		generalPanelFT.setMaximumSize(new Dimension(2000, 1000));
		generalPanelFT.setToolTipText("Press double-click to adjust zoom");
		splitPane.setRightComponent(generalPanelFT);
		generalPanelFT.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Case Visualizer",
				TitledBorder.LEADING, TitledBorder.TOP, titleFont, new Color(0, 0, 0)));
		// FTVisualizer ft = featureTermPanel(panel_2);
		generalPanelFT.setLayout(new java.awt.BorderLayout());

		// panel_2.add(ft);
		generalPanelFT.revalidate();

		generalOntologyListListener(generalOntologyList);
		return generalOntologyList;
	}

	/**
	 * Creates the inductive panel.
	 * 
	 * @param tabbedPane
	 *            the tabbed pane
	 */
	private void createPanelInductive(final JTabbedPane tabbedPane) {
		JPanel Inductive = new JPanel();
		tabbedPane.addTab("Inductive", null, Inductive, null);
		GridBagLayout gbl_Inductive = new GridBagLayout();
		gbl_Inductive.columnWidths = new int[] { 276, 987, 0 };
		gbl_Inductive.rowHeights = new int[] { 497, 0 };
		gbl_Inductive.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_Inductive.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		Inductive.setLayout(gbl_Inductive);

		JPanel inductiveInputPanel = new JPanel();
		inductiveInputPanel.setMaximumSize(new Dimension(385, 1200));
		GridBagConstraints gbc_inductiveInputPanel = new GridBagConstraints();
		gbc_inductiveInputPanel.insets = new Insets(0, 0, 0, 5);
		gbc_inductiveInputPanel.fill = GridBagConstraints.BOTH;
		gbc_inductiveInputPanel.gridx = 0;
		gbc_inductiveInputPanel.gridy = 0;
		Inductive.add(inductiveInputPanel, gbc_inductiveInputPanel);
		GridBagLayout gbl_inductiveInputPanel = new GridBagLayout();
		gbl_inductiveInputPanel.columnWidths = new int[] { 358, 0 };
		gbl_inductiveInputPanel.rowHeights = new int[] { 277, 169, 139, 120, 0 };
		gbl_inductiveInputPanel.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_inductiveInputPanel.rowWeights = new double[] { 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		inductiveInputPanel.setLayout(gbl_inductiveInputPanel);

		JScrollPane inductiveOntologyScroll = new JScrollPane();
		inductiveOntologyScroll.setBorder(new TitledBorder(null, "Ontology", TitledBorder.LEADING, TitledBorder.TOP, titleFont, null));
		GridBagConstraints gbc_inductiveOntologyScroll = new GridBagConstraints();
		gbc_inductiveOntologyScroll.insets = new Insets(0, 0, 5, 0);
		gbc_inductiveOntologyScroll.fill = GridBagConstraints.BOTH;
		gbc_inductiveOntologyScroll.gridx = 0;
		gbc_inductiveOntologyScroll.gridy = 0;
		inductiveInputPanel.add(inductiveOntologyScroll, gbc_inductiveOntologyScroll);

		final JList inductiveOntologyList = new JList(ontologyList.keySet().toArray());
		inductiveOntologyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		inductiveOntologyScroll.setViewportView(inductiveOntologyList);

		final DefaultListModel inductiveModel = new DefaultListModel();

		inductiveClassesScroll = new JScrollPane();
		inductiveClassesScroll.setBorder(new TitledBorder(null, "Classes", TitledBorder.LEADING, TitledBorder.TOP, titleFont, null));
		GridBagConstraints gbc_inductiveClassesScroll = new GridBagConstraints();
		gbc_inductiveClassesScroll.insets = new Insets(0, 0, 5, 0);
		gbc_inductiveClassesScroll.fill = GridBagConstraints.BOTH;
		gbc_inductiveClassesScroll.gridx = 0;
		gbc_inductiveClassesScroll.gridy = 1;
		inductiveInputPanel.add(inductiveClassesScroll, gbc_inductiveClassesScroll);
		final JList inductiveClassesList = new JList(inductiveModel);
		inductiveClassesList.setToolTipText("You can select class to show after EVALUATE");

		inductiveClassesListListener(inductiveClassesList);
		inductiveClassesList.setFocusable(false);
		inductiveClassesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		inductiveClassesScroll.setViewportView(inductiveClassesList);

		final JScrollPane inductiveMethodsScroll = new JScrollPane();
		inductiveMethodsScroll.setBorder(new TitledBorder(null, "Methods", TitledBorder.LEADING, TitledBorder.TOP, titleFont, null));
		GridBagConstraints gbc_inductiveMethodsScroll = new GridBagConstraints();
		gbc_inductiveMethodsScroll.insets = new Insets(0, 0, 5, 0);
		gbc_inductiveMethodsScroll.fill = GridBagConstraints.BOTH;
		gbc_inductiveMethodsScroll.gridx = 0;
		gbc_inductiveMethodsScroll.gridy = 2;
		inductiveInputPanel.add(inductiveMethodsScroll, gbc_inductiveMethodsScroll);

		final JList inductiveMethodsList = new JList();
		inductiveMethodsList.setEnabled(false);

		inductiveMethodsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		inductiveMethodsList.setListData(inductiveMethods.values());
		inductiveMethodsScroll.setViewportView(inductiveMethodsList);

		JPanel panel_1 = new JPanel();
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 3;
		inductiveInputPanel.add(panel_1, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[] { 65, 95, 38, 0, 0 };
		gbl_panel_1.rowHeights = new int[] { 0, 0, 20, 20, 0 };
		gbl_panel_1.columnWeights = new double[] { 1.0, 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_panel_1.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panel_1.setLayout(gbl_panel_1);

		JLabel lblTestSetSize = new JLabel("TRAINING SET SIZE");
		GridBagConstraints gbc_lblTestSetSize = new GridBagConstraints();
		gbc_lblTestSetSize.fill = GridBagConstraints.BOTH;
		gbc_lblTestSetSize.insets = new Insets(0, 0, 5, 5);
		gbc_lblTestSetSize.gridx = 0;
		gbc_lblTestSetSize.gridy = 0;
		panel_1.add(lblTestSetSize, gbc_lblTestSetSize);

		final JSlider inductiveSlider = new JSlider();

		inductiveSlider.setPaintLabels(true);
		inductiveSlider.setMinorTickSpacing(5);
		inductiveSlider.setToolTipText("Choose a value between 0 and 100");
		inductiveSlider.setSnapToTicks(false);
		inductiveSlider.setPaintTicks(true);
		inductiveSlider.setMajorTickSpacing(25);
		inductiveSlider.setMinorTickSpacing(5);
		inductiveSlider.setValue(85);

		GridBagConstraints gbc_slider = new GridBagConstraints();
		gbc_slider.fill = GridBagConstraints.BOTH;
		gbc_slider.insets = new Insets(0, 0, 5, 5);
		gbc_slider.gridx = 1;
		gbc_slider.gridy = 0;
		panel_1.add(inductiveSlider, gbc_slider);

		inductiveSliderValue = new JFormattedTextField(new Integer(85));
		inductiveSliderValue.setHorizontalAlignment(SwingConstants.CENTER);
		inductiveSliderValueListener(inductiveSlider);

		GridBagConstraints gbc_sliderValue = new GridBagConstraints();
		gbc_sliderValue.fill = GridBagConstraints.BOTH;
		gbc_sliderValue.insets = new Insets(0, 0, 5, 5);
		gbc_sliderValue.gridx = 2;
		gbc_sliderValue.gridy = 0;
		panel_1.add(inductiveSliderValue, gbc_sliderValue);
		inductiveSliderValue.setColumns(4);
		inductiveSliderValue.setText(String.valueOf(inductiveSlider.getValue()));

		inductiveSliderListener(inductiveSlider);
		inductiveSliderValueKeyListener(inductiveSlider);

		JLabel label = new JLabel("%");
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.insets = new Insets(0, 0, 5, 0);
		gbc_label.gridx = 3;
		gbc_label.gridy = 0;
		panel_1.add(label, gbc_label);

		final JButton inductiveBtnBuildCaseBase = new JButton("Build Case Base");
		inductiveBtnBuildCaseBase.setEnabled(false);

		GridBagConstraints gbc_btnBuildCaseBase = new GridBagConstraints();
		gbc_btnBuildCaseBase.fill = GridBagConstraints.BOTH;
		gbc_btnBuildCaseBase.gridwidth = 4;
		gbc_btnBuildCaseBase.insets = new Insets(0, 0, 5, 5);
		gbc_btnBuildCaseBase.gridx = 0;
		gbc_btnBuildCaseBase.gridy = 1;
		panel_1.add(inductiveBtnBuildCaseBase, gbc_btnBuildCaseBase);

		final JButton inductiveBtnLearn = new JButton("Learn");
		inductiveBtnLearn.setEnabled(false);

		GridBagConstraints gbc_btnLearn = new GridBagConstraints();
		gbc_btnLearn.gridwidth = 4;
		gbc_btnLearn.fill = GridBagConstraints.BOTH;
		gbc_btnLearn.insets = new Insets(0, 0, 5, 5);
		gbc_btnLearn.gridx = 0;
		gbc_btnLearn.gridy = 2;
		panel_1.add(inductiveBtnLearn, gbc_btnLearn);

		final JButton inductiveBtnEvaluate = new JButton("Evaluate");
		inductiveBtnEvaluateListener(inductiveBtnEvaluate);
		inductiveBtnEvaluate.setEnabled(false);

		GridBagConstraints gbc_btnEvaluate = new GridBagConstraints();
		gbc_btnEvaluate.insets = new Insets(0, 0, 0, 5);
		gbc_btnEvaluate.gridwidth = 4;
		gbc_btnEvaluate.fill = GridBagConstraints.BOTH;
		gbc_btnEvaluate.gridx = 0;
		gbc_btnEvaluate.gridy = 3;
		panel_1.add(inductiveBtnEvaluate, gbc_btnEvaluate);

		JSplitPane inductivePanel = new JSplitPane();
		inductivePanel.setOneTouchExpandable(true);
		inductivePanel.setPreferredSize(new Dimension(500, 33));
		inductivePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		inductivePanel.setResizeWeight(0.5);
		GridBagConstraints gbc_inductivePanel = new GridBagConstraints();
		gbc_inductivePanel.fill = GridBagConstraints.BOTH;
		gbc_inductivePanel.gridx = 1;
		gbc_inductivePanel.gridy = 0;
		Inductive.add(inductivePanel, gbc_inductivePanel);

		final JPanel testSetPanel = new JPanel();
		testSetPanel.setMaximumSize(new Dimension(1000, 1000));
		testSetPanel.setPreferredSize(new Dimension(450, 400));
		testSetPanel.setBorder(new TitledBorder(null, "Test Set", TitledBorder.LEADING, TitledBorder.TOP, titleFont, null));
		inductivePanel.setLeftComponent(testSetPanel);
		testSetPanel.setLayout(new BorderLayout(0, 0));

		classRules = new JPanel();
		inductivePanel.setRightComponent(classRules);
		GridBagLayout gbl_classRules = new GridBagLayout();
		gbl_classRules.columnWidths = new int[] { 472, 0 };
		gbl_classRules.rowHeights = new int[] { 298, 36, 0 };
		gbl_classRules.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_classRules.rowWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		classRules.setLayout(gbl_classRules);

		inductiveRules = new JPanel();
		inductiveRules.setMaximumSize(new Dimension(2000, 1000));
		inductiveRules.setToolTipText("Select the class you want to show");
		inductiveRules.setBorder(new TitledBorder(null, "Rules", TitledBorder.LEADING, TitledBorder.TOP, titleFont, null));
		GridBagConstraints gbc_inductiveRules = new GridBagConstraints();
		gbc_inductiveRules.insets = new Insets(0, 0, 5, 0);
		gbc_inductiveRules.fill = GridBagConstraints.BOTH;
		gbc_inductiveRules.gridx = 0;
		gbc_inductiveRules.gridy = 0;
		classRules.add(inductiveRules, gbc_inductiveRules);
		inductiveRules.setLayout(new BorderLayout(0, 0));

		inductiveControlPanel = new JPanel();
		inductiveControlPanel.setEnabled(false);
		GridBagConstraints gbc_inductiveControlPanel = new GridBagConstraints();
		gbc_inductiveControlPanel.fill = GridBagConstraints.BOTH;
		gbc_inductiveControlPanel.gridx = 0;
		gbc_inductiveControlPanel.gridy = 1;
		classRules.add(inductiveControlPanel, gbc_inductiveControlPanel);
		inductiveControlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JLabel lblSolutions = new JLabel("Rule");
		inductiveControlPanel.add(lblSolutions);

		ruleIndex = new JFormattedTextField(new Integer(0));
		ruleIndex.setEnabled(false);
		ruleIndex.setEditable(false);
		inductiveControlPanel.add(ruleIndex);
		ruleIndex.setColumns(10);

		ruleTotal = new JFormattedTextField(new Integer(0));
		ruleTotal.setEnabled(false);
		ruleTotal.setEditable(false);
		inductiveControlPanel.add(ruleTotal);
		ruleTotal.setColumns(10);

		rulePrevious = new JButton("Previous");
		rulePrevious.setEnabled(false);
		inductiveRulePreviousListener();
		inductiveControlPanel.add(rulePrevious);

		ruleNext = new JButton("Next");
		ruleNext.setEnabled(false);
		inductiveRuleNextListener();
		inductiveControlPanel.add(ruleNext);

		inductiveFunctions(inductiveOntologyList, inductiveModel, inductiveClassesList, inductiveMethodsScroll, inductiveMethodsList,
				inductiveSlider, inductiveBtnBuildCaseBase, inductiveBtnLearn, inductiveBtnEvaluate, testSetPanel);
	}

	/**
	 * Creates the lazy panel.
	 * 
	 * @param tabbedPane
	 *            the tabbed pane
	 */
	private void createPanelLazy(final JTabbedPane tabbedPane) {
		JPanel Lazy = new JPanel();
		tabbedPane.addTab("Lazy Methods", null, Lazy, null);
		GridBagLayout gbl_Lazy = new GridBagLayout();
		gbl_Lazy.columnWidths = new int[] { 229, 1087, 0 };
		gbl_Lazy.rowHeights = new int[] { 499, 0 };
		gbl_Lazy.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_Lazy.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		Lazy.setLayout(gbl_Lazy);

		JPanel lazyInputPanel = new JPanel();
		lazyInputPanel.setMaximumSize(new Dimension(385, 1200));
		GridBagConstraints gbc_lazyInputPanel = new GridBagConstraints();
		gbc_lazyInputPanel.fill = GridBagConstraints.BOTH;
		gbc_lazyInputPanel.insets = new Insets(0, 0, 0, 5);
		gbc_lazyInputPanel.gridx = 0;
		gbc_lazyInputPanel.gridy = 0;
		Lazy.add(lazyInputPanel, gbc_lazyInputPanel);
		GridBagLayout gbl_lazyInputPanel = new GridBagLayout();
		gbl_lazyInputPanel.columnWidths = new int[] { 250, 0 };
		gbl_lazyInputPanel.rowHeights = new int[] { 293, 106, 110, 74, 109, 0 };
		gbl_lazyInputPanel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_lazyInputPanel.rowWeights = new double[] { 1.0, 1.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		lazyInputPanel.setLayout(gbl_lazyInputPanel);

		JScrollPane lazyOntologyScroll = new JScrollPane();
		lazyOntologyScroll.setBorder(new TitledBorder(null, "Ontology", TitledBorder.LEADING, TitledBorder.TOP, titleFont, null));
		GridBagConstraints gbc_lazyOntologyScroll = new GridBagConstraints();
		gbc_lazyOntologyScroll.fill = GridBagConstraints.BOTH;
		gbc_lazyOntologyScroll.insets = new Insets(0, 0, 5, 0);
		gbc_lazyOntologyScroll.gridx = 0;
		gbc_lazyOntologyScroll.gridy = 0;
		lazyInputPanel.add(lazyOntologyScroll, gbc_lazyOntologyScroll);

		final JList lazyOntologyList = new JList(ontologyList.keySet().toArray());
		lazyOntologyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		lazyOntologyScroll.setViewportView(lazyOntologyList);

		final DefaultListModel lazyModel = new DefaultListModel();

		JScrollPane lazyClassesScroll = new JScrollPane();
		lazyClassesScroll.setBorder(new TitledBorder(null, "Classes", TitledBorder.LEADING, TitledBorder.TOP, titleFont, null));
		GridBagConstraints gbc_lazyClassesScroll = new GridBagConstraints();
		gbc_lazyClassesScroll.fill = GridBagConstraints.BOTH;
		gbc_lazyClassesScroll.insets = new Insets(0, 0, 5, 0);
		gbc_lazyClassesScroll.gridx = 0;
		gbc_lazyClassesScroll.gridy = 1;
		lazyInputPanel.add(lazyClassesScroll, gbc_lazyClassesScroll);
		final JList lazyClassesList = new JList(lazyModel);
		lazyClassesScroll.setViewportView(lazyClassesList);

		btnLazyEvaluate = new JButton("Evaluate");
		btnLazyEvaluateListener();

		JScrollPane lazyMethodsScroll = new JScrollPane();
		lazyMethodsScroll.setEnabled(false);
		lazyMethodsScroll.setBorder(new TitledBorder(null, "Methods", TitledBorder.LEADING, TitledBorder.TOP, titleFont, null));
		GridBagConstraints gbc_lazyMethodsScroll = new GridBagConstraints();
		gbc_lazyMethodsScroll.fill = GridBagConstraints.BOTH;
		gbc_lazyMethodsScroll.insets = new Insets(0, 0, 5, 0);
		gbc_lazyMethodsScroll.gridx = 0;
		gbc_lazyMethodsScroll.gridy = 2;
		lazyInputPanel.add(lazyMethodsScroll, gbc_lazyMethodsScroll);

		final JList lazyMethodsList = new JList();
		lazyMethodsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lMethodsList = lazyMethodsList;
		lazyMethodsList.setListData(lazyMethods.values());
		lazyMethodsScroll.setViewportView(lazyMethodsList);
		lazyMethodsList.setSelectedIndex(0);

		JPanel controlPanel = new JPanel();
		GridBagConstraints gbc_controlPanel = new GridBagConstraints();
		gbc_controlPanel.insets = new Insets(0, 0, 5, 0);
		gbc_controlPanel.fill = GridBagConstraints.BOTH;
		gbc_controlPanel.gridx = 0;
		gbc_controlPanel.gridy = 3;
		lazyInputPanel.add(controlPanel, gbc_controlPanel);
		GridBagLayout gbl_controlPanel = new GridBagLayout();
		gbl_controlPanel.columnWidths = new int[] { 101, 124, 39, 0, 0 };
		gbl_controlPanel.rowHeights = new int[] { 0, 0, 0 };
		gbl_controlPanel.columnWeights = new double[] { 1.0, 1.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_controlPanel.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		controlPanel.setLayout(gbl_controlPanel);

		JLabel label = new JLabel("TRAINING SET SIZE");
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.fill = GridBagConstraints.HORIZONTAL;
		gbc_label.insets = new Insets(0, 0, 5, 5);
		gbc_label.gridx = 0;
		gbc_label.gridy = 0;
		controlPanel.add(label, gbc_label);

		final JSlider lazySlider = new JSlider();

		lazySlider.setValue(85);
		lazySlider.setToolTipText("Choose a value between 0 and 100");
		lazySlider.setSnapToTicks(false);
		lazySlider.setPaintTicks(true);
		lazySlider.setPaintLabels(true);
		lazySlider.setMinorTickSpacing(5);
		lazySlider.setMajorTickSpacing(25);
		GridBagConstraints gbc_lazySlider = new GridBagConstraints();
		gbc_lazySlider.fill = GridBagConstraints.HORIZONTAL;
		gbc_lazySlider.insets = new Insets(0, 0, 5, 5);
		gbc_lazySlider.gridx = 1;
		gbc_lazySlider.gridy = 0;
		controlPanel.add(lazySlider, gbc_lazySlider);

		lazySliderValue = new JTextField();
		lazySliderValue.setHorizontalAlignment(SwingConstants.CENTER);
		lazySliderValueListener(lazySlider);

		lazySliderValue.setText("85");
		lazySliderValue.setColumns(10);
		GridBagConstraints gbc_lazySliderValue = new GridBagConstraints();
		gbc_lazySliderValue.fill = GridBagConstraints.HORIZONTAL;
		gbc_lazySliderValue.insets = new Insets(0, 0, 5, 5);
		gbc_lazySliderValue.gridx = 2;
		gbc_lazySliderValue.gridy = 0;
		controlPanel.add(lazySliderValue, gbc_lazySliderValue);

		lazySliderListener(lazySlider);

		JLabel label_1 = new JLabel("%");
		GridBagConstraints gbc_label_1 = new GridBagConstraints();
		gbc_label_1.insets = new Insets(0, 0, 5, 0);
		gbc_label_1.gridx = 3;
		gbc_label_1.gridy = 0;
		controlPanel.add(label_1, gbc_label_1);

		final JButton lazyBtnBuildCaseBase = new JButton("Build Case Base");

		lazyBtnBuildCaseBase.setEnabled(false);
		GridBagConstraints gbc_lazyBtnBuildCaseBase = new GridBagConstraints();
		gbc_lazyBtnBuildCaseBase.fill = GridBagConstraints.HORIZONTAL;
		gbc_lazyBtnBuildCaseBase.gridwidth = 4;
		gbc_lazyBtnBuildCaseBase.gridx = 0;
		gbc_lazyBtnBuildCaseBase.gridy = 1;
		controlPanel.add(lazyBtnBuildCaseBase, gbc_lazyBtnBuildCaseBase);

		final JPanel lazyMethodsSettings = new JPanel();
		lazyMethodsSettings.setEnabled(false);
		lazyMethodsSettings.setBorder(new TitledBorder(null, "Method Settings", TitledBorder.LEADING, TitledBorder.TOP, titleFont, null));
		GridBagConstraints gbc_lazyMethodsSettings = new GridBagConstraints();
		gbc_lazyMethodsSettings.fill = GridBagConstraints.BOTH;
		gbc_lazyMethodsSettings.gridx = 0;
		gbc_lazyMethodsSettings.gridy = 4;
		lazyInputPanel.add(lazyMethodsSettings, gbc_lazyMethodsSettings);
		lazyMethodsSettings.setLayout(new CardLayout(0, 0));

		final JPanel lazySettingsKNN = new JPanel();
		lazySettingsKNN.setName("settingsKNN");
		lazyMethodsSettings.add(lazySettingsKNN, "settingsKNN");
		GridBagLayout gbl_lazySettingsKNN = new GridBagLayout();
		gbl_lazySettingsKNN.columnWidths = new int[] { 0, 163, 0 };
		gbl_lazySettingsKNN.rowHeights = new int[] { 0, 0, 0 };
		gbl_lazySettingsKNN.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_lazySettingsKNN.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		lazySettingsKNN.setLayout(gbl_lazySettingsKNN);

		JLabel lblK = new JLabel("K");
		GridBagConstraints gbc_lblK = new GridBagConstraints();
		gbc_lblK.fill = GridBagConstraints.VERTICAL;
		gbc_lblK.insets = new Insets(0, 0, 5, 5);
		gbc_lblK.gridx = 0;
		gbc_lblK.gridy = 0;
		lazySettingsKNN.add(lblK, gbc_lblK);

		kValue = new JTextField();
		kValue.setEnabled(false);
		kValueListener();
		kValue.setText("1");
		GridBagConstraints gbc_kValue = new GridBagConstraints();
		gbc_kValue.insets = new Insets(0, 0, 5, 0);
		gbc_kValue.fill = GridBagConstraints.VERTICAL;
		gbc_kValue.gridx = 1;
		gbc_kValue.gridy = 0;
		lazySettingsKNN.add(kValue, gbc_kValue);
		kValue.setColumns(10);

		JLabel lblDistance = new JLabel("Distance");
		GridBagConstraints gbc_lblDistance = new GridBagConstraints();
		gbc_lblDistance.fill = GridBagConstraints.VERTICAL;
		gbc_lblDistance.insets = new Insets(0, 0, 0, 5);
		gbc_lblDistance.gridx = 0;
		gbc_lblDistance.gridy = 1;
		lazySettingsKNN.add(lblDistance, gbc_lblDistance);

		distanceComboBox = new JComboBox(distanceMethods.values());
		GridBagConstraints gbc_distanceComboBox = new GridBagConstraints();
		gbc_distanceComboBox.fill = GridBagConstraints.BOTH;
		gbc_distanceComboBox.gridx = 1;
		gbc_distanceComboBox.gridy = 1;
		lazySettingsKNN.add(distanceComboBox, gbc_distanceComboBox);

		final JPanel lazySettingsLID = new JPanel();
		lazySettingsLID.setName("settingsLID");
		lazyMethodsSettings.add(lazySettingsLID, "settingsLID");
		GridBagLayout gbl_lazySettingsLID = new GridBagLayout();
		gbl_lazySettingsLID.columnWidths = new int[] { 0, 0, 151, 0 };
		gbl_lazySettingsLID.rowHeights = new int[] { 0, 0, 0, 0 };
		gbl_lazySettingsLID.columnWeights = new double[] { 0.0, 0.0, 1.0, Double.MIN_VALUE };
		gbl_lazySettingsLID.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		lazySettingsLID.setLayout(gbl_lazySettingsLID);

		JLabel lblHeuristic = new JLabel("Heuristic");
		GridBagConstraints gbc_lblHeuristic = new GridBagConstraints();
		gbc_lblHeuristic.insets = new Insets(0, 0, 5, 5);
		gbc_lblHeuristic.anchor = GridBagConstraints.EAST;
		gbc_lblHeuristic.gridx = 0;
		gbc_lblHeuristic.gridy = 0;
		lazySettingsLID.add(lblHeuristic, gbc_lblHeuristic);

		lazyHeuristic = new JComboBox(heuristics.values());
		GridBagConstraints gbc_lazyHeuristic = new GridBagConstraints();
		gbc_lazyHeuristic.gridwidth = 2;
		gbc_lazyHeuristic.insets = new Insets(0, 0, 5, 0);
		gbc_lazyHeuristic.fill = GridBagConstraints.HORIZONTAL;
		gbc_lazyHeuristic.gridx = 1;
		gbc_lazyHeuristic.gridy = 0;
		lazySettingsLID.add(lazyHeuristic, gbc_lazyHeuristic);

		JLabel lblSelectionMode = new JLabel("Selection Mode");
		GridBagConstraints gbc_lblSelectionMode = new GridBagConstraints();
		gbc_lblSelectionMode.insets = new Insets(0, 0, 5, 5);
		gbc_lblSelectionMode.anchor = GridBagConstraints.EAST;
		gbc_lblSelectionMode.gridx = 0;
		gbc_lblSelectionMode.gridy = 1;
		lazySettingsLID.add(lblSelectionMode, gbc_lblSelectionMode);

		lazySelection = new JComboBox(selectionMode.values());
		GridBagConstraints gbc_lazySelection = new GridBagConstraints();
		gbc_lazySelection.gridwidth = 2;
		gbc_lazySelection.insets = new Insets(0, 0, 5, 0);
		gbc_lazySelection.fill = GridBagConstraints.HORIZONTAL;
		gbc_lazySelection.gridx = 1;
		gbc_lazySelection.gridy = 1;
		lazySettingsLID.add(lazySelection, gbc_lazySelection);

		final JLabel lblMinPrecedents = new JLabel("Min Precedents");
		GridBagConstraints gbc_lblMinPrecedents = new GridBagConstraints();
		gbc_lblMinPrecedents.insets = new Insets(0, 0, 0, 5);
		gbc_lblMinPrecedents.anchor = GridBagConstraints.EAST;
		gbc_lblMinPrecedents.gridx = 0;
		gbc_lblMinPrecedents.gridy = 2;
		lazySettingsLID.add(lblMinPrecedents, gbc_lblMinPrecedents);

		lazyMinPrecedents = new JTextField();
		lazyMinPrecedents.setText("1");
		GridBagConstraints gbc_lazyMinPrecedents = new GridBagConstraints();
		gbc_lazyMinPrecedents.insets = new Insets(0, 0, 0, 5);
		gbc_lazyMinPrecedents.gridx = 1;
		gbc_lazyMinPrecedents.gridy = 2;
		lazySettingsLID.add(lazyMinPrecedents, gbc_lazyMinPrecedents);
		lazyMinPrecedents.setColumns(4);

		lazyMinPrecedentsListener();
		lazyChckBoxGeneralize = new JCheckBox("Generalize");
		GridBagConstraints gbc_lazyChckBoxGeneralize = new GridBagConstraints();
		gbc_lazyChckBoxGeneralize.gridx = 2;
		gbc_lazyChckBoxGeneralize.gridy = 2;
		lazySettingsLID.add(lazyChckBoxGeneralize, gbc_lazyChckBoxGeneralize);
		GridBagConstraints gbc_btnLazyEvaluate_1;

		final JSplitPane splitPane_4 = new JSplitPane();
		splitPane_4.setMaximumSize(new Dimension(2560, 1200));
		splitPane_4.setResizeWeight(0.5);
		GridBagConstraints gbc_splitPane_4 = new GridBagConstraints();
		gbc_splitPane_4.fill = GridBagConstraints.BOTH;
		gbc_splitPane_4.gridx = 1;
		gbc_splitPane_4.gridy = 0;
		Lazy.add(splitPane_4, gbc_splitPane_4);

		lazyTestSetPanel = new JPanel();
		lazyTestSetPanel.setMaximumSize(new Dimension(1000, 1000));
		lazyTestSetPanel.setToolTipText("Select case to classify");
		lazyTestSetPanel.setBorder(new TitledBorder(null, "Test Set", TitledBorder.LEADING, TitledBorder.TOP, titleFont, null));
		splitPane_4.setLeftComponent(lazyTestSetPanel);
		lazyTestSetPanel.setLayout(new BorderLayout(0, 0));

		lazyClassifyPanel = new JPanel();
		lazyClassifyPanel.setMaximumSize(new Dimension(2000, 1000));
		lazyClassifyPanel.setBorder(new TitledBorder(null, "Classify", TitledBorder.LEADING, TitledBorder.TOP, titleFont, null));
		splitPane_4.setRightComponent(lazyClassifyPanel);
		lazyClassifyPanel.setLayout(new BorderLayout(0, 0));

		lazyBuildCaseListener(lazyOntologyList, lazySlider, lazyBtnBuildCaseBase, lazyMethodsList, splitPane_4);

		lazyOntologyListListener(lazyOntologyList, lazyModel, lazyClassesList, lazyBtnBuildCaseBase, lazyMethodsList);

		lazyMethodsListListener(lazyOntologyList, lazyBtnBuildCaseBase, lazyMethodsList, lazyMethodsSettings, lblMinPrecedents);
	}

	/**
	 * Creates the panel more.
	 * 
	 * @param tabbedPane
	 *            the tabbed pane
	 */
	private void createPanelMore(final JTabbedPane tabbedPane) {
		JPanel More = new JPanel();
		tabbedPane.addTab("A-MAIL", null, More, null);
		GridBagLayout gbl_More = new GridBagLayout();
		gbl_More.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_More.rowHeights = new int[] { 0, 0, 0 };
		gbl_More.columnWeights = new double[] { 1.0, 0.0, 1.0, Double.MIN_VALUE };
		gbl_More.rowWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
		More.setLayout(gbl_More);
		
		JTextPane txtpnTextoEjemplo = new JTextPane();
		txtpnTextoEjemplo.setContentType("text/html");
		txtpnTextoEjemplo.setEditable(false);
		String fontfamily = txtpnTextoEjemplo.getFont().getFamily();
		
		txtpnTextoEjemplo.setText("<body style=\"font-family: Lucida Grande\"<br />\n<center><b>Argumentation-based Multi-Agent Inductive Learning</b></center>\n\n <br /><br /> <b>A-MAIL</b> (Argumentation-based Multi-Agent Inductive Learning) is platform that integrates induction and argumentation (both using FTerms) <br />with the goal of performing Coordinated Inductive Learning (CIL). <br />\n\nAgents are able to learn from individual experience first, using inductive rule learning, and later they can communicate with each other using argumentation. <br />This process is used to criticize the rules of other agents and also to learn from communication. <br />This precess ends when an agreement is reached on the rules describing a concept by each individual (thus coordinating in the group their individual inductive learning).\n\n<br />This panel launches an argumentation process between to agents, so it may take some time, <br />and when finishes it visualizes the steps of argumentation and learning from communication performed by those agents.");
		txtpnTextoEjemplo.setBackground(SystemColor.window);
		GridBagConstraints gbc_txtpnTextoEjemplo = new GridBagConstraints();
		gbc_txtpnTextoEjemplo.insets = new Insets(0, 0, 5, 5);
		gbc_txtpnTextoEjemplo.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtpnTextoEjemplo.gridx = 1;
		gbc_txtpnTextoEjemplo.gridy = 0;
		More.add(txtpnTextoEjemplo, gbc_txtpnTextoEjemplo);

		JButton btnNewButton = new JButton("Launch A-MAIL Visualization");
		launchAmail(btnNewButton);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.anchor = GridBagConstraints.NORTH;
		gbc_btnNewButton.insets = new Insets(0, 0, 0, 5);
		gbc_btnNewButton.gridx = 1;
		gbc_btnNewButton.gridy = 1;
		More.add(btnNewButton, gbc_btnNewButton);
	}

	/**
	 * Creates the operations panel.
	 *
	 * @param tabbedPane the tabbed pane
	 * @throws FeatureTermException the feature term exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void createPanelOperations(final JTabbedPane tabbedPane) throws FeatureTermException, IOException {

		final String[] selectionBasics = { "Subsumption", "Unification", "Antiunification", "Disintegration", "Amalgam" };
		JPanel Operation = new JPanel();
		tabbedPane.addTab("Operations", null, Operation, null);
		GridBagLayout gbl_Operation = new GridBagLayout();
		gbl_Operation.columnWidths = new int[] { 290, 410, 0 };
		gbl_Operation.rowHeights = new int[] { 559, 0 };
		gbl_Operation.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_Operation.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		Operation.setLayout(gbl_Operation);

		final JPanel operationsLeftPanel = new JPanel();
		operationsLeftPanel.setBorder(null);
		GridBagConstraints gbc_operationsLeftPanel = new GridBagConstraints();
		gbc_operationsLeftPanel.fill = GridBagConstraints.BOTH;
		gbc_operationsLeftPanel.insets = new Insets(0, 0, 0, 5);
		gbc_operationsLeftPanel.gridx = 0;
		gbc_operationsLeftPanel.gridy = 0;
		Operation.add(operationsLeftPanel, gbc_operationsLeftPanel);
		GridBagLayout gbl_operationsLeftPanel = new GridBagLayout();
		gbl_operationsLeftPanel.columnWidths = new int[] { 179, 0 };
		gbl_operationsLeftPanel.rowHeights = new int[] { 167, 159, 339, 0, 0 };
		gbl_operationsLeftPanel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_operationsLeftPanel.rowWeights = new double[] { 0.0, 1.0, 1.0, 0.0, Double.MIN_VALUE };
		operationsLeftPanel.setLayout(gbl_operationsLeftPanel);

		final JList domainListOperations = new JList();
		domainListOperations.setListData(selectionBasics);
		domainListOperations.setBorder(new TitledBorder(null, "Avaliable Operations", TitledBorder.LEADING, TitledBorder.TOP, titleFont,
				null));
		domainListOperations.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		domainListOperations.setFont(new Font("Lucida Grande", Font.PLAIN, 14));
		domainListOperations.setSelectedIndex(0);

		GridBagConstraints gbc_domainListOperations = new GridBagConstraints();
		gbc_domainListOperations.fill = GridBagConstraints.BOTH;
		gbc_domainListOperations.insets = new Insets(0, 0, 5, 0);
		gbc_domainListOperations.gridx = 0;
		gbc_domainListOperations.gridy = 0;
		operationsLeftPanel.add(domainListOperations, gbc_domainListOperations);

		final DemoFileList dmList = DemoTest.getDemos("Resources/Demos/subsumption.xml");
		// �rbol demos

		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Demos");
		DefaultTreeModel modelo = new DefaultTreeModel(root);
		DefaultMutableTreeNode currentDomain;
		DefaultMutableTreeNode currentNode;

		// Crate demo tree
		HashSet<String> domainList = new HashSet<String>();

		for (Demo element : dmList.demoList) {
			currentDomain = new DefaultMutableTreeNode(element.domain);
			if (!domainList.contains(element.domain)) {
				modelo.insertNodeInto(currentDomain, root, 0);
				domainList.add(element.domain);
			}
			currentNode = new DefaultMutableTreeNode(Long.toString(element.nid));
			modelo.insertNodeInto(currentNode, currentDomain, 0);
		}

		operationsDatasetTest = new JScrollPane();
		operationsDatasetTest.setBorder(new TitledBorder(null, "Preloaded Demos", TitledBorder.LEADING, TitledBorder.TOP, titleFont, null));
		GridBagConstraints gbc_operationsDatasetTest = new GridBagConstraints();
		gbc_operationsDatasetTest.insets = new Insets(0, 0, 5, 0);
		gbc_operationsDatasetTest.fill = GridBagConstraints.BOTH;
		gbc_operationsDatasetTest.gridx = 0;
		gbc_operationsDatasetTest.gridy = 1;
		operationsLeftPanel.add(operationsDatasetTest, gbc_operationsDatasetTest);

		treeTests = new JTree(modelo);
		treeListener(domainListOperations, dmList);

		operationsDatasetTest.setViewportView(treeTests);
		operationsReloadTree(dmList, operationsLeftPanel, domainListOperations);

		JScrollPane operationsDatasetDemos = new JScrollPane();
		operationsDatasetDemos.setBorder(new TitledBorder(null, "Dataset", TitledBorder.LEADING, TitledBorder.TOP, titleFont, null));
		GridBagConstraints gbc_operationsDatasetDemos = new GridBagConstraints();
		gbc_operationsDatasetDemos.insets = new Insets(0, 0, 5, 0);
		gbc_operationsDatasetDemos.fill = GridBagConstraints.BOTH;
		gbc_operationsDatasetDemos.gridx = 0;
		gbc_operationsDatasetDemos.gridy = 2;
		operationsLeftPanel.add(operationsDatasetDemos, gbc_operationsDatasetDemos);
		operationsRunBtn = new JButton("Apply Operation");

		// Create SPONGE and TRAINS demo tree "TRAINS_DATASET""DEMOSPONGIAE_120_DATASET"
		operationsDemosTree(operationsDatasetDemos, domainListOperations);

		GridBagConstraints gbc_operationsRunBtn = new GridBagConstraints();
		gbc_operationsRunBtn.gridx = 0;
		gbc_operationsRunBtn.gridy = 3;
		operationsLeftPanel.add(operationsRunBtn, gbc_operationsRunBtn);

		JSplitPane splitPane_2 = new JSplitPane();
		splitPane_2.setResizeWeight(0.6);
		GridBagConstraints gbc_splitPane_2 = new GridBagConstraints();
		gbc_splitPane_2.fill = GridBagConstraints.BOTH;
		gbc_splitPane_2.gridx = 1;
		gbc_splitPane_2.gridy = 0;
		Operation.add(splitPane_2, gbc_splitPane_2);

		panelsOpTab = new JPanel();
		panelsOpTab.setBorder(null);
		splitPane_2.setLeftComponent(panelsOpTab);
		CardLayout cardLayout = new CardLayout(10, 10);
		panelsOpTab.setLayout(cardLayout);

		final JSplitPane splitPane2FT = new JSplitPane();
		splitPane2FT.setBorder(null);
		splitPane2FT.setResizeWeight(0.5);
		splitPane2FT.setOrientation(JSplitPane.VERTICAL_SPLIT);
		panelsOpTab.add(splitPane2FT, "name_1341564264366935000");

		panelDemoFT1 = new JPanel();
		panelDemoFT1.setBorder(new TitledBorder(null, "Feature Term 1", TitledBorder.LEADING, TitledBorder.TOP, titleFont, null));
		splitPane2FT.setLeftComponent(panelDemoFT1);
		panelDemoFT1.setLayout(new BorderLayout());

		panelDemoFT2 = new JPanel();
		panelDemoFT2.setBorder(new TitledBorder(null, "Feature Term 2", TitledBorder.LEADING, TitledBorder.TOP, titleFont, null));
		splitPane2FT.setRightComponent(panelDemoFT2);
		panelDemoFT2.setLayout(new BorderLayout());

		splitPane1FT = new JPanel();
		splitPane1FT.setBorder(new TitledBorder(null, "Feature Term", TitledBorder.LEADING, TitledBorder.TOP, titleFont, null));
		panelsOpTab.add(splitPane1FT, "name_1341564264396999000");
		splitPane1FT.setLayout(new BorderLayout(0, 0));

		final JPanel resultPanel = new JPanel();
		resultPanel.setBorder(new TitledBorder(null, "Result", TitledBorder.LEADING, TitledBorder.TOP, titleFont, null));
		splitPane_2.setRightComponent(resultPanel);
		resultPanel.setLayout(new BorderLayout(0, 0));

		operationsPanelFTResult = new JPanel();
		resultPanel.add(operationsPanelFTResult, BorderLayout.CENTER);
		operationsPanelFTResult.setLayout(new BorderLayout(0, 0));

		JPanel panelFTResultControls = new JPanel();
		resultPanel.add(panelFTResultControls, BorderLayout.SOUTH);

		JLabel lblresults = new JLabel("Result");
		panelFTResultControls.add(lblresults);

		resultCount = new JTextField();
		resultCount.setEditable(false);
		panelFTResultControls.add(resultCount);
		resultCount.setColumns(10);

		btnPrevious = new JButton("Previous");

		btnPrevious.setEnabled(false);
		panelFTResultControls.add(btnPrevious);

		btnNext = new JButton("Next");
		btnNext.setEnabled(false);
		btnNextListener();
		panelFTResultControls.add(btnNext);

		btnPreviousListener();

		// Operations RUN
		operationsRunBtnListener(domainListOperations, operationsRunBtn);
		domainListListener(operationsLeftPanel, domainListOperations, splitPane2FT);
	}

	/**
	 * Operations demos tree.
	 *
	 * @param operationsDatasetDemos the operations dataset demos
	 * @param domainListOperations the domain list operations
	 * @throws FeatureTermException the feature term exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void operationsDemosTree(JScrollPane operationsDatasetDemos, final JList domainListOperations) throws FeatureTermException,
			IOException {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Demos");
		DefaultTreeModel modelo = new DefaultTreeModel(root);
		treeDemos = new JTree(modelo);

		treeDemos.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				btnNext.setEnabled(false);
				btnPrevious.setEnabled(false);
				resultCount.setText("");
				TreePath path = treeDemos.getSelectionPath();
				String ftSelected = "";

				if (path != null && path.getPath().length == 3) {
					ftSelected = path.getLastPathComponent().toString();
					operationsEmptyPanels();

					String selected = (String) domainListOperations.getSelectedValue();
					if (!selected.equals("Disintegration")) {

						CardLayout cl = (CardLayout) panelsOpTab.getLayout();
						cl.first(panelsOpTab);
						if (panelDemoFT1.getComponents().length != 0 && actualTs == path.getPathComponent(1).toString()) { // If
																															// there
																															// is
																															// already
																															// a
																															// FT
																															// in
																															// Panel
																															// 1
																															// and
																															// is
																															// not
																															// the
																															// same
							try {
								cDemo = null;
								operationsDemoFT2 = ts.getCaseByName(ftSelected);
								System.out.println(operationsDemoFT1.getName());
								if (!(operationsDemoFT1.getName().equals(operationsDemoFT2.getName()))) {
									loadFT(ftSelected, panelDemoFT2);
									operationsRunBtn.setEnabled(true);
								}

							} catch (FeatureTermException e) {
								log.error("Error: " + e);
								e.printStackTrace();
							}
						} else { // If this is the first panel to add
							try {
								operationsEmptyPanels();
								cDemo = null;
								actualTs = path.getPathComponent(1).toString();
								try {
									resetOntology();
									// System.out.println("TS cargado: " + path.getPathComponent(1).toString());
									ts = TrainingSetUtils.loadTrainingSet(ontologyList.get(path.getPathComponent(1).toString()), o, dm,
											case_base);
								} catch (FeatureTermException e1) {
									log.error("Error: " + e1);
									e1.printStackTrace();
								} catch (IOException e1) {
									log.error("Error: " + e1);
									e1.printStackTrace();
								}
								loadFT(ftSelected, panelDemoFT1);
								operationsDemoFT1 = ts.getCaseByName(ftSelected);
								operationsRunBtn.setEnabled(false);
							} catch (FeatureTermException e) {
								log.error("Error: " + e);
								e.printStackTrace();
							}
						}
					} else { // disintegration
						try {
							try {
								resetOntology();
								// System.out.println("TS cargado: " + path.getPathComponent(1).toString());
								ts = TrainingSetUtils.loadTrainingSet(ontologyList.get(path.getPathComponent(1).toString()), o, dm,
										case_base);
							} catch (FeatureTermException e1) {
								log.error("Error: " + e1);
								e1.printStackTrace();
							} catch (IOException e1) {
								log.error("Error: " + e1);
								e1.printStackTrace();
							}
							cDemo = null;
							dm = new FTKBase();
							o = new Ontology();
							operationsPanelFTResult.removeAll();
							operationsPanelFTResult.repaint();
							resetOntology();

							CardLayout cl = (CardLayout) panelsOpTab.getLayout();
							cl.last(panelsOpTab);
							ts = null;
							ts = TrainingSetUtils.loadTrainingSet(ontologyList.get(path.getPathComponent(1).toString()), o, dm, case_base);
							FeatureTerm c = ts.getCaseByName(ftSelected);
							loadFT(c, splitPane1FT);
							operationsRunBtn.setEnabled(true);
						} catch (FeatureTermException e) {
							log.error("Error: " + e);
							e.printStackTrace();
						} catch (IOException e) {
							log.error("Error: " + e);
							e.printStackTrace();
						}
					}
				} else {
					operationsEmptyPanels();
				}
			}

			/**
			 * 
			 */
			private void operationsEmptyPanels() {
				// Vaciar FT
				if (panelDemoFT1.getComponents().length != 0 && panelDemoFT2.getComponents().length != 0) {
					try {
						panelDemoFT1.removeAll();
						panelDemoFT1.repaint();
						panelDemoFT2.removeAll();
						panelDemoFT2.repaint();
						operationsPanelFTResult.removeAll();
						operationsPanelFTResult.repaint();
						resetOntology();
					} catch (FeatureTermException e) {
						log.error("Error: " + e);
						e.printStackTrace();
					}

				}
			}
		});
		operationsDatasetDemos.setViewportView(treeDemos);

		dm = new FTKBase();
		resetOntology();

		String tsFile = "DEMOSPONGIAE_120_DATASET";
		ts = TrainingSetUtils.loadTrainingSet(ontologyList.get(tsFile), o, dm, case_base);

		DefaultMutableTreeNode father = new DefaultMutableTreeNode(tsFile);
		modelo.insertNodeInto(father, root, root.getChildCount());
		DefaultMutableTreeNode currentNode = new DefaultMutableTreeNode();

		List<FeatureTerm> ftList = ts.cases;
		Collections.sort(ftList, new FTNameComparator());

		for (FeatureTerm ft : ts.cases) {
			currentNode = new DefaultMutableTreeNode(ft.getName());
			father.add(currentNode);
			modelo.insertNodeInto(currentNode, father, father.getChildCount() - 1);
		}

		tsFile = "TRAINS_DATASET";
		ts = TrainingSetUtils.loadTrainingSet(ontologyList.get(tsFile), o, dm, case_base);
		father = new DefaultMutableTreeNode(tsFile);
		root.add(father);
		ftList = ts.cases;
		Collections.sort(ftList, new FTNameComparator());

		for (FeatureTerm ft : ts.cases) {
			currentNode = new DefaultMutableTreeNode(ft.getName());
			father.add(currentNode);
			modelo.insertNodeInto(currentNode, father, father.getChildCount() - 1);
		}

		treeDemos.expandRow(0);

	}

	/**
	 * Domain list listener.
	 * 
	 * @param panel_5
	 *            the panel_5
	 * @param domainListOperations
	 *            the domain list operations
	 * @param splitPane2FT
	 *            the split pane2 ft
	 */
	private void domainListListener(final JPanel panel_5, final JList domainListOperations, final JSplitPane splitPane2FT) {
		domainListOperations.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				String selected = (String) domainListOperations.getSelectedValue();
				CardLayout cl = (CardLayout) (panelsOpTab.getLayout());

				resetResultPanel();
				dm = new FTKBase();
				try {
					resetOntology();
				} catch (FeatureTermException e) {
					log.error("Error: " + e);
					e.printStackTrace();
				}
				// writeConsole("Operation " + selected);

				treeDemos.setEnabled(true);
				if (selected.equals("Subsumption")) {
					// load 2 ft
					cl.show(panelsOpTab, splitPane2FT.getName());
					DemoFileList cList = DemoTest.getDemos("Resources/Demos/subsumption.xml");
					operationsReloadTree(cList, panel_5, domainListOperations);
				}
				if (selected.equals("Unification")) {
					// load 2ft
					cl.show(panelsOpTab, splitPane2FT.getName());
					DemoFileList cList = DemoTest.getDemos("Resources/Demos/unification.xml");
					operationsReloadTree(cList, panel_5, domainListOperations);
				}
				if (selected.equals("Antiunification")) {
					// load 2ft
					cl.show(panelsOpTab, splitPane2FT.getName());
					DemoFileList cList = DemoTest.getDemos("Resources/Demos/antiunification.xml");
					operationsReloadTree(cList, panel_5, domainListOperations);
				}
				if (selected.equals("Disintegration")) {
					// load 1 ft
					cl.show(panelsOpTab, splitPane1FT.getName());
					DemoFileList cList = DemoTest.getDemos("Resources/Demos/disintegration.xml");
					operationsReloadTree(cList, panel_5, domainListOperations);
				}
				if (selected.equals("Amalgam")) {
					// load 2ft
					treeDemos.setEnabled(false);
					cl.show(panelsOpTab, splitPane2FT.getName());
					DemoFileList cList = DemoTest.getDemos("Resources/Demos/amalgam.xml");
					operationsReloadTree(cList, panel_5, domainListOperations);
				}
			}
		});
	}

	/**
	 * Expand all.
	 * 
	 * @param tree
	 *            the tree
	 */
	public void expandAll(JTree tree) {
		int row = 0;
		while (row < tree.getRowCount()) {
			tree.expandRow(row);
			row++;
		}
	}

	/**
	 * General ontology list listener.
	 * 
	 * @param generalOntologyList
	 *            the general ontology list
	 */
	private void generalOntologyListListener(final JList generalOntologyList) {
		generalOntologyList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

				dm = new FTKBase();

				// list_1.getSelectedValue();
				try {
					writeConsole("Loading " + generalOntologyList.getSelectedValue() + " ...");
					log.info("Loading " + generalOntologyList.getSelectedValue() + " ...");
					generalReloadCB(ontologyList.get(generalOntologyList.getSelectedValue()), panelGeneralCB);
					writeConsole("OK");
					log.info("OK");
					setCursor(null);
				} catch (FeatureTermException e) {
					e.printStackTrace();
					writeConsole("Error: " + e);
					log.error("Error: " + e);
				} catch (IOException e) {
					e.printStackTrace();
					writeConsole("Error: " + e);
					log.error("Error: " + e);
				}
			}
		});
	}

	/**
	 * Gets the distance.
	 * 
	 * @param distance
	 *            the distance
	 * @return the distance
	 */
	private Distance getDistance(Distance distance) {
		distanceMethods key = (distanceMethods) distanceComboBox.getSelectedItem();

		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		try {
			switch (key) {
			case AUDistance:
				distance = new AUDistance();
				break;
			case KashimaKernel:
				distance = new KashimaKernel();
				break;
			case KashimaKernelDAGs:
				distance = new KashimaKernelDAGs();
				break;
			case KashimaKernelDAGsWithRoot:
				distance = new KashimaKernelDAGsWithRoot();
				break;
			case KashimaKernelSparse:
				distance = new KashimaKernelSparse();
				break;
			case KashimaKernelWithRoot:
				distance = new KashimaKernelWithRoot();
				break;
			case PropertiesDistance:
				distance = new PropertiesDistance();
				break;
			case RIBL:
				distance = new RIBL(trainingSetCases, 3);
				break;
			case SHAUD:
				distance = new SHAUD(trainingSetCases);
				break;
			case SHAUD2:
				distance = new SHAUD2(trainingSetCases, 3);
				break;
			case WeightedPropertiesDistance:
				distance = new WeightedPropertiesDistance();
				break;
			default:
				break;
			}
			setCursor(null);
		} catch (FeatureTermException e1) {
			log.error("Error: " + e1);
			e1.printStackTrace();
		}
		return distance;
	}

	/**
	 * Gets the ft from noos string.
	 * 
	 * @param input
	 *            the input
	 * @return the ft from noos string
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	private FeatureTerm getFtFromNoosString(String input) throws FeatureTermException {
		input = input.replace('"', '\0');
		input = input.replace('+', '\0');
		input = input.replace('\\', '"');
		input = input.trim();

		FeatureTerm ft = null;
		try {
			ft = NOOSParser.parse(new RewindableInputStream(new ByteArrayInputStream(input.getBytes("UTF-8"))), case_base, o);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			writeConsole("Error: " + e);
			log.error("Error: " + e);
			JOptionPane.showMessageDialog(new JFrame(), e, "Error", JOptionPane.ERROR_MESSAGE);
		} catch (IOException e) {
			e.printStackTrace();
			writeConsole("Error: " + e);
			log.error("Error: " + e);
			JOptionPane.showMessageDialog(new JFrame(), e, "Error", JOptionPane.ERROR_MESSAGE);
		}
		return ft;
	}

	/**
	 * Gets the heuristic.
	 * 
	 * @return the heuristic
	 */
	private int getHeuristic() {
		int h = 0;
		heuristics key = (heuristics) lazyHeuristic.getSelectedItem();
		switch (key) {
		case HEURISTIC_ENTROPY:
			h = 2;
			break;
		case HEURISTIC_GAIN:
			h = 0;
			break;
		case HEURISTIC_RLDM:
			h = 1;
			break;
		default:
			break;
		}

		return h;
	}

	/**
	 * Gets the pan.
	 * 
	 * @return the pan
	 */
	public CBVisualizer getPan() {
		return pan;
	}

	/**
	 * Gets the selection mode.
	 * 
	 * @return the selection mode
	 */
	private int getSelectionMode() {
		int sm = 0;
		selectionMode key = (selectionMode) lazySelection.getSelectedItem();
		switch (key) {
		case SELECT_MAXIMUM:
			sm = 0;
			break;
		case SELECT_MINIMUM:
			sm = 3;
			break;
		case SELECT_RANDOM:
			sm = 1;
			break;
		case SELECT_RANDOM_PONDERATED:
			sm = 2;
			break;
		default:
			break;
		}

		return sm;
	}

	/**
	 * Gets the t sdata.
	 * 
	 * @param datasetOntologyList
	 *            the inductive ontology list
	 * @return the t sdata
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private TrainingSetProperties getTSdata(final JList datasetOntologyList) throws FeatureTermException, IOException {
		resetOntology();
		int CB = ontologyList.get(datasetOntologyList.getSelectedValue());
		TrainingSetProperties ts;
		ts = TrainingSetUtils.loadTrainingSet(CB, o, dm, case_base);
		return ts;
	}

	/**
	 * Inductive btn evaluate listener.
	 *
	 * @param inductiveBtnEvaluate the inductive btn evaluate
	 */
	private void inductiveBtnEvaluateListener(final JButton inductiveBtnEvaluate) {
		inductiveBtnEvaluate.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// El Learn es lo que dices, sobre el Evaluate, lo que tienes que hacer es coger lo que te ha devuelto
				// el generateHypothesis, que deber�a ser un objeto de tipo "Hypothesis". La hypothesis tiene un m�todo
				// que se llama
				// "generatePrediction. Lo que tienes que hacer, es para cada uno de los problemas de test, llamar al "generatePrediction"
				// y comprobar que la soluci�n que da es la misma que la del problema.
				// El m�todo "generatePrediction" devuelve una "prediction". Prediction tiene un campo que es
				// "solutions", que normalmente solo tiene un elemento.
				// Si la lista solo tiene un elemento, y el elemento es "equivalents" a la soluci�n del problema, es que
				// la predicci�n es correcta. El resultade de "Evaluate"
				// es el porcentaje de problemas de test para los que se ha predecido la soluci�n correcta.

				if (inductiveBtnEvaluate.isEnabled()) {

					int trueSols = 0;
					FeatureTerm sol = null;

					// recorrer
					for (FeatureTerm f : testSetCases) {
						try {
							sol = f.readPath(tsSol);
							Prediction pre = generatedHypothesis.generatePrediction(f, dm, false);

							// if (generatedHypothesis instanceof RuleHypothesis) {
							// ruleHypothesis.add((RuleHypothesis) generatedHypothesis);
							// inductiveRules.setVisible(true);
							// }

							if (pre.solutions.size() == 1 && pre.solutions.get(0).equivalents(sol)) {
								trueSols++;
							}
						} catch (FeatureTermException e1) {
							log.error("Error: " + e1);
							e1.printStackTrace();
						} catch (Exception e1) {
							log.error("Error: " + e1);
							e1.printStackTrace();
						}
					}

					String message = "Accuracy: " + dec.format((float) ((float) trueSols / (float) testSetCases.size()) * 100) + "%";
					System.out.println(message);
					writeConsole(message);
					log.info(message);
					log.info(message);
					JOptionPane.showMessageDialog(new JFrame(), message, "Result", JOptionPane.INFORMATION_MESSAGE);

				}

			}
		});
	}

	/**
	 * Inductive build case button listener.
	 * 
	 * @param inductiveOntologyList
	 *            the inductive ontology list
	 * @param inductiveMethodsList
	 *            the inductive methods list
	 * @param inductiveSlider
	 *            the inductive slider
	 * @param inductiveBtnBuildCaseBase
	 *            the inductive btn build case base
	 * @param inductiveBtnLearn
	 *            the inductive btn learn
	 * @param inductiveBtnEvaluate
	 *            the inductive btn evaluate
	 * @param testSetPanel
	 *            the test set panel
	 */
	private void inductiveBuildCaseListener(final JList inductiveOntologyList, final JList inductiveMethodsList,
			final JSlider inductiveSlider, final JButton inductiveBtnBuildCaseBase, final JButton inductiveBtnLearn,
			final JButton inductiveBtnEvaluate, final JPanel testSetPanel) {
		inductiveBtnBuildCaseBase.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (inductiveBtnBuildCaseBase.isEnabled()) {
					if (inductiveOntologyList.isSelectionEmpty()) {
						JOptionPane.showMessageDialog(new JFrame(), "Please select an ontology first", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}

					// learn
					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					// switch seg�n m�todo
					try {
						ts = getTSdata(inductiveOntologyList);
						List<FeatureTerm> allCases = ts.cases;
						trainingSetCases = randomSelection(allCases, inductiveSlider.getValue());

						tsSol = ts.solution_path;

						testSetCases = null;
						testSetCases = new ArrayList<FeatureTerm>();
						testSetCases.addAll(allCases);
						testSetCases.removeAll(trainingSetCases);
						generatedHypothesis = null;

						testSetPanel.removeAll();
						pan = new CBVisualizer((int) testSetPanel.getSize().getWidth(), (int) testSetPanel.getSize().getHeight(),
								testSetCases, dm, ts.solution_path, ts.description_path, true, FTLGUI.this, ts, false);
						testSetPanel.add(pan);
						// testSetPanel.revalidate();
						testSetPanel.setVisible(true);

						if (!inductiveMethodsList.isSelectionEmpty())
							inductiveBtnLearn.setEnabled(true);

						inductiveBtnEvaluate.setEnabled(false);
						inductiveControlPanel.setEnabled(false);

					} catch (FeatureTermException e) {
						e.printStackTrace();
						writeConsole("Error: " + e);
						log.error("Error: " + e);
					} catch (IOException e) {
						e.printStackTrace();
						writeConsole("Error: " + e);
						log.error("Error: " + e);
					} catch (Exception e) {
						e.printStackTrace();
						writeConsole("Error: " + e);
						log.error("Error: " + e);
					}
				}
			}
		});
	}

	/**
	 * Inductive classes list listener.
	 * 
	 * @param inductiveClassesList
	 *            the inductive classes list
	 */
	private void inductiveClassesListListener(final JList inductiveClassesList) {
		inductiveClassesList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (inductiveClassesList.isEnabled()) {
					rulesEnabler(true);

					rulesList.clear();
					HashSet<FeatureTerm> hash = new HashSet<FeatureTerm>();
					for (RuleHypothesis rh : ruleHypothesis) {
						for (Rule r : rh.getRules()) {
							if (r.solution.getName().equals(inductiveClassesList.getSelectedValue())) {
								hash.add(r.pattern);
								// rulesList.add(r.pattern);
							}
						}
					}

					// System.out.println("Hash: " + hash.size() + " List: " + rulesList.size());
					rulesList = new ArrayList<FeatureTerm>(hash);
					if (!ruleHypothesis.isEmpty())
						ruleIndex.setValue(1);
					ruleTotal.setValue(rulesList.size() - 1);
					try {
						if (!rulesList.isEmpty()) {
							loadFT(rulesList.get(0), inductiveRules);
						} else {
							ruleNext.setEnabled(false);
							rulePrevious.setEnabled(false);
							writeConsole("There are no rules for " + inductiveClassesList.getSelectedValue().toString());
						}
						
					} catch (FeatureTermException e) {
						e.printStackTrace();
						writeConsole("Error: " + e);
						log.error("Error: " + e);
					}
				}
			}

		});
	}

	/**
	 * Inductive functions.
	 * 
	 * @param inductiveOntologyList
	 *            the inductive ontology list
	 * @param inductiveModel
	 *            the inductive model
	 * @param inductiveClassesList
	 *            the inductive classes list
	 * @param inductiveMethodsScroll
	 *            the inductive methods scroll
	 * @param inductiveMethodsList
	 *            the inductive methods list
	 * @param inductiveSlider
	 *            the inductive slider
	 * @param inductiveBtnBuildCaseBase
	 *            the inductive btn build case base
	 * @param inductiveBtnLearn
	 *            the inductive btn learn
	 * @param inductiveBtnEvaluate
	 *            the inductive btn evaluate
	 * @param testSetPanel
	 *            the test set panel
	 */
	private void inductiveFunctions(final JList inductiveOntologyList, final DefaultListModel inductiveModel,
			final JList inductiveClassesList, final JScrollPane inductiveMethodsScroll, final JList inductiveMethodsList,
			final JSlider inductiveSlider, final JButton inductiveBtnBuildCaseBase, final JButton inductiveBtnLearn,
			final JButton inductiveBtnEvaluate, final JPanel testSetPanel) {
		inductiveLearnListener(inductiveMethodsList, inductiveBtnLearn, inductiveBtnEvaluate, inductiveClassesList);
		inductiveBuildCaseListener(inductiveOntologyList, inductiveMethodsList, inductiveSlider, inductiveBtnBuildCaseBase,
				inductiveBtnLearn, inductiveBtnEvaluate, testSetPanel);
		inductiveOntologyListListener(inductiveOntologyList, inductiveModel, inductiveClassesList, inductiveMethodsList,
				inductiveBtnBuildCaseBase, inductiveBtnLearn, inductiveBtnEvaluate, testSetPanel);
		inductiveMethodsListListener(inductiveOntologyList, inductiveMethodsScroll, inductiveMethodsList, inductiveBtnBuildCaseBase,
				inductiveBtnEvaluate);
	}

	/**
	 * Inductive learn listener.
	 *
	 * @param inductiveMethodsList the inductive methods list
	 * @param inductiveBtnLearn the inductive btn learn
	 * @param inductiveBtnEvaluate the inductive btn evaluate
	 * @param inductiveClassesList the inductive classes list
	 */
	private void inductiveLearnListener(final JList inductiveMethodsList, final JButton inductiveBtnLearn,
			final JButton inductiveBtnEvaluate, final JList inductiveClassesList) {
		inductiveBtnLearn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (inductiveBtnLearn.isEnabled()) {
					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

					if (inductiveMethodsList.isSelectionEmpty()) {
						JOptionPane.showMessageDialog(new JFrame(), "Please select a method from the list", "Error",
								JOptionPane.ERROR_MESSAGE);
						return;
					}
					inductiveMethods selMethod = (inductiveMethods) inductiveMethodsList.getSelectedValue();
					generatedHypothesis = null;

					System.out.println("Casos: " + trainingSetCases.size() + " ts: " + ts.name);
					writeConsole("#Cases: " + trainingSetCases.size() + " Type: " + ts.name);
					log.info("#Cases: " + trainingSetCases.size() + " Type: " + ts.name);
					int dialogButton = JOptionPane.YES_NO_OPTION;
					try {
						switch (selMethod) {
						case CN2:
							CN2 learner = new CN2(3);
							generatedHypothesis = learner
									.generateHypothesis(trainingSetCases, ts.description_path, ts.solution_path, o, dm);
							break;
						case ID3:
							ID3 learner2 = new ID3();
							generatedHypothesis = learner2.generateHypothesis(trainingSetCases, ts.description_path, ts.solution_path, o,
									dm);
							break;
						case J48:
							ConversionRecord r = NOOSToWeka.toInstances(ts.cases, ts.description_path, ts.solution_path, dm, o);
							J48 learner3 = new J48(r);
							generatedHypothesis = learner3.generateHypothesis(trainingSetCases, ts.description_path, ts.solution_path, o,
									dm);
							break;
						case PropertiesCN2:

							int dialogresult = JOptionPane.showConfirmDialog(null, "PropertiesCN2 can be really slow. Are you sure?",
									"Warning", dialogButton);
							if (dialogresult == JOptionPane.YES_OPTION) {
								PropertiesCN2 learner4 = new PropertiesCN2();
								generatedHypothesis = learner4.generateHypothesis(trainingSetCases, ts.description_path, ts.solution_path,
										o, dm);
							} else {
								setCursor(null);
								return;
							}

							break;
						case PropertiesTree:
							int dialogresult2 = JOptionPane.showConfirmDialog(null, "PropertiesCN2 can be really slow. Are you sure?",
									"Warning", dialogButton);

							if (dialogresult2 == JOptionPane.YES_OPTION) {
								PropertiesTree learner5 = new PropertiesTree(false);
								generatedHypothesis = learner5.generateHypothesis(trainingSetCases, ts.description_path, ts.solution_path,
										o, dm);
							} else {
								setCursor(null);
								return;
							}
							break;
						case RefinementCN2:
							RefinementCN2 learner6 = new RefinementCN2();
							generatedHypothesis = learner6.generateHypothesis(trainingSetCases, ts.description_path, ts.solution_path, o,
									dm);
							break;
						case RefinementHydra:
							RefinementHYDRA learner7 = new RefinementHYDRA();
							generatedHypothesis = learner7.generateHypothesis(trainingSetCases, ts.description_path, ts.solution_path, o,
									dm);
							break;
						case RefinementINDIE:
							RefinementINDIE learner8 = new RefinementINDIE();
							generatedHypothesis = learner8.generateHypothesis(trainingSetCases, ts.description_path, ts.solution_path, o,
									dm);
							break;
						default:
							break;

						}
						FeatureTerm sol = null;

						// recorrer
						for (FeatureTerm f : testSetCases) {
							try {
								sol = f.readPath(tsSol);
								Prediction pre = generatedHypothesis.generatePrediction(f, dm, false);

								if (generatedHypothesis instanceof RuleHypothesis) {
									ruleHypothesis.add((RuleHypothesis) generatedHypothesis);
									inductiveRules.setVisible(true);
								}

							} catch (FeatureTermException e1) {
								log.error("Error: " + e1);
								e1.printStackTrace();
							} catch (Exception e1) {
								log.error("Error: " + e1);
								e1.printStackTrace();
							}
						}
					} catch (FeatureTermException e) {
						e.printStackTrace();
						writeConsole("Error: " + e);
						log.error("Error: " + e);
					} catch (Exception e) {
						e.printStackTrace();
						writeConsole("Error: " + e);
						log.error("Error: " + e);
					}
					inductiveBtnEvaluate.setEnabled(true);
					inductiveClassesList.setEnabled(true);
					setCursor(null);
				}
			}

		});
	}

	/**
	 * Inductive methods list listener.
	 * 
	 * @param inductiveOntologyList
	 *            the inductive ontology list
	 * @param inductiveMethodsScroll
	 *            the inductive methods scroll
	 * @param inductiveMethodsList
	 *            the inductive methods list
	 * @param inductiveBtnBuildCaseBase
	 *            the inductive btn build case base
	 * @param inductiveBtnEvaluate
	 *            the inductive btn evaluate
	 */
	private void inductiveMethodsListListener(final JList inductiveOntologyList, final JScrollPane inductiveMethodsScroll,
			final JList inductiveMethodsList, final JButton inductiveBtnBuildCaseBase, final JButton inductiveBtnEvaluate) {
		inductiveMethodsList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (!inductiveOntologyList.isSelectionEmpty()) {
					inductiveBtnBuildCaseBase.setEnabled(true);
					inductiveMethodsScroll.setEnabled(true);
				}
				// btnLearn.setEnabled(false);
				inductiveBtnEvaluate.setEnabled(false);
				// testSetPanel.removeAll();
				// testSetPanel.revalidate();
				// testSetPanel.setVisible(true);
			}
		});
	}

	/**
	 * Inductive ontology list listener.
	 * 
	 * @param inductiveOntologyList
	 *            the inductive ontology list
	 * @param inductiveModel
	 *            the inductive model
	 * @param inductiveClassesList
	 *            the inductive classes list
	 * @param inductiveMethodsList
	 *            the inductive methods list
	 * @param inductiveBtnBuildCaseBase
	 *            the inductive btn build case base
	 * @param inductiveBtnLearn
	 *            the inductive btn learn
	 * @param inductiveBtnEvaluate
	 *            the inductive btn evaluate
	 * @param testSetPanel
	 *            the test set panel
	 */
	private void inductiveOntologyListListener(final JList inductiveOntologyList, final DefaultListModel inductiveModel,
			final JList inductiveClassesList, final JList inductiveMethodsList, final JButton inductiveBtnBuildCaseBase,
			final JButton inductiveBtnLearn, final JButton inductiveBtnEvaluate, final JPanel testSetPanel) {
		inductiveOntologyList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				writeConsole("Inductive: loading " + inductiveOntologyList.getSelectedValue());
				log.info("Inductive: loading " + inductiveOntologyList.getSelectedValue());
				try {
					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

					inductiveBtnLearn.setEnabled(false);
					inductiveBtnEvaluate.setEnabled(false);
					inductiveControlPanel.setEnabled(false);
					inductiveClassesList.setEnabled(false);
					inductiveMethodsList.setEnabled(true);
					inductiveRules.removeAll();
					rulesList.clear();
					rulesEnabler(false);
					if (!inductiveMethodsList.isSelectionEmpty()) {
						inductiveBtnBuildCaseBase.setEnabled(true);
						inductiveMethodsList.setEnabled(true);
					}
					testSetPanel.removeAll();

					TrainingSetProperties ts = getTSdata(inductiveOntologyList);

					inductiveModel.removeAllElements();
					for (FeatureTerm solution : ts.differentSolutions()) {
						if (solution.getName() != null)
							inductiveModel.add(inductiveClassesList.getModel().getSize(), solution.getName());
					}

					setCursor(null);
				} catch (FeatureTermException e) {
					e.printStackTrace();
					writeConsole("Error: " + e);
					log.error("Error: " + e);
				} catch (IOException e) {
					e.printStackTrace();
					writeConsole("Error: " + e);
					log.error("Error: " + e);
				}

				// if (!inductiveMethodsList.isSelectionEmpty())
				// btnLearn.setEnabled(true);
			}
		});
	}

	/**
	 * Inductive rule next listener.
	 */
	private void inductiveRuleNextListener() {
		ruleNext.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {

				if ((Integer) ruleIndex.getValue() > 0 && (Integer) ruleIndex.getValue() < (Integer) ruleTotal.getValue()) {
					ruleIndex.setValue((Integer) ruleIndex.getValue() + 1);
					try {
						loadFT(rulesList.get((Integer) ruleIndex.getValue()), inductiveRules);
					} catch (FeatureTermException e1) {
						log.error("Error: " + e1);
						e1.printStackTrace();
					}
				}
			}
		});
	}

	/**
	 * Inductive rule previous listener.
	 */
	private void inductiveRulePreviousListener() {
		rulePrevious.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				inductiveRules.removeAll();
				if ((Integer) ruleIndex.getValue() > 1) {
					ruleIndex.setValue((Integer) ruleIndex.getValue() - 1);

					try {
						loadFT(rulesList.get((Integer) ruleIndex.getValue()), inductiveRules);
					} catch (FeatureTermException e1) {
						log.error("Error: " + e1);
						e1.printStackTrace();
					}
				}
			}
		});
	}

	/**
	 * Inductive slider listener.
	 * 
	 * @param inductiveSlider
	 *            the inductive slider
	 */
	private void inductiveSliderListener(final JSlider inductiveSlider) {
		inductiveSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				inductiveSliderValue.setText(String.valueOf(inductiveSlider.getValue()));
			}
		});
	}

	/**
	 * Inductive slider value key listener.
	 * 
	 * @param inductiveSlider
	 *            the inductive slider
	 */
	private void inductiveSliderValueKeyListener(final JSlider inductiveSlider) {
		inductiveSliderValue.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				int key = arg0.getKeyCode();
				if (key == KeyEvent.VK_ENTER) {
					keyControl(inductiveSlider);
				}
			}
		});
	}

	/**
	 * Inductive slider value listener.
	 * 
	 * @param inductiveSlider
	 *            the inductive slider
	 */
	private void inductiveSliderValueListener(final JSlider inductiveSlider) {
		inductiveSliderValue.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				keyControl(inductiveSlider);
			}
		});
	}

	/**
	 * Checks if is numeric.
	 * 
	 * @param string
	 *            the string
	 * @return true, if is numeric
	 */
	public boolean isNumeric(String string) {

		try {
			Integer.parseInt(string);
			return true;
		} catch (NumberFormatException nfe) {
			return false;
		}

	}

	/**
	 * K edited.
	 */
	private void kEdited() {
		if (!isNumeric(kValue.getText())) {
			kValue.setText(String.valueOf(kValue.getText().replaceAll("\\D+", "")));
		}
		// if (kValue.getText().isEmpty())
		// kValue.setText("1");
		if (Integer.parseInt(kValue.getText()) < 1 || Integer.parseInt(kValue.getText()) > lazyTestSetCases.size()) {
			kValue.setText("1");
		}
		if (Integer.parseInt(kValue.getText()) > 15 || Integer.parseInt(kValue.getText()) > lazyTestSetCases.size() / 10) {
			String message = "K should be a value from 1 to 5.";
			writeConsole(message);
			log.info(message);
			log.info(message);
			JOptionPane.showMessageDialog(new JFrame(), message, "Warning", JOptionPane.WARNING_MESSAGE);
		}
	}

	/**
	 * Key control.
	 * 
	 * @param slider
	 *            the slider
	 */
	private void keyControl(final JSlider slider) {
		if (isNumeric(inductiveSliderValue.getText())) {
			int intSlider = Integer.parseInt(inductiveSliderValue.getText());
			if (intSlider < 0 || intSlider > 100) {
				inductiveSliderValue.setText(String.valueOf(slider.getValue()));
			} else {
				slider.setValue(intSlider);
			}
		} else {
			inductiveSliderValue.setText(String.valueOf(slider.getValue()));
		}
	}

	/**
	 * Key control2.
	 * 
	 * @param slider
	 *            the slider
	 */
	protected void keyControl2(final JSlider slider) {
		if (isNumeric(lazySliderValue.getText())) {
			int intSlider = Integer.parseInt(lazySliderValue.getText());
			if (intSlider < 0 || intSlider > 100) {
				lazySliderValue.setText(String.valueOf(slider.getValue()));
			} else {
				slider.setValue(intSlider);
			}
		} else {
			lazySliderValue.setText(String.valueOf(slider.getValue()));
		}
	}

	/**
	 * K value listener.
	 */
	private void kValueListener() {
		kValue.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				kEdited();
			}
		});
		kValue.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				kEdited();
			}
		});
		kValue.setToolTipText("K can't be more than the number of feature terms in the set");
		kValue.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				kEdited();
			}
		});
	}

	/**
	 * Launch amail.
	 * 
	 * @param btnNewButton
	 *            the btn new button
	 */
	private void launchAmail(JButton btnNewButton) {
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				AMAILVis.main(null);
				setCursor(null);
			}
		});
	}

	/**
	 * Lazy build case listener.
	 * 
	 * @param lazyOntologyList
	 *            the lazy ontology list
	 * @param lazySlider
	 *            the lazy slider
	 * @param lazyBtnBuildCaseBase
	 *            the lazy btn build case base
	 * @param lazyMethodsList
	 *            the lazy methods list
	 * @param splitPane_4
	 *            the split pane_4
	 */
	private void lazyBuildCaseListener(final JList lazyOntologyList, final JSlider lazySlider, final JButton lazyBtnBuildCaseBase,
			final JList lazyMethodsList, final JSplitPane splitPane_4) {
		lazyBtnBuildCaseBase.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (lazyBtnBuildCaseBase.isEnabled()) {
					if (lazyOntologyList.isSelectionEmpty()) {
						JOptionPane.showMessageDialog(new JFrame(), "Please select an ontology first", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					if (lazyMethodsList.isSelectionEmpty()) {
						JOptionPane.showMessageDialog(new JFrame(), "Please select a method from the list", "Error",
								JOptionPane.ERROR_MESSAGE);
						return;
					}
					// learn
					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					// switch seg�n m�todo
					try {
						ts = getTSdata(lazyOntologyList);

						List<FeatureTerm> allCases = ts.cases;
						trainingSetCases = randomSelection(allCases, lazySlider.getValue());

						tsSol = ts.solution_path;

						lazyTestSetCases = new ArrayList<FeatureTerm>();
						lazyTestSetCases.addAll(allCases);
						lazyTestSetCases.removeAll(trainingSetCases);

						lazyTestSetPanel.removeAll();
						pan = new CBVisualizer((int) lazyTestSetPanel.getSize().getWidth(), (int) lazyTestSetPanel.getSize().getHeight(),
								lazyTestSetCases, dm, ts.solution_path, ts.description_path, true, FTLGUI.this, ts, false);
						lazyTestSetPanel.add(pan);
						// lazyTestSetPanel.revalidate();
						lazyTestSetPanel.setVisible(true);
						// splitPane_4.revalidate();
						splitPane_4.setVisible(true);
						kValue.setEnabled(true);

					} catch (FeatureTermException e1) {
						log.error("Error: " + e1);
						e1.printStackTrace();
					} catch (IOException e1) {
						log.error("Error: " + e1);
						e1.printStackTrace();
					} catch (Exception e1) {
						log.error("Error: " + e1);
						e1.printStackTrace();
					}

					// kValue.setText(String.valueOf(lazyTestSetCases.size()));
					// lazyMinPrecedents.setText(String.valueOf(lazyTestSetCases.size()));
					setCursor(null);

				}
			}
		});
	}

	/**
	 * Lazy classify.
	 * 
	 * @param t
	 *            the t
	 * @param cases
	 *            the cases
	 * @param solution_path
	 *            the solution_path
	 * @param description_path
	 *            the description_path
	 */
	public void lazyClassify(FeatureTerm t, List<FeatureTerm> cases, Path solution_path, Path description_path) {

		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		lazyMethods selMethod = (lazyMethods) lMethodsList.getSelectedValue();

		System.out.println("Casos: " + trainingSetCases.size() + " ts: " + ts.name);

		int K = 0;
		Distance distance = null;

		try {
			switch (selMethod) {
			case KNN:
				K = Math.min(Integer.parseInt(kValue.getText()), cases.size());
				distance = getDistance(distance);
				lazyPrediction = KNN.predict(t.readPath(description_path), cases, description_path, solution_path, o, dm, K, distance);
				break;
			case KNNCSA:
				K = Math.min(Integer.parseInt(kValue.getText()), cases.size());
				distance = getDistance(distance);
				lazyPrediction = KNNCSA.predict(t.readPath(description_path), cases, description_path, solution_path, o, dm, K, distance);
				break;
			case LID:

				int heuristic = getHeuristic();
				int selection_mode = getSelectionMode();

				lazyPrediction = LID.predict(t.readPath(description_path), cases, description_path, solution_path, o, dm, heuristic,
						selection_mode, 0);

				FeatureTerm justification = lazyPrediction.justifications.get(lazyPrediction.solutions.get(0));
				lazyPrediction.toString(dm);
				// System.out.println(lazyPrediction.toString(dm));

				loadFTintoPanel(justification, lazyClassifyPanel);
				break;
			case RefinementLID:
				int heuristic1 = getHeuristic();
				int selection_mode1 = getSelectionMode();
				int min_precedents1 = Integer.parseInt(lazyMinPrecedents.getText());
				boolean generalize = lazyChckBoxGeneralize.isSelected();
				lazyPrediction = RefinementLID.predict_RLID3(t.readPath(description_path), cases, description_path, solution_path, o, dm,
						heuristic1, selection_mode1, min_precedents1, generalize);
				FeatureTerm justification1 = lazyPrediction.justifications.get(lazyPrediction.solutions.get(0));
				loadFTintoPanel(justification1, lazyClassifyPanel);
				break;
			default:
				break;
			}

			final String message;
			btnLazyEvaluate.setEnabled(true);
			setCursor(null);
			this.setEnabled(true);

			if (lazyPrediction.solutions.size() != 0) {
				message = "Case [" + t.getName() + "] is from class [" + lazyPrediction.solutions.get(0).getName() + "] according to "
						+ lMethodsList.getSelectedValue();
				writeConsole(message);
				log.info(message);
			} else {
				message = "Case [" + t.getName() + "] doesn't have any solution according to  " + lMethodsList.getSelectedValue();
				writeConsole(message);
				log.info(message);

			}

			// javax.swing.SwingUtilities.invokeLater(new Runnable() {
			// public void run() {
			// JOptionPane.showMessageDialog(new JFrame(), message, "Solution", JOptionPane.INFORMATION_MESSAGE);
			// }
			// });

		} catch (Exception e) {
			e.printStackTrace();
			writeConsole("Error: " + e);
			log.error("Error: " + e);
		}
	}

	/**
	 * Lazy methods list listener.
	 * 
	 * @param lazyOntologyList
	 *            the lazy ontology list
	 * @param lazyBtnBuildCaseBase
	 *            the lazy btn build case base
	 * @param lazyMethodsList
	 *            the lazy methods list
	 * @param lazyMethodsSettings
	 *            the lazy methods settings
	 * @param lblMinPrecedents
	 *            the lbl min precedents
	 */
	private void lazyMethodsListListener(final JList lazyOntologyList, final JButton lazyBtnBuildCaseBase, final JList lazyMethodsList,
			final JPanel lazyMethodsSettings, final JLabel lblMinPrecedents) {
		lazyMethodsList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				lazyClassifyPanel.removeAll();
				lazyClassifyPanel.repaint();

				lazyMethods selMethod = (lazyMethods) lMethodsList.getSelectedValue();

				CardLayout cl = (CardLayout) (lazyMethodsSettings.getLayout());

				try {
					switch (selMethod) {
					case KNN:
						cl.show(lazyMethodsSettings, "settingsKNN");
						break;
					case KNNCSA:
						cl.show(lazyMethodsSettings, "settingsKNN");
						break;
					case LID:
						lazyChckBoxGeneralize.setVisible(false);
						lblMinPrecedents.setVisible(false);
						lazyMinPrecedents.setVisible(false);
						cl.show(lazyMethodsSettings, "settingsLID");

						break;
					case RefinementLID:
						lazyChckBoxGeneralize.setVisible(true);
						lblMinPrecedents.setVisible(true);
						lazyMinPrecedents.setVisible(true);
						cl.show(lazyMethodsSettings, "settingsLID");
						break;
					default:
						System.out.println("Error");
						break;
					}

				} catch (Exception e1) {
					log.error("Error: " + e1);
					e1.printStackTrace();
				}

				if (!lazyOntologyList.isSelectionEmpty())
					lazyBtnBuildCaseBase.setEnabled(true);

				btnLazyEvaluate.setEnabled(false);

				// testSetPanel.removeAll();
				// testSetPanel.revalidate();
				// testSetPanel.setVisible(true);
			}
		});
	}

	/**
	 * Lazy min precedents listener.
	 */
	private void lazyMinPrecedentsListener() {
		lazyMinPrecedents.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (!isNumeric(lazyMinPrecedents.getText())) {
					lazyMinPrecedents.setText(String.valueOf(lazyMinPrecedents.getText().replaceAll("\\D+", "")));
				}
				if (Integer.parseInt(lazyMinPrecedents.toString()) <= 0)
					lazyMinPrecedents.setText("1");

				if (lazyMinPrecedents.getText().isEmpty())
					lazyMinPrecedents.setText("1");
			}
		});
	}

	/**
	 * Lazy ontology list listener.
	 * 
	 * @param lazyOntologyList
	 *            the lazy ontology list
	 * @param lazyModel
	 *            the lazy model
	 * @param lazyClassesList
	 *            the lazy classes list
	 * @param lazyBtnBuildCaseBase
	 *            the lazy btn build case base
	 * @param lazyMethodsList
	 *            the lazy methods list
	 */
	private void lazyOntologyListListener(final JList lazyOntologyList, final DefaultListModel lazyModel, final JList lazyClassesList,
			final JButton lazyBtnBuildCaseBase, final JList lazyMethodsList) {
		lazyOntologyList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				try {

					// dm = new FTKBase();
					// resetOntology();

					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					writeConsole("Lazy methods: loading " + lazyOntologyList.getSelectedValue().toString());
					log.info("Lazy methods: loading " + lazyOntologyList.getSelectedValue().toString());
					if (!lazyMethodsList.isSelectionEmpty())
						lazyBtnBuildCaseBase.setEnabled(true);

					// splitPane_4.removeAll();
					lazyTestSetPanel.removeAll();
					lazyTestSetPanel.repaint();
					lazyClassifyPanel.removeAll();
					lazyClassifyPanel.repaint();
					btnLazyEvaluate.setEnabled(false);

					ts = getTSdata(lazyOntologyList);

					lazyModel.removeAllElements();
					for (FeatureTerm solution : ts.differentSolutions()) {
						if (solution.getName() != null)
							lazyModel.add(lazyClassesList.getModel().getSize(), solution.getName());
					}

					setCursor(null);
				} catch (FeatureTermException e) {
					e.printStackTrace();
					writeConsole("Error: " + e);
					log.error("Error: " + e);
				} catch (IOException e) {
					e.printStackTrace();
					writeConsole("Error: " + e);
					log.error("Error: " + e);
				}
			}
		});
	}

	/**
	 * Lazy slider listener.
	 * 
	 * @param lazySlider
	 *            the lazy slider
	 */
	private void lazySliderListener(final JSlider lazySlider) {
		lazySlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				lazySliderValue.setText(String.valueOf(lazySlider.getValue()));
			}
		});
	}

	/**
	 * Lazy slider value listener.
	 * 
	 * @param lazySlider
	 *            the lazy slider
	 */
	private void lazySliderValueListener(final JSlider lazySlider) {
		lazySliderValue.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				keyControl2(lazySlider);
			}
		});
		lazySliderValue.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				keyControl2(lazySlider);
			}
		});
	};

	/**
	 * Reload ft.
	 * 
	 * @param ft
	 *            the ft
	 * @param panel
	 *            the panel
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public void loadFT(FeatureTerm ft, JPanel panel) throws FeatureTermException {
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		FTVisualizer ftr = new FTVisualizer(panel.getHeight(), panel.getWidth(), ft, dm, true, true);
		panel.removeAll();
		panel.add(ftr);
		panel.revalidate();
		panel.setVisible(true);
		setCursor(null);

	}

	/**
	 * Reload ft.
	 * 
	 * @param input
	 *            the input
	 * @param panel
	 *            the panel
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public void loadFT(String input, JPanel panel) throws FeatureTermException {
		FeatureTerm ft = getFtFromNoosString(input);

		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		FTVisualizer ftr = new FTVisualizer(1, 1, ft, dm, true, true);
		panel.removeAll();
		panel.add(ftr);
		panel.revalidate();
		setCursor(null);

	}

	/**
	 * Load f tinto panel.
	 * 
	 * @param ft
	 *            the ft
	 * @param panel
	 *            the panel
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public void loadFTintoPanel(FeatureTerm ft, JPanel panel) throws FeatureTermException {
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		FTVisualizer ftr = new FTVisualizer(1, 1, ft, dm, true, true);
		panel.removeAll();
		panel.add(ftr);
		panel.revalidate();
		setCursor(null);

	}

	/**
	 * Process.
	 * 
	 * @param line
	 *            the line
	 * @param textArea
	 *            the text area
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	private void processConsoleInput(String line, JTextArea textArea) throws FeatureTermException {
		line = line.replaceAll(" ", "");

		if (line.equals("help")) {
			textArea.append("\n\n  Once an ontology loaded, you can type each of these avaliable operations: "
					+ "\n  ** subsumption(<Feature Term 1>,<Feature Term 2>) " + "\n  ** unification(<Feature Term 1>,<Feature Term 2>) "
					+ "\n  ** antiunification(<Feature Term 1>,<Feature Term 2>) " + "\n  ** amalgam(<Feature Term 1>,<Feature Term 2>) "
					+ " \n  ** termVisualization(<Feature Term>)");
			return;
		}

		if (terms.size() == 0) {
			textArea.append("\nLoad an ontology first or type 'help'");
			return;
		}
		if (line.contains("(") && line.contains(")")) {
			// parse
			int startParams = line.indexOf('(');
			int endParams = line.indexOf(')');
			String method = line.substring(0, startParams);
			String params = line.substring(startParams + 1, endParams);

			String[] param = params.split(",");
			if (param.length == 2 && param[0] == param[1]) {
				textArea.append("\nSame parameters");
				return;
			}

			// get terms from ontology
			FeatureTerm f1 = null;
			FeatureTerm f2 = null;

			for (Iterator<FeatureTerm> iterator = terms.iterator(); iterator.hasNext();) {

				FeatureTerm ft = (FeatureTerm) iterator.next();
				// System.out.println(ft.getName());
				if (ft.getName().equals(param[0])) {
					f1 = ft;
					// System.out.println(f1.getName());
				}
				if (param.length > 1 && ft.getName().equals(param[1])) {
					f2 = ft;
					// System.out.println(f2.getName());
				}
			}

			if (param.length == 1 && f1 == null) {
				textArea.append("\nFeature term not found. Incorrect name?");
				return;
			}
			if (param.length == 2) {
				if (f1 == null || f2 == null) {
					textArea.append("\nFeature terms not found. Incorrect names?");
					return;
				}
			}
			// select method
			if (method.toLowerCase().equals("subsumption")) {
				if (param.length == 2) {
					textArea.append("\nSubsumption = " + f1.subsumes(f2));
				} else {
					textArea.append("\nWrong number of parameters");
				}
			} else if (method.toLowerCase().equals("unification")) {
				if (param.length == 2) {
					textArea.append("\nLoading...");
					List<FeatureTerm> unifications = FTUnification.unification(f1, f2, dm);
					if (unifications != null && !unifications.isEmpty()) {
						textArea.append("\nUnification successful!");
						textArea.append("\n" + unifications.size() + " unifications");

						for (FeatureTerm u : unifications) {
							textArea.append("\n" + u.toStringNOOS(dm));
							if (!f1.subsumes(u)) {
								textArea.append("\n" + f1.getName() + " not subsumes U");
							}
							if (!f2.subsumes(u)) {
								textArea.append("\n" + f2.getName() + " not subsumes U");
							}
						}
					} else {
						textArea.append("\nNo unifications");
					}
				} else {
					textArea.append("\nWrong number of parameters");
				}
			} else if (method.toLowerCase().equals("antiunification")) {
				if (param.length == 2) {
					List<FeatureTerm> objects = new LinkedList<FeatureTerm>();

					objects.add(f1);
					objects.add(f2);
					textArea.append("\nLoading...");
					List<FeatureTerm> Antiunifications = FTAntiunification.antiunification(objects, 0, null, o, dm, true, 0);

					if (Antiunifications != null && !Antiunifications.isEmpty()) {
						textArea.append("\nAntiunification successful!");
						textArea.append("\n" + Antiunifications.size() + " Antiunifications");

						for (FeatureTerm u : Antiunifications) {
							textArea.append("\n" + u.toStringNOOS(dm));
							if (!u.subsumes(f1)) {
								textArea.append("\nU not subsumes " + f1.getName());
							}
							if (!u.subsumes(f2)) {
								textArea.append("\nU not subsumes " + f2.getName());
							}
						}
					} else {
						textArea.append("\nNo antiunifications");
					}
				}
			} else if (method.toLowerCase().equals("amalgam")) {
				if (param.length == 2) {
					AmalgamEvaluationFunction ef = new FTEFCompactness();

					textArea.append("\nLoading...");
					List<AmalgamResult> amalgams = Amalgam.amalgamRefinementsGreedy(f1, f2, ef, o, dm);

					if (amalgams != null && !amalgams.isEmpty()) {
						textArea.append("\nAmalgam successful!");
						textArea.append("\n" + amalgams.size() + " amalgams");

						for (AmalgamResult u : amalgams) {
							textArea.append("\n AMALGAM");
							textArea.append("\n" + u.getAmalgam().toStringNOOS(dm));
							textArea.append("\n EVALUATION");
							textArea.append("\n" + u.getEvaluation());
							textArea.append("\n TRANSFER 1");
							textArea.append("\n" + u.getTransfer1().toStringNOOS(dm));
							textArea.append("\n TRANSFER 2");
							textArea.append("\n" + u.getTransfer2().toStringNOOS(dm));
						}
					} else {
						textArea.append("\nNo amalgams");
					}

				} else {
					textArea.append("\nWrong number of parameters");
				}
			} else if (method.toLowerCase().equals("disintegration")) {
				textArea.append("\nNot implemented yet");
				return;
			} else if (method.toLowerCase().equals("termvisualization")) {
				if (param.length == 1) {
					JFrame frame;
					frame = FTVisualizer.newWindow("FTVisualizer demo", 640, 480, f1, dm, true, true);
					frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					CloseMacAction.addMacCloseBinding(frame);

					frame.setVisible(true);
					textArea.append("\nLoaded visualization for case " + f1.getName().toString());
				} else {
					textArea.append("\nUnavaliable operation for 2 terms");
				}
				return;
			} else {
				textArea.append("\nUnavaliable operation");
				return;
			}

		} else {
			textArea.append("\nIncorrect sentence. Type 'help'");
			return;
		}

	};

	/**
	 * Random selection.
	 * 
	 * @param allCases
	 *            the all cases
	 * @param percentage
	 *            the percentage
	 * @return the list
	 */
	private List<FeatureTerm> randomSelection(List<FeatureTerm> allCases, int percentage) {
		List<FeatureTerm> ret = new ArrayList<FeatureTerm>();
		int nSamplesNeeded = (allCases.size() * percentage) / 100;
		int i = 0;
		int nLeft = allCases.size();
		Random r = new Random();
		while (nSamplesNeeded > 0) {
			int rand = r.nextInt(nLeft);
			if (rand < nSamplesNeeded) {
				ret.add(allCases.get(i));
				nSamplesNeeded--;
			}
			nLeft--;
			i++;
		}
		return ret;
	}

	/**
	 * Reload cb.
	 * 
	 * @param CB
	 *            the cB
	 * @param panel
	 *            the panel
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void generalReloadCB(int CB, JPanel panel) throws FeatureTermException, IOException {

		System.gc();
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		this.setEnabled(false);

		terms.clear();
		resetOntology();

		TrainingSetProperties ts = TrainingSetUtils.loadTrainingSet(CB, o, dm, case_base);
		terms = ts.cases;

		pan = null;
		panel.removeAll();
		generalPanelFT.removeAll();

		// System.out.println("Training set " + ts.name + " tiene " + ts.cases.size());
		pan = new CBVisualizer((int) panel.getSize().getWidth(), (int) panel.getSize().getHeight(), terms, dm, ts.solution_path,
				ts.description_path, true, this, ts, true);

		// termPane = new JScrollPane(pan);
		// panel.add(termPane);
		// termPane.revalidate();
		// termPane.repaint();

		panel.add(pan);
		panel.revalidate();
		panel.setVisible(true);

		setCursor(null);
		this.setCursor(null);
	}

	/**
	 * Reload ft.
	 * 
	 * @param t
	 *            the t
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public void generalReloadFT(FeatureTerm t) throws FeatureTermException {
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		writeConsole("Case " + t.getName());
		log.info("Case " + t.getName());
		FTVisualizer ftr = new FTVisualizer(generalPanelFT.getHeight(), generalPanelFT.getWidth(), t, dm, true, true);
		// JScrollPane termPane = new JScrollPane(ftr);
		generalPanelFT.removeAll();
		generalPanelFT.add(ftr);
		generalPanelFT.revalidate();
		// ftr.zoomToFit(ftr);
		setCursor(null);

	}

	/**
	 * Reload tree.
	 * 
	 * @param dmList
	 *            the dm list
	 * @param panel
	 *            the panel
	 * @param operationList
	 *            the operation list
	 */
	protected void operationsReloadTree(final DemoFileList dmList, JPanel panel, final JList operationList) {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Tests");
		DefaultTreeModel modelo = new DefaultTreeModel(root);
		DefaultMutableTreeNode currentDomain;
		DefaultMutableTreeNode currentNode;

		// Create demo tree
		HashSet<String> domainList = new HashSet<String>();
		for (Demo element : dmList.demoList) {
			DefaultMutableTreeNode n = searchNode(element.domain, root);
			if (n == null) {
				currentDomain = new DefaultMutableTreeNode(element.domain);
				modelo.insertNodeInto(currentDomain, root, root.getChildCount());
				domainList.add(element.domain);
			} else {
				currentDomain = n;
			}
			currentNode = new DefaultMutableTreeNode(Long.toString(element.nid));
			modelo.insertNodeInto(currentNode, currentDomain, currentDomain.getChildCount());
		}

		operationsDemoListTree = new JTree(modelo);
		expandAll(treeTests);

		operationsDatasetTest.setViewportView(operationsDemoListTree);
		operationsDatasetTest.revalidate();
		operationsDatasetTest.setVisible(true);
		panel.revalidate();
		panel.setVisible(true);

		operationsDemoListTree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				try {
					dm = new FTKBase();
					o = new Ontology();
					operationsPanelFTResult.removeAll();
					operationsTreeClicked(dmList, (String) operationList.getSelectedValue());
				} catch (FeatureTermException e) {
					e.printStackTrace();
					writeConsole("Error: " + e);
					log.error("Error: " + e);
					JOptionPane.showMessageDialog(new JFrame(), e, "Error", JOptionPane.ERROR_MESSAGE);
				} catch (IOException e) {
					e.printStackTrace();
					writeConsole("Error: " + e);
					log.error("Error: " + e);
					JOptionPane.showMessageDialog(new JFrame(), e, "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
	}

	/**
	 * Reset result panel.
	 */
	private void resetResultPanel() {
		splitPane1FT.removeAll();
		// panelsOpTab.repaint();
		panelDemoFT1.removeAll();
		panelDemoFT1.repaint();
		panelDemoFT2.removeAll();
		panelDemoFT2.repaint();
		operationsPanelFTResult.removeAll();
		operationsPanelFTResult.repaint();
		btnNext.setEnabled(false);
		btnPrevious.setEnabled(false);
		resultCount.setText("");
	}

	/**
	 * Rules enabler.
	 * 
	 * @param e
	 *            the e
	 */
	private void rulesEnabler(boolean e) {
		ruleIndex.setEnabled(e);
		ruleTotal.setEnabled(e);
		rulePrevious.setEnabled(e);
		ruleNext.setEnabled(e);
		return;
	}

	/**
	 * Load results list.
	 *
	 * @param panelFTResult the panel ft result
	 * @param results the results
	 * @throws FeatureTermException the feature term exception
	 */
	private void loadResultsList(final JPanel panelFTResult, List<FeatureTerm> results) throws FeatureTermException {
		loadFT(results.get(0), panelFTResult);
		resultCount.setText("1 of " + results.size());
		// Desactivar botones en ausencia de m�s resultados
		if (results.size() > 1) {
			btnNext.setEnabled(true);
		}
	}

	/**
	 * Run btn listener.
	 * 
	 * @param domainListOperations
	 *            the domain list operations
	 * @param runBtn
	 *            the run btn
	 */
	private void operationsRunBtnListener(final JList domainListOperations, JButton runBtn) {
		runBtn.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (operationsRunBtn.isEnabled()) {
					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					// get operation
					String selected = domainListOperations.getSelectedValue().toString();
					if ((panelDemoFT1.getComponentCount() + panelDemoFT2.getComponentCount()) == 2) {

						try {
							// dm = new FTKBase();
							// o = new Ontology();
							//
							if (selected.equals("Subsumption")) {
								// initializeDefaultDM();
								FeatureTerm f1;
								FeatureTerm f2;
								if (cDemo != null) {
									f1 = getFtFromNoosString(cDemo.ft1);
									f2 = getFtFromNoosString(cDemo.ft2);
								} else {
									f1 = operationsDemoFT1;
									f2 = operationsDemoFT2;
								}

								boolean result = f1.subsumes(f2);

								String resString;
								if (result) {
									resString = "Feature Term 1 SUBSUMES Feature Term 2";
									writeConsole(resString);
									log.info(resString);
								} else {
									resString = "Feature Term 1 DOES NOT SUBSUME Feature Term 2";
									writeConsole(resString);
									log.info(resString);
								}

								JTextArea textArea_2 = new JTextArea(resString);
								textArea_2.setEditable(false);
								operationsPanelFTResult.add(textArea_2);
								operationsPanelFTResult.revalidate();

							}
							if (selected.equals("Unification")) {
								// initializeDefaultDM();

								FeatureTerm f1;
								FeatureTerm f2;
								if (cDemo != null) {
									f1 = getFtFromNoosString(cDemo.ft1);
									f2 = getFtFromNoosString(cDemo.ft2);
								} else {
									f1 = operationsDemoFT1;
									f2 = operationsDemoFT2;
								}

								List<FeatureTerm> unifications = FTUnification.unification(f1, f2, dm);
								resultList = unifications;

								if (unifications != null && !unifications.isEmpty()) {
									loadResultsList(operationsPanelFTResult, unifications);
									resultIndex = 0;
								}

							}
							if (selected.equals("Antiunification")) {
								// initializeDefaultDM();

								FeatureTerm f1;
								FeatureTerm f2;
								if (cDemo != null) {
									f1 = getFtFromNoosString(cDemo.ft1);
									f2 = getFtFromNoosString(cDemo.ft2);
								} else {
									f1 = operationsDemoFT1;
									f2 = operationsDemoFT2;
								}

								List<FeatureTerm> objects = new LinkedList<FeatureTerm>();

								objects.add(f1);
								objects.add(f2);

								List<FeatureTerm> antiunifications = FTAntiunification.antiunification(objects, 0, null, o, dm, true, 1);
								resultList = antiunifications;

								if (antiunifications != null && !antiunifications.isEmpty()) {
									loadResultsList(operationsPanelFTResult, antiunifications);
									resultIndex = 0;
								}

							}

							if (selected.equals("Amalgam")) {
								String dName = "";
								int tsCode = 0;
								if (cDemo != null) {
									dName = cDemo.domain;
									tsCode = ontologyList.get(dName);
								} else {
									TreePath path = treeDemos.getSelectionPath();
									tsCode = ontologyList.get(path.getPathComponent(1).toString());
								}
								resetOntology();

								FeatureTerm f1;
								FeatureTerm f2;
								if (cDemo != null) {
									f1 = getFtFromNoosString(cDemo.ft1);
									f2 = getFtFromNoosString(cDemo.ft2);
								} else {
									f1 = operationsDemoFT1;
									f2 = operationsDemoFT2;
								}

								TrainingSetProperties ts = TrainingSetUtils.loadTrainingSet(tsCode, o, dm, case_base);
								if (cDemo != null) {
									f1 = ts.getCaseByName(cDemo.ft1);
									f2 = ts.getCaseByName(cDemo.ft2);
								} else {
									f1 = operationsDemoFT1;
									f2 = operationsDemoFT2;
								}
								AmalgamEvaluationFunction ef = new FTEFCompactness();
								List<AmalgamResult> amalgams = Amalgam.amalgamRefinementsGreedy(f1, f2, ef, o, dm);

								System.out.println("SIZE: " + amalgams.size());
								if (amalgams != null && !amalgams.isEmpty()) {
									resultList = new LinkedList<FeatureTerm>();
									for (AmalgamResult u : amalgams) {
										// System.out.println(u.getAmalgam().toStringNOOS(dm));
										// u.getEvaluation();
										// u.getTransfer1().toStringNOOS(dm);
										// u.getTransfer2().toStringNOOS(dm);
										resultList.add(u.getAmalgam());
									}
									loadResultsList(operationsPanelFTResult, resultList);
									resultIndex = 0;
								}
							}

							if (!selected.equals("Subsumption")) {
								String dName = "";
								if (cDemo != null) {
									dName = cDemo.domain;
								} else {
									dName = treeDemos.getSelectionPath().toString();

								}

								if (dName != null && resultList != null) {
									String message = selected + " demo " + dName + " has " + resultList.size() + " result/s";
									writeConsole(message);
									log.info(message);
								} else {
									String message = selected + " demo has no results";
									writeConsole(message);
									log.info(message);
								}
							}
						} catch (FeatureTermException e) {
							e.printStackTrace();
							writeConsole("Error: " + e);
							log.error("Error: " + e);
						} catch (IOException e) {
							e.printStackTrace();
							writeConsole("Error: " + e);
							log.error("Error: " + e);
						}
					} else if (splitPane1FT.getComponentCount() > 0) {
						if (selected.equals("Disintegration")) {
							try {

								// dm = new FTKBase();
								// o = new Ontology();
								// initializeDefaultDM();

								int tsCode = 0;
								String selectedFT = new String();
								if (cDemo != null) {
									String dName = cDemo.domain;
									tsCode = ontologyList.get(dName);
									selectedFT = cDemo.ft1;
								} else {
									TreePath path = treeDemos.getSelectionPath();
									tsCode = ontologyList.get(path.getPathComponent(1).toString());
									selectedFT = path.getPathComponent(2).toString();
								}

								TrainingSetProperties ts = TrainingSetUtils.loadTrainingSet(tsCode, o, dm, case_base);
								FeatureTerm c = ts.getCaseByName(selectedFT);
								FeatureTerm description = c.readPath(ts.description_path);

								// List<Pair<FeatureTerm, FeatureTerm>> disintegrationTrace =
								// Disintegration.disintegrateWithTrace(description, dm, o);
								// resultList = Disintegration.disintegrate(description, dm, o);
								resultList = Disintegration.disintegrateFast(description, dm, o);
								PropertiesVisualizer frame = new PropertiesVisualizer(1, 1, resultList, dm, true);
								frame.setVisible(true);
								operationsPanelFTResult.removeAll();
								operationsPanelFTResult.add(frame);
								operationsPanelFTResult.revalidate();
								operationsPanelFTResult.repaint();

								setCursor(null);

								// if (resultList != null && !resultList.isEmpty()) {
								// loadResultsList(panelFTResult, resultList);
								// resultIndex = 0;
								// }
							} catch (FeatureTermException e) {
								log.error("Error: " + e);
								e.printStackTrace();
							} catch (IOException e) {
								log.error("Error: " + e);
								e.printStackTrace();
							}

						}

					} else {
						JOptionPane.showMessageDialog(null, "Please select a demo first");
					}
					setCursor(null);
				}

			}
		});
	}

	/**
	 * This method takes the node string and traverses the tree till it finds the node matching the string. If the match
	 * is found the node is returned else null is returned
	 * 
	 * @param nodeStr
	 *            node string to search for
	 * @param root
	 *            the root
	 * @return tree node
	 */
	public DefaultMutableTreeNode searchNode(String nodeStr, DefaultMutableTreeNode root) {
		DefaultMutableTreeNode node = null;

		// Get the enumeration
		Enumeration<?> enumw = root.children();

		// iterate through the enumeration
		while (enumw.hasMoreElements()) {
			// get the node
			node = (DefaultMutableTreeNode) enumw.nextElement();

			// match the string with the user-object of the node
			if (nodeStr.equals(node.getUserObject().toString())) {
				// tree node with string found
				return node;
			}
		}

		// tree node with string node found return null
		return null;
	}

	/**
	 * Sets the pan.
	 * 
	 * @param pan
	 *            the pan to set
	 */
	public void setPan(CBVisualizer pan) {
		this.pan = pan;
	}

	/**
	 * Sets the up ontology list.
	 */
	public void generalSetUpOntologyList() {
		TreeMap<String, Integer> ontoMap = new TreeMap<String, Integer>();
		ontoMap.put("ARTIFICIAL_DATASET", 0);
		ontoMap.put("ZOOLOGY_DATASET", 1);
		ontoMap.put("SOYBEAN_DATASET", 2);
		ontoMap.put("DEMOSPONGIAE_503_DATASET", 3);
		ontoMap.put("DEMOSPONGIAE_280_DATASET", 4);
		ontoMap.put("DEMOSPONGIAE_120_DATASET", 5);
		ontoMap.put("TRAINS_DATASET", 6);
		ontoMap.put("TRAINS_82_DATASET", 61);
		ontoMap.put("TRAINS_900_DATASET", 62);
		// ontoMap.put("TRAINS_100_DATASET ", 63);
		// ontoMap.put("TRAINS_1000_DATASET ", 64);
		// ontoMap.put("TRAINS_10000_DATASET ", 65);
		// ontoMap.put("TRAINS_100000_DATASET ", 66);
		ontoMap.put("UNCLE_DATASET", 7);
		ontoMap.put("UNCLE_DATASET_SETS", 8);
		ontoMap.put("UNCLE_DATASET_BOTH", 9);
		ontoMap.put("CARS_DATASET", 10);
		ontoMap.put("TOXICOLOGY_DATASET_MRATS ", 11);
		ontoMap.put("TOXICOLOGY_DATASET_FRATS ", 12);
		ontoMap.put("TOXICOLOGY_DATASET_MMICE ", 13);
		ontoMap.put("TOXICOLOGY_DATASET_FMICE ", 14);
		// ontoMap.put("TOXICOLOGY_OLD_DATASET_MRATS ", 15);
		// ontoMap.put("TOXICOLOGY_OLD_DATASET_FRATS ", 16);
		// ontoMap.put("TOXICOLOGY_OLD_DATASET_MMICE ", 17);
		// ontoMap.put("TOXICOLOGY_OLD_DATASET_FMICE ", 18);
		// ontoMap.put("KR_VS_KP_DATASET", 19);
		ontoMap.put("FINANCIAL ", 20);
		ontoMap.put("FINANCIAL_NO_TRANSACTIONS", 21);
		ontoMap.put("MUTAGENESIS", 22);
		ontoMap.put("MUTAGENESIS_EASY", 23);
		ontoMap.put("MUTAGENESIS_DISCRETIZED", 24);
		ontoMap.put("MUTAGENESIS_EASY_DISCRETIZED", 25);
		ontoMap.put("MUTAGENESIS_NOL_DISCRETIZED", 26);
		ontoMap.put("MUTAGENESIS_EASY_NOL_DISCRETIZED", 27);
		ontoMap.put("RIU_STORIES", 28);

		ontologyList = ontoMap;

	}

	/**
	 * Tree clicked.
	 * 
	 * @param dmList
	 *            the dm list
	 * @param selection
	 *            the selection
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	protected void operationsTreeClicked(DemoFileList dmList, String selection) throws FeatureTermException, IOException {
		resetResultPanel();

		// Comprobar si es hoja
		TreePath path = operationsDemoListTree.getSelectionPath();
		if (path != null) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
			if (node.isLeaf()) {
				// get node id
				Demo demo = dmList.getDemoById(Long.valueOf(node.toString()));
				CardLayout cl = (CardLayout) panelsOpTab.getLayout();
				operationsRunBtn.setEnabled(true);
				if (selection.equals("Disintegration")) {
					resetOntology();

					cl.last(panelsOpTab);
					String dName = (String) demo.domain;
					int tsCode = ontologyList.get(dName);
					TrainingSetProperties ts = TrainingSetUtils.loadTrainingSet(tsCode, o, dm, case_base);
					FeatureTerm c = ts.getCaseByName(demo.ft1);
					loadFT(c, splitPane1FT);
				} else if (selection.equals("Amalgam")) {
					resetOntology();

					cl.first(panelsOpTab);
					String dName = (String) demo.domain;
					int tsCode = ontologyList.get(dName);
					TrainingSetProperties ts = TrainingSetUtils.loadTrainingSet(tsCode, o, dm, case_base);
					FeatureTerm ft1 = ts.getCaseByName(demo.ft1);
					FeatureTerm ft2 = ts.getCaseByName(demo.ft2);
					loadFT(ft1, panelDemoFT1);
					loadFT(ft2, panelDemoFT2);
				} else {
					initializeDefaultDM("NOOS");
					cl.first(panelsOpTab);
					loadFT(demo.ft1, panelDemoFT1);
					loadFT(demo.ft2, panelDemoFT2);
				}
				cDemo = demo;
				// (node.getParent());
			} else {// is Dataset
				operationsRunBtn.setEnabled(false);
			}
		}
	}

	/**
	 * Tree listener.
	 * 
	 * @param domainListOperations
	 *            the domain list operations
	 * @param dmList
	 *            the dm list
	 */
	private void treeListener(final JList domainListOperations, final DemoFileList dmList) {
		treeTests.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				try {
					boolean ft1;
					// ft1 = domainListOperations.getSelectedValue().equals("Disintegration");
					operationsTreeClicked(dmList, (String) domainListOperations.getSelectedValue());
				} catch (FeatureTermException e) {
					e.printStackTrace();
					writeConsole("Error: " + e);
					log.error("Error: " + e);
				} catch (IOException e) {
					e.printStackTrace();
					writeConsole("Error: " + e);
					log.error("Error: " + e);
				}
			}
		});
	}

	/**
	 * Open webpage.
	 *
	 * @param uri the uri
	 */
	public static void openWebpage(URI uri) {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(uri);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Open webpage.
	 *
	 * @param url the url
	 */
	public static void openWebpage(URL url) {
		try {
			openWebpage(url.toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sets the url.
	 *
	 * @param url the url
	 * @return the url
	 */
	public URL setUrl(String url) {
		URL urlToReturn = null;
		try {
			urlToReturn = new URL(url);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return urlToReturn;
	}
}
