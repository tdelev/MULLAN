package mulan.data.converter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import weka.core.Instances;
import weka.core.converters.ArffSaver;

public class FileIO {

	// we use this temp file because of inconsistencies between the weka's arff
	// format and CLUS
	// check the cleanUp function for more details
	private static String temp_file_name = "you_are_dreaming";

	/**
	 * Loads a xml file into its java representation org.w3c.Document.
	 * 
	 * @param xml_file
	 *            - the name of the file
	 * @return - the org.w3c.Document representation of the xml file
	 * @throws ParserConfigurationException
	 *             - errors in the configuration files for the parser
	 * @throws SAXException
	 *             - errors in the xml structure
	 * @throws IOException
	 *             - if the file can't be located or read
	 */
	public static Document loadFromFile(String xml_file)
			throws ParserConfigurationException, SAXException, IOException {
		File fXmlFile = new File(xml_file);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		doc.getDocumentElement().normalize();
		return doc;
	}

	/**
	 * Takes an xml document and saves it to HD. It does some formating.
	 * 
	 * @param xmlName
	 *            - the name under which the file will be saved on HD
	 * @param labelsXMLDoc
	 *            - the xml document to be saved
	 */
	public static void saveToXMLFile(String xml_file_name, Document document) {
		Source source = new DOMSource(document);
		File xmlFile = new File(xml_file_name + ".xml");
		StreamResult result = new StreamResult(xmlFile);
		try {
			Transformer transformer = TransformerFactory.newInstance()
					.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(
					"{http://xml.apache.org/xslt}indent-amount", "4");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.transform(source, result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * When the dataset is stored in .arff format it causes errors with the
	 * CLUS, probably because of different Weka versions. The following troubles
	 * the CLUS: any attributes that in their names contain the char ';' the
	 * definition of the hierarchical attribute
	 * 
	 * @attribute 'class hierarchical values_separated_by_comas' string ^
	 *            ^^^^^^^^ all these need to be removed. There are probably even
	 *            more errors not yet found.
	 * @param file_name
	 *            - the name of the file we are altering
	 * @throws IOException
	 */
	private static void cleanUpFile(String file_name) throws IOException {
		try {
			// copy back the content from the temp file
			BufferedReader in = new BufferedReader(new FileReader(
					temp_file_name + ".arff"));
			BufferedWriter out = new BufferedWriter(new FileWriter(file_name));
			String line;
			while ((line = in.readLine()) != null) {
				// do the regex replecement
				if (line.contains("@attribute")) {
					if (line.contains("'class hierarchical")) {
						line = line.replaceAll("' string", "");
						line = line.replaceAll("'", "");
					}
					line = line.replaceAll(";", "SEMI_COLON");
				} else {
					/*
					 * if ( was_sparse ) { StringBuilder n_line = new
					 * StringBuilder(); int i = 0; int last_number = 0; boolean
					 * in_number = false; boolean attribute_index_next = true;
					 * while ( i < line.length() ) { if ( !in_number ) { if (
					 * Character.isDigit(line.charAt(i)) ) { if (
					 * line.charAt(i-1) == ',' || line.charAt(i-1) == ' ' ||
					 * line.charAt(i-1) == '{') in_number = true; } } if (
					 * Character.isDigit(line.charAt(i)) && in_number ) {
					 * last_number *= 10; last_number += line.charAt(i)-48; }
					 * else { if ( in_number ) { in_number = false; if (
					 * attribute_index_next )++last_number; attribute_index_next
					 * = !attribute_index_next; n_line.append(last_number);
					 * last_number = 0; } n_line.append(line.charAt(i)); } ++i;
					 * } line = n_line.toString(); }
					 */
				}

				out.write(line.toString() + "\n");
			}
			in.close();
			out.flush();
			out.close();
			File file = new File(temp_file_name + ".arff");
			file.delete();
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	/**
	 * Saves a dataset as an .arff file.
	 * 
	 * @param arffName
	 *            - the name for the file
	 * @param dataSet
	 *            - the dataset we want to export as an arff file
	 * @throws IOException
	 *             - When there is a problem, creating or writing to the file.
	 */
	public static void saveDataSet(String arffName, Instances dataSet)
			throws IOException {
		ArffSaver saver = new ArffSaver();
		saver.setInstances(dataSet);
		saver.setFile(new File(temp_file_name + ".arff"));
		saver.writeBatch();
		cleanUpFile(arffName + ".arff");
	}

}
