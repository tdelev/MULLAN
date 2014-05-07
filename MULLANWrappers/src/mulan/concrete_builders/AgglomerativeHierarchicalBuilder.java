/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mulan.concrete_builders;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import mulan.classifier.meta.HierarchyBuilder;
import mulan.data.DataUtils;
import mulan.data.LabelNode;
import mulan.data.LabelNodeImpl;
import mulan.data.LabelsMetaData;
import mulan.data.LabelsMetaDataImpl;
import mulan.data.MultiLabelInstances;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import weka.clusterers.HierarchicalClusterer;
import weka.clusterers.HierarchicalClusterer.Node;
import weka.core.Attribute;
import weka.core.ChebyshevDistance;
import weka.core.DistanceFunction;
import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.ManhattanDistance;
import weka.core.MinkowskiDistance;
import weka.core.SelectedTag;
import weka.core.converters.ArffSaver;

/**
 * 
 * @author Gore
 */
public class AgglomerativeHierarchicalBuilder extends HierarchyBuilder {
	private Document labelsXMLDoc;
	private DistanceFunction distanceFunction;
	private String linkType;
	public final static String SINGLE = "SINGLE";
	public final static String COMPLETE = "COMPLETE";
	public final static String AVERAGE = "AVERAGE";
	public final static String MEAN = "MEAN";
	public final static String CENTROID = "CENTROID";
	public final static String WARD = "WARD";
	public final static String ADJCOMPLETE = "ADJCOMPLETE";
	public final static String NEIGHBOR_JOINING = "NEIGHBOR_JOINING";

	public final static DistanceFunction EUCLIDEAN_DISTANCE;
	public final static DistanceFunction CHEBYSHEV_DISTANCE;
	public final static DistanceFunction MANHATTAN_DISTANCE;
	public final static DistanceFunction MINKOWSKI_DISTANCE;
	static {
		EUCLIDEAN_DISTANCE = new EuclideanDistance();
		MANHATTAN_DISTANCE = new ManhattanDistance();
		CHEBYSHEV_DISTANCE = new ChebyshevDistance();
		MINKOWSKI_DISTANCE = new MinkowskiDistance();
	}

	AgglomerativeHierarchicalBuilder() {
		this(EUCLIDEAN_DISTANCE, SINGLE);
	}

	AgglomerativeHierarchicalBuilder(DistanceFunction distanceFunction,
			String linkType) {
		super(0, null);
		this.distanceFunction = distanceFunction;
		this.linkType = linkType;
	}

	public MultiLabelInstances buildHierarchy(MultiLabelInstances mlData)
			throws Exception {
		LabelsMetaData labelsMetaData = buildLabelHierarchy(mlData);
		return HierarchyBuilder.createHierarchicalDataset(mlData,
				labelsMetaData);
	}

	/**
	 * Builds a hierarhy of labels on top of the labels of a flat multi-label
	 * dataset, by recursively partitioning the labels into a specified number
	 * of partitions.
	 * 
	 * @param mlData
	 *            the multiLabel data on with the new hierarchy will be built
	 * @return a hierarchy of labels
	 * @throws java.lang.Exception
	 */
	public LabelsMetaData buildLabelHierarchy(MultiLabelInstances mlData)
			throws Exception {
		Set<String> setOfLabels = mlData.getLabelsMetaData().getLabelNames();
		List<String> listOfLabels = new ArrayList<String>();
		for (String label : setOfLabels) {
			listOfLabels.add(label);
		}
		LabelsMetaData metaData = clustering(listOfLabels, mlData);
		return metaData;
	}

	private LabelsMetaData clustering(List<String> labels,
			MultiLabelInstances mlData) {
		// transpose data and keep only labels in the parameter list
		int numInstances = mlData.getDataSet().numInstances();
		ArrayList<Attribute> attInfo = new ArrayList<Attribute>(numInstances);
		for (int i = 0; i < numInstances; i++) {
			Attribute att = new Attribute("instance" + (i + 1));
			attInfo.add(att);
		}
		System.out.println(new Date() + " constructing instances");
		Instances transposed = new Instances("transposed", attInfo, 0);
		int[] labelIndices = mlData.getLabelIndices();
		for (int i = 0; i < labels.size(); i++) {
			int index = -1;
			for (int k = 0; k < labelIndices.length; k++) {
				if (mlData.getDataSet().attribute(labelIndices[k]).name()
						.equals(labels.get(i))) {
					index = labelIndices[k];
				}
			}
			double[] values = new double[numInstances];
			for (int j = 0; j < numInstances; j++) {
				values[j] = mlData.getDataSet().instance(j).value(index);
			}
			Instance newInstance = DataUtils.createInstance(mlData.getDataSet()
					.instance(0), 1, values);
			transposed.add(newInstance);
		}
		AgglomerativeClusterer ahc = new AgglomerativeClusterer(
				distanceFunction, linkType);
		try {
			System.out.println("hierarchical clustering");
			ahc.buildClusterer(transposed);
			//printCluster(ahc.getRoot(), ahc, labels, 1);
		} catch (Exception e) {
			Logger.getLogger(HierarchyBuilder.class.getName()).log(
					Level.SEVERE, null, e);
		}
		LabelsMetaDataImpl metaData = new LabelsMetaDataImpl();
		LabelNodeImpl metaLabel = new LabelNodeImpl("MetaLabel 1");
		metaData.addRootNode(metaLabel);
		createLabelsMetaDataRecursive(metaData, metaLabel, labels, ahc.getRoot(), 1);
		return metaData;
	}

