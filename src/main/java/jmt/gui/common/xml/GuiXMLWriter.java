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

package jmt.gui.common.xml;

import java.io.File;
import java.io.OutputStream;
import java.util.*;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import jmt.gui.common.definitions.CommonModel;
import jmt.gui.common.definitions.parametric.ParametricAnalysisChecker;
import jmt.gui.common.definitions.parametric.ParametricAnalysisDefinition;
import jmt.gui.common.definitions.parametric.ParametricAnalysisModelFactory;
import jmt.gui.jsimgraph.definitions.JSimGraphModel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * <p>Title: Gui XML Writer</p>
 * <p>Description: Writes JModel GUI specific information to an XML file. This
 * class provides methods for model save.</p>
 * 
 * @author Bertoli Marco
 *         Date: 22-lug-2005
 *         Time: 15.54.48
 */
public class GuiXMLWriter implements GuiXMLConstants {

	/**
	 * Writes gui model informations into an XML file
	 * @param fileName name of the file to be created
	 * @param model data structure
	 */
	public static void writeXML(String fileName, CommonModel model) {
		writeToResult(new StreamResult(new File(fileName)), model);
	}

	/**
	 * Writes gui model informations into an XML file
	 * @param xmlFile handler to the file to be created
	 * @param model data structure
	 */
	public static void writeXML(File xmlFile, CommonModel model) {
		writeToResult(new StreamResult(xmlFile), model);
	}

	/**
	 * Writes gui model informations into an XML file
	 * @param out stream where XML should be written
	 * @param model data structure
	 */
	public static void writeXML(OutputStream out, CommonModel model) {
		writeToResult(new StreamResult(out), model);
	}

	/**
	 * Helper method used to call transformer to build up an XML file from a Document
	 * @param result result where created xml should be put
	 * @param model data structure
	 */
	private static void writeToResult(Result result, CommonModel model) {
		Document modelDoc = getDocument(model);
		if (modelDoc == null) {
			return;
		}
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(new DOMSource(modelDoc), result);
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the entire Document representing GUI data structure.
	 * @param model data structure
	 * @return complete GUI data structure in Document format
	 */
	public static Document getDocument(CommonModel model) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = null;
		try {
			docBuilder = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}
		Document modelDoc = docBuilder.newDocument();
		// Writes all elements on Document
		writeGuiInfos(modelDoc, model);
		return modelDoc;
	}

	/**
	 * Creates root element and adds schema informations to it
	 * @param guiDoc document root
	 * @param model data structure
	 */
	static protected void writeGuiInfos(Document guiDoc, CommonModel model) {
		Element elem = guiDoc.createElement(XML_DOCUMENT_ROOT);
		guiDoc.appendChild(elem);
		elem.setAttribute("xsi:noNamespaceSchemaLocation", XML_DOCUMENT_XSD);
		elem.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		if (model instanceof JSimGraphModel) {
			writeClasses(guiDoc, elem, (JSimGraphModel) model);
			writeStations(guiDoc, elem, (JSimGraphModel) model);
			writeConnectionShapes(guiDoc, elem, (JSimGraphModel) model);
		}
		writeParametricAnalysisInfos(guiDoc, elem, model);
	}

	/**
	 * Write informations for user Classes (name and color)
	 * @param doc document root
	 * @param guiNode parent node where class informations should be added
	 * @param model data structure
	 */
	static protected void writeClasses(Document doc, Node guiNode, JSimGraphModel model) {
		for (Object classKey : model.getClassKeys()) {
			// Now transforms Class color in hexadecimal (Web like) representation
			String classColor = "#" + Integer.toHexString(model.getClassColor(classKey).getRGB()).toUpperCase();
			Element userClass = doc.createElement(XML_E_CLASS);
			userClass.setAttribute(XML_A_CLASS_NAME, model.getClassName(classKey));
			userClass.setAttribute(XML_A_CLASS_COLOR, classColor);
			guiNode.appendChild(userClass);
		}
	}

	/**
	 * Write informations for stations (name and position)
	 * @param doc document root
	 * @param guiNode parent node where station informations should be added
	 * @param model data structure
	 */
	static protected void writeStations(Document doc, Node guiNode, JSimGraphModel model) {
		Vector<Object> stations = model.getStationKeys();
		Element station, position;
		for (int i = 0; i < stations.size(); i++) {
			Object stationKey = stations.get(i);
			station = doc.createElement(XML_E_STATION);
			station.setAttribute(XML_A_STATION_NAME, model.getStationName(stations.get(i)));
			position = doc.createElement(XML_E_POSITION);
			position.setAttribute(XML_A_POSITION_X, String.valueOf(model.getStationPosition(stationKey).getX()));
			position.setAttribute(XML_A_POSITION_Y, String.valueOf(model.getStationPosition(stationKey).getY()));
			position.setAttribute(XML_A_POSITION_ROTATE, String.valueOf(model.getStationPosition(stationKey).isRotate()));
			position.setAttribute(XML_A_POSITION_ANGLE, String.valueOf(model.getStationPosition(stationKey).getAngle()));
			station.appendChild(position);
			guiNode.appendChild(station);
		}
	}

