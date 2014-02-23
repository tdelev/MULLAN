/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mulan.concrete_builders;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
	private int numPartitions;
	private DistanceFunction distanceFunction;
	private String linkType;
	public final static String SINGLE = "SINGLE";
	public final static String COMPLETE = "COMPLETE";
	public final static String AVERAGE = "COMPLETE";
	public final static String MEAN = "COMPLETE";
	public final static String CENTROID = "COMPLETE";
	public final static String WARD = "COMPLETE";
	public final static String ADJCOMPLETE = "COMPLETE";
	public final static String NEIGHBOR_JOINING = "COMPLETE";

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
		this(2, CHEBYSHEV_DISTANCE, SINGLE);
	}

	AgglomerativeHierarchicalBuilder(int numPartitions,
			DistanceFunction distanceFunction, String linkType) {
		super(numPartitions, null);
		this.numPartitions = numPartitions;
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
		if (numPartitions > mlData.getNumLabels()) {
			throw new IllegalArgumentException(
					"Number of labels is smaller than the number of partitions");
		}
		Set<String> setOfLabels = mlData.getLabelsMetaData().getLabelNames();
		List<String> listOfLabels = new ArrayList<String>();
		for (String label : setOfLabels) {
			listOfLabels.add(label);
		}
		ArrayList<String>[] childrenLabels = clustering(numPartitions,
				listOfLabels, mlData);

		LabelsMetaDataImpl metaData = new LabelsMetaDataImpl();
		for (int i = 0; i < numPartitions; i++) {
			if (childrenLabels[i].isEmpty()) {
				continue;
			}
			if (childrenLabels[i].size() == 1) {
				metaData.addRootNode(new LabelNodeImpl(childrenLabels[i].get(0)));
				continue;
			}
			if (childrenLabels[i].size() > 1) {
				LabelNodeImpl metaLabel = new LabelNodeImpl("MetaLabel "
						+ (i + 1));
				createLabelsMetaDataRecursive(metaLabel, childrenLabels[i],
						mlData);
				metaData.addRootNode(metaLabel);
			}
		}

		return metaData;
	}

	private ArrayList<String>[] clustering(int clusters, List<String> labels,
			MultiLabelInstances mlData) {
		ArrayList<String>[] childrenLabels = new ArrayList[clusters];
		for (int i = 0; i < clusters; i++) {
			childrenLabels[i] = new ArrayList<String>();
		}

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
		HierarchicalClusterer hc = new HierarchicalClusterer();
		try {
			hc.setNumClusters(clusters);
			System.out.println("hierarchical clustering");
			hc.setDistanceFunction(distanceFunction);
			hc.setLinkType(new SelectedTag(linkType,
					HierarchicalClusterer.TAGS_LINK_TYPE));

			hc.buildClusterer(transposed);
			// return the clustering
			for (int i = 0; i < labels.size(); i++) {
				int labelIndex = hc.clusterInstance(transposed.instance(i));
				childrenLabels[labelIndex].add(labels.get(i));
			}
		} catch (Exception e) {
			Logger.getLogger(HierarchyBuilder.class.getName()).log(
					Level.SEVERE, null, e);
		}
		return childrenLabels;
	}

	private void createLabelsMetaDataRecursive(LabelNodeImpl node,
			List<String> labels, MultiLabelInstances mlData) {
		if (labels.size() <= numPartitions) {
			for (int i = 0; i < labels.size(); i++) {
				LabelNodeImpl child = new LabelNodeImpl(labels.get(i));
				node.addChildNode(child);
			}
			return;
		}
		ArrayList<String>[] childrenLabels = clustering(numPartitions, labels,
				mlData);
		for (int i = 0; i < numPartitions; i++) {
			if (childrenLabels[i].isEmpty()) {
				continue;
			}
			if (childrenLabels[i].size() == 1) {
				LabelNodeImpl child = new LabelNodeImpl(
						childrenLabels[i].get(0));
				node.addChildNode(child);
				continue;
			}
			if (childrenLabels[i].size() > 1) {
				LabelNodeImpl child = new LabelNodeImpl(node.getName() + "."
						+ (i + 1));
				node.addChildNode(child);
				createLabelsMetaDataRecursive(child, childrenLabels[i], mlData);
			}
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
		for (LabelNode rootLabel : metaData.getRootLabels()) {
			Element newLabelElem = labelsXMLDoc.createElement("label");
			newLabelElem.setAttribute("name", rootLabel.getName());
			appendElement(newLabelElem, rootLabel);
			rootElement.appendChild(newLabelElem);
		}
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
			newLabelElem.setAttribute("name", childNode.getName());
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
