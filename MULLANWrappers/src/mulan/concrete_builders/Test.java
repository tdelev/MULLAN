package mulan.concrete_builders;

import mulan.classifier.hierarchy_builders.ClusteringHierarchyBuilder;
import mulan.classifier.hierarchy_builders.RandomHierarchyBuilder;
import mulan.data.LabelNode;
import mulan.data.LabelsMetaData;
import mulan.data.MultiLabelInstances;
import mulan.data.converter.FileIO;
import mulan.data.converter.UtilsXML;

import org.w3c.dom.Document;

public class Test {
	public static void main(String[] args) throws Exception {
		String xmlFileName = "datasets/scene";
		String dataFileName = "datasets/scene";
		Document xml_file = FileIO.loadFromFile(xmlFileName + ".xml");
		MultiLabelInstances dataset = new MultiLabelInstances(dataFileName
				+ ".arff", UtilsXML.numChildNodes(xml_file));
		RandomHierarchyBuilder rhb = new RandomHierarchyBuilder();
		ClusteringHierarchyBuilder chb = new ClusteringHierarchyBuilder();
		AgglomerativeHierarchicalBuilder ahb = new AgglomerativeHierarchicalBuilder();
		// LabelsMetaData lmd = rhb.buildLabelHierarchy(dataset);
		// LabelsMetaData lmd = chb.buildLabelHierarchy(dataset);
		// LabelsMetaData lmd = ahb.buildLabelHierarchy(dataset);
		// printLMD(lmd);
		MultiLabelInstances mli = ahb.buildHierarchyAndSaveFiles(dataset,
				"datasets/result.arff", "datasets/result.xml");
		System.out.println("DONE");
	}

	static void printLMD(LabelsMetaData labelsMetaData) {
		if (labelsMetaData.getNumLabels() > 0) {
			for (LabelNode labelNode : labelsMetaData.getRootLabels()) {
				System.out.println(labelNode.getName());
				printLN(labelNode);
			}
		}
	}

	static void printLN(LabelNode labelNode) {
		System.out.println(labelNode.getName());
		if (labelNode.hasChildren()) {
			for (LabelNode ln : labelNode.getChildren()) {
				printLN(ln);
			}
		}
	}
}