	/**
	 * Write informations for connection shapes
	 * @param doc document root
	 * @param guiNode parent node where station informations should be added
	 * @param model data structure
	 */
	static protected void writeConnectionShapes(Document doc, Node guiNode, JSimGraphModel model) {
		Vector<Object> stations = model.getStationKeys();
		Element connectionshape, arc, sourceArc, targetArc, controlPointSource, controlPointTarget;
		Integer arcsNb;
		// For each pair of station, we test if a connection shape has been defined
		for (int i = 0; i < stations.size(); i++) {
			for (int j = 0; j < stations.size(); j++) {
				if (model.hasConnectionShape(stations.get(i), stations.get(j))) {
					//if a connection shape has been defined for the pair of stations, we insert the connection shape
					//in the document
					connectionshape = doc.createElement(XML_E_CONNECTION_SHAPE);
					connectionshape.setAttribute(XML_A_CONNECTION_SOURCE, model.getStationName(stations.get(i)));
					connectionshape.setAttribute(XML_A_CONNECTION_TARGET, model.getStationName(stations.get(j)));

					//Then we get the number of arcs
					arcsNb = model.getConnectionShape(stations.get(i), stations.get(j)).getArcsNb();
					for (int k = 0; k < arcsNb; k++) {
						arc = doc.createElement(XML_E_ARC);
						sourceArc = doc.createElement(XML_E_SOURCE);
						controlPointSource = doc.createElement(XML_E_POINT);
						controlPointTarget = doc.createElement(XML_E_POINT);
						targetArc = doc.createElement(XML_E_TARGET);

						sourceArc.setAttribute(XML_A_SOURCE_X, String.valueOf(model.getConnectionShape(stations.get(i), stations.get(j)).getArc(k).getSource().getX()));
						sourceArc.setAttribute(XML_A_SOURCE_Y, String.valueOf(model.getConnectionShape(stations.get(i), stations.get(j)).getArc(k).getSource().getY()));
						controlPointSource.setAttribute(XML_A_POINT_X, String.valueOf(model.getConnectionShape(stations.get(i), stations.get(j)).getArc(k).getArcPoints().get(0).getX()));
						controlPointSource.setAttribute(XML_A_POINT_Y, String.valueOf(model.getConnectionShape(stations.get(i), stations.get(j)).getArc(k).getArcPoints().get(0).getY()));
						controlPointTarget.setAttribute(XML_A_POINT_X, String.valueOf(model.getConnectionShape(stations.get(i), stations.get(j)).getArc(k).getArcPoints().get(1).getX()));
						controlPointTarget.setAttribute(XML_A_POINT_Y, String.valueOf(model.getConnectionShape(stations.get(i), stations.get(j)).getArc(k).getArcPoints().get(1).getY()));
						targetArc.setAttribute(XML_A_TARGET_X, String.valueOf(model.getConnectionShape(stations.get(i), stations.get(j)).getArc(k).getTarget().getX()));
						targetArc.setAttribute(XML_A_TARGET_Y, String.valueOf(model.getConnectionShape(stations.get(i), stations.get(j)).getArc(k).getTarget().getY()));

						connectionshape.appendChild(arc);
						arc.appendChild(sourceArc);
						arc.appendChild(controlPointSource);
						arc.appendChild(controlPointTarget);
						arc.appendChild(targetArc);
					}
					guiNode.appendChild(connectionshape);
				}
			}
		}
	}

	/**
	 *  Write informations about parametric analysis. If the parametric analysis model
	 *  is no more consistent with the simulation model the user will be asked to choose
	 *  about trying to automatically correct it
	 * @param doc document root
	 * @param guiNode parent node where station informations should be added
	 * @param model data structure
	 */
	static void writeParametricAnalysisInfos(Document doc, Node guiNode, CommonModel model) {
		Element parametric;
		if (model.isParametricAnalysisEnabled()) {
			ParametricAnalysisDefinition pad = model.getParametricAnalysisModel();
			int result = pad.checkCorrectness(false);
			//if the parametric analysis model is not consistent with the model..
			if (result != 0) {
				int choice = JOptionPane.showConfirmDialog(null,
						"The parametric analysis model is not consistent with the simulation model, try to change it?", "JMT - Warning",
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				//if user chooses ok..
				if (choice == 0) {
					//if the old PAModel could not be corrected..
					if (result == 2) {
						ParametricAnalysisChecker checker = new ParametricAnalysisChecker(model, model, model);
						//if there are no PA simulation available, disable parametric analysis
						//and return
						if (!checker.canBeEnabled()) {
							parametric = doc.createElement(XML_E_PARAMETRIC);
							parametric.setAttribute(XML_A_PARAMETRIC_ENABLED, "false");
							return;
						}
						//else set the first available
						else {
							String availableModel = checker.getRunnableParametricAnalysis()[0];
							pad = ParametricAnalysisModelFactory.createParametricAnalysisModel(availableModel, model, model, model);
						}
					}
					//else the PAModel can be corrected, so correct it
					else {
						pad.checkCorrectness(true);
					}
				}
			}
			//save parametric analysis configuration
			Map<String, String> properties = pad.getProperties();
			Set<String> keys = properties.keySet();
			parametric = doc.createElement(XML_E_PARAMETRIC);
			parametric.setAttribute(XML_A_PARAMETRIC_ENABLED, Boolean.toString(model.isParametricAnalysisEnabled()));
			parametric.setAttribute(XML_A_PARAMETRIC_CLASSPATH, pad.getClass().toString().replaceFirst("class ", ""));
			Iterator<String> it = keys.iterator();
			while (it.hasNext()) {
				Object thisParam = it.next();
				Element field = doc.createElement(XML_E_FIELD);
				field.setAttribute(XML_A_FIELD_NAME, thisParam.toString());
				field.setAttribute(XML_A_FIELD_VALUE, properties.get(thisParam).toString());
				parametric.appendChild(field);
			}
			guiNode.appendChild(parametric);
		}
		//parametric analysis is not available, set it
		else {
			parametric = doc.createElement(XML_E_PARAMETRIC);
			parametric.setAttribute(XML_A_PARAMETRIC_ENABLED, "false");
		}
	}

}
