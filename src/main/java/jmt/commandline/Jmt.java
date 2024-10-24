/**
 * Copyright (C) 2016, Laboratorio di Valutazione delle Prestazioni - Politecnico di Milano

 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 */

package jmt.commandline;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import jmt.jmva.analytical.solvers.dispatchers.SolverDispatcher;
import jmt.common.exception.InputDataException;
import jmt.common.exception.SolverException;
import jmt.engine.simDispatcher.DispatcherJSIMschema;
import jmt.gui.common.xml.XMLArchiver;
import jmt.gui.jsimgraph.mainGui.JSIMGraphMain;
import jmt.gui.jsimwiz.JSIMWizMain;

import jmt.jmva.gui.JMVAWizard;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class Jmt {

	private static final String OPTION_SEED = "-seed";
	private static final String OPTION_MAXTIME = "-maxtime";

	public static void printDocument(Document doc, OutputStream out) throws IOException, TransformerException {
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

		transformer.transform(new DOMSource(doc), 
				new StreamResult(new OutputStreamWriter(out, "UTF-8")));
	}

	public static void help() {
		System.err.println("Usage: jmt.commandline.Jmt [mva|sim|jsimw|jsimg] [modelfilename] [options]");
		System.err.println("mva options:");
		System.err.println("  <none>");
		System.err.println("sim options:");
		System.err.println("  -seed 1234 : sets the simulation seed to 1234");
		System.err.println("  -maxtime 60 : sets the maximum simulation time to 60 seconds");
		System.err.println("jsimw options:");
		System.err.println("  <none>");
		System.err.println("jsimg options:");
		System.err.println("  <none>");
		System.exit(0);
	}

	public static void copyFile(File sourceFile, File destFile) throws IOException {
		if (!destFile.exists()) {
			destFile.createNewFile();
		}

		FileChannel source = null;
		FileChannel destination = null;
		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();

			//previous code: destination.transferFrom(source, 0, source.size());
			//should be to avoid infinite loops.

			long count = 0;
			long size = source.size();
			while ((count += destination.transferFrom(source, 0, size - count)) < size);
		} finally {
			if (source != null) {
				source.close();
			}
			if (destination != null) {
				destination.close();
			}
		}
	}

	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			help();
		}
		File model = new File(args[1]);
		if (!model.isFile()) {
			System.err.print("Invalid model file: " + model.getAbsolutePath());
			System.exit(1);
		}
		if (args[0].equals("mva")) {
			File result = new File(args[1] + "-result.jmva");
			mva(model, result);
		} else if (args[0].equals("sim")) {
			File result = new File(args[1] + "-result.jsim");
			Map<String, String> options = parseParameters(args, 2);
			sim(model, result, options);
		} else if (args[0].equals("jsimw")) {
			JSIMWizMain.main(new String[] { args[1] });
		} else if (args[0].equals("jsimg")) {
			JSIMGraphMain.main(new String[] { args[1] });
		} else if (args[0].equals("jmva")) {
			new JMVAWizard(args[1]);
		} else {
			help();
		}
	}

	public static void mva(File model, File result) throws InputDataException, SolverException, IOException {
		SolverDispatcher dispatcher = new SolverDispatcher();
		// Starts the solution
		dispatcher.solve(model);
		copyFile(model, result);
	}

	public static boolean sim(File model, File result) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();

		File temp = File.createTempFile("tempfileSim", ".jsim");
		temp.deleteOnExit();

		Document doc = db.parse(model);									// Convert the XML doc into structured elements.
		Element sim = XMLArchiver.getSimFromArchiveDocument(doc);		// Extract the element and its sub-elements enclosed with tag <sim></sim>

		Document doc2 = db.newDocument();
		Node dup = doc2.importNode(sim, true);
		NamedNodeMap attributes = dup.getAttributes();
		attributes.removeNamedItem("xsi:noNamespaceSchemaLocation");
		doc2.appendChild(dup);

		/*
		 * save to a temp file
		 */
		TransformerFactory tranFactory = TransformerFactory.newInstance();
		Transformer aTransformer = tranFactory.newTransformer();
		Source src = new DOMSource(doc2);
		Result dest = new StreamResult(temp);
		aTransformer.transform(src, dest);

		DispatcherJSIMschema dispatcher = new DispatcherJSIMschema(temp);
		dispatcher.setTerminalSimulation(true);

		// Starts the simulation
		boolean success = dispatcher.solveModel();
		File output = dispatcher.getOutputFile();
		copyFile(output, result);
		output.delete();
		return success;
	}

	public static boolean sim(File model, File result, Map<String, String> options) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();

		File temp = File.createTempFile("tempfileSim", ".jsim");
		temp.deleteOnExit();

		Document doc = db.parse(model);
		Element sim = XMLArchiver.getSimFromArchiveDocument(doc);

		Document doc2 = db.newDocument();
		Node dup = doc2.importNode(sim, true);
		NamedNodeMap attributes = dup.getAttributes();
		attributes.removeNamedItem("xsi:noNamespaceSchemaLocation");
		doc2.appendChild(dup);

		/*
		 * save to a temp file
		 */
		TransformerFactory tranFactory = TransformerFactory.newInstance();
		Transformer aTransformer = tranFactory.newTransformer();
		Source src = new DOMSource(doc2);
		Result dest = new StreamResult(temp);
		aTransformer.transform(src, dest);

		DispatcherJSIMschema dispatcher = new DispatcherJSIMschema(temp);
		dispatcher.setTerminalSimulation(true);
		// Sets simulation seed if required
		if (options.containsKey(OPTION_SEED)) {
			try {
				dispatcher.setSimulationSeed(Long.parseLong(options.get(OPTION_SEED)));
			} catch (NumberFormatException ex) {
				System.err.println("Invalid simulation seed. Should be a number.");
				System.exit(1);
			}
		}

		if (options.containsKey(OPTION_MAXTIME)) {
			try {
				dispatcher.setSimulationMaxDuration(Long.parseLong(options.get(OPTION_MAXTIME)) * 1000);
			} catch (NumberFormatException ex) {
				System.err.println("Invalid maximum simulation time. Should be a number.");
				System.exit(1);
			}
		}

		// Starts the simulation
		boolean success = dispatcher.solveModel();
		File output = dispatcher.getOutputFile();
		copyFile(output, result);
		output.delete();
		return success;
	}

	/**
	 * Returns a map with option name as key and list of parameters as value. Parameter without options are saved with "" as key
	 * @param args command line args
	 * @param startIndex the first index to consider in params
	 * @return a map with option as key and 
	 */
	public static Map<String, String> parseParameters(String[] args, int startIndex) {
		Map<String, String> options = new HashMap<String, String>();
		// Parses command line options
		String option = "";
		for (int i = startIndex; i < args.length; i++) {
			String str = args[i];
			if (str.startsWith("-")) {
				option = str.toLowerCase();
				options.put(option, null);
			} else {
				options.put(option, str);
				// Reset options field.
				option = "";
			}
		}
		return options;
	}

}