	void printCluster(Node node, AgglomerativeClusterer ahc,
			List<String> labels, int level) {
		if (node != null) {
			System.out.println("LEVEL: " + level);
			System.out.println("Left: " + labels.get(node.m_iLeftInstance));
			System.out.println("Right: "
					+ labels.get(node.m_iRightInstance));
			printCluster(node.m_left, ahc, labels, level + 1);
			printCluster(node.m_right, ahc, labels, level + 1);
		}
	}

	private void createLabelsMetaDataRecursive(LabelsMetaDataImpl metaData, LabelNodeImpl node,
			List<String> labels, Node clusterNode, int level) {
		if (clusterNode != null) {
			LabelNodeImpl leftNode = new LabelNodeImpl(node.getName() + "1");
			if(clusterNode.m_left == null) {
				LabelNodeImpl terminalNode = new LabelNodeImpl(labels.get(clusterNode.m_iLeftInstance));
				leftNode.addChildNode(terminalNode);
			}
			//metaData.addRootNode(leftNode);
			LabelNodeImpl rightNode = new LabelNodeImpl(node.getName() + "0");
			if(clusterNode.m_right == null) {
				LabelNodeImpl terminalNode = new LabelNodeImpl(labels.get(clusterNode.m_iRightInstance));
				rightNode.addChildNode(terminalNode);
			}
			//metaData.addRootNode(rightNode);
			createLabelsMetaDataRecursive(metaData, leftNode, labels, clusterNode.m_left,
					level + 1);
			createLabelsMetaDataRecursive(metaData, rightNode, labels,
					clusterNode.m_right, level + 1);
			node.addChildNode(leftNode);
			node.addChildNode(rightNode);
		}
	}

	protected void saveToArffFile(Instances dataSet, File file)
			throws IOException {
		ArffSaver saver = new ArffSaver();
		saver.setInstances(dataSet);
		saver.setFile(file);
		saver.writeBatch();
	}

	protected void createXMLFile(LabelsMetaData metaData) throws Exception {
		DocumentBuilderFactory docBF = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBF.newDocumentBuilder();
		labelsXMLDoc = docBuilder.newDocument();

		Element rootElement = labelsXMLDoc.createElement("labels");
		rootElement
				.setAttribute("xmlns", "http://mulan.sourceforge.net/labels");
		labelsXMLDoc.appendChild(rootElement);
		Set<LabelNode> root = metaData.getRootLabels();
		LabelNode first = root.iterator().next();
		for (LabelNode rootLabel : first.getChildren()) {
			Element newLabelElem = labelsXMLDoc.createElement("label");
			String name = rootLabel.getName();
			newLabelElem.setAttribute("name", binaryToInt(name));
			appendElement(newLabelElem, rootLabel);
			rootElement.appendChild(newLabelElem);
		}
	}
	
	String binaryToInt(String label) {
		if(label.startsWith("MetaLabel")) {
			String number = label.split(" ")[1];
			//long a = Long.parseLong(number, 2);
			BigInteger bi = new BigInteger(number, 2);
			return String.format("MetaLabel %s", bi.toString());
		}
		return label;
	}

	protected void saveToXMLFile(String fileName) {
		Source source = new DOMSource(labelsXMLDoc);
		File xmlFile = new File(fileName);
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

	protected void appendElement(Element labelElem, LabelNode labelNode) {
		for (LabelNode childNode : labelNode.getChildren()) {
			Element newLabelElem = labelsXMLDoc.createElement("label");
			
			newLabelElem.setAttribute("name", binaryToInt(childNode.getName()));
			appendElement(newLabelElem, childNode);
			labelElem.appendChild(newLabelElem);
		}
	}

	public MultiLabelInstances buildHierarchyAndSaveFiles(
			MultiLabelInstances mlData, String arffName, String xmlName)
			throws Exception {
		MultiLabelInstances newData = buildHierarchy(mlData);
		saveToArffFile(newData.getDataSet(), new File(arffName));
		createXMLFile(mlData.getLabelsMetaData());
		saveToXMLFile(xmlName);
		return newData;
	}

}
