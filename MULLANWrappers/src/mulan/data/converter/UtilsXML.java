package mulan.data.converter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import mulan.data.LabelNode;
import mulan.data.LabelNodeImpl;
import mulan.data.LabelsMetaData;
import mulan.data.LabelsMetaDataImpl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class UtilsXML {
	
	/**
	 * Counts the exact number of child Nodes (xml elements).
	 * Child node is a node that contains no other nodes (elements).
	 * @param xml_doc - the document representing the xml
	 * @return a number of the child nodes, 0 if there aren't any
	 */
	public  static int numChildNodes(Document xml_doc) {
		int result = 0;
		NodeList list_children = xml_doc.getDocumentElement().getChildNodes();
		for ( int i = 1 ; i < list_children.getLength() ; i += 2 ) {
			result += countChildNodes(list_children.item(i));
		}
		return result;
	}

	private static int countChildNodes(Node item) {
		if ( item.hasChildNodes() ) {
			int result = 0;
			NodeList list_children = item.getChildNodes();
			for ( int i = 1 ; i < list_children.getLength() ; i += 2 ) {
				result += countChildNodes(list_children.item(i));
			}
			return result;
		}
		return 1;
	}
	
	/**
	 * Transforms the class label hierarchy into a valid xml format.
	 * @param metaData - contains the class label hierarchy
	 * @return a Document with a valid xml representation of the class label hierarchy in metaData
	 * @throws Exception - when ??
	 */
	public static Document createXMLFile(LabelsMetaData metaData) throws Exception {
		// create an empty XML document
		DocumentBuilderFactory docBF = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBF.newDocumentBuilder();
		Document labelsXMLDoc = docBuilder.newDocument();
		// create the root element of the xml
		Element rootElement = labelsXMLDoc.createElement("labels");
		rootElement
				.setAttribute("xmlns", "http://mulan.sourceforge.net/labels");
		labelsXMLDoc.appendChild(rootElement);
		// we add all the root labels from the hierarchy
		for (LabelNode rootLabel : metaData.getRootLabels()) {
			rootElement.appendChild(appendNode(rootLabel, labelsXMLDoc));
		}
		return labelsXMLDoc;
	}
	
	/**
	 * Recursively appends the labelsXMLDoc and all its children to the labelNode.
	 * @param labelsXMLDoc - the that gets added to the node
	 * @param labelNode - we add the document to this node
	 * @return the LabelNode that corresponds to the labelsXMLDoc
	 */
	private static LabelNode appendNode(Node labelsXMLDoc) {
		LabelNodeImpl result = new LabelNodeImpl(labelsXMLDoc.getAttributes().item(0).getNodeValue());
		NodeList list_children = labelsXMLDoc.getChildNodes();
		for ( int i = 1 ; i < list_children.getLength() ; i += 2 ) {
			result.addChildNode(appendNode(list_children.item(i)));
		}
		return result;
	}

	/**
	 * Recursively appends the labelNode and all its children to the document.
	 * @param labelNode - the node that gets added to the document
	 * @param labelsXMLDoc - we add the node to this document
	 * @return the Element in xml document that corresponds to the added labelNode
	 */
	private static Element appendNode(LabelNode labelNode, Document labelsXMLDoc) {
		// we create the element itself
		Element resLabelElem = labelsXMLDoc.createElement("label");
		resLabelElem.setAttribute("name", labelNode.getName());
		// we then proceed to adding all its children
		for (LabelNode childNode : labelNode.getChildren()) {
			Element newLabelElem = labelsXMLDoc.createElement("label");
			newLabelElem.setAttribute("name", childNode.getName());
			resLabelElem.appendChild(appendNode(childNode, labelsXMLDoc));
		}
		return resLabelElem;
	}
	
	/**
	 * Transforms the valid xml format into a class label hierarchy.
	 * @param metaData - contains the valid .xml format
	 * @return a class label hierarchy
	 * @throws Exception - when ??
	 */
	public static LabelsMetaDataImpl createLabelsMetaData(Document xml_doc) throws Exception {
		LabelsMetaDataImpl result = new LabelsMetaDataImpl();
		Element root_element = xml_doc.getDocumentElement();
		NodeList list_children = root_element.getChildNodes();
		for ( int i = 1 ; i < list_children.getLength() ; i += 2 ) {
			result.addRootNode(appendNode(list_children.item(i)));
		}
		return result;
	}
	

}
