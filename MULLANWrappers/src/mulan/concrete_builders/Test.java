package mulan.concrete_builders;

import mulan.data.LabelNode;
import mulan.data.LabelsMetaData;
import mulan.data.MultiLabelInstances;
import mulan.data.converter.FileIO;
import mulan.data.converter.UtilsXML;

import org.w3c.dom.Document;

import weka.core.DistanceFunction;

public class Test {
	public static void main(String[] args) throws Exception {
		if (args.length < 5) {
			System.out
					.println("USAGE: [data_file_name] [xml_file_name] [distance 1-4] [link_type 1-8]");
			System.out.println("Distance function:");
			System.out.println("1 Euclidean");
			System.out.println("2 Chebyshev");
			System.out.println("3 Manhattan");
			System.out.println("4 Minkowski");
			System.out.println("Link type");
			System.out.println("1 Single");
			System.out.println("2 Complete");
			System.out.println("3 Avarage");
			System.out.println("4 Mean");
			System.out.println("5 Centroid");
			System.out.println("6 Ward");
			System.out.println("7 Adjcomplete");
			System.out.println("8 Neighbor joining");
			return;
		}
		String dataFileName = args[0];
		String xmlFileName = args[1];
		int distance = Integer.parseInt(args[2]);
		int link = Integer.parseInt(args[3]);
		String outFile = args[4];
		DistanceFunction distanceFunction = AgglomerativeHierarchicalBuilder.EUCLIDEAN_DISTANCE;
		if (distance == 2) {
			distanceFunction = AgglomerativeHierarchicalBuilder.CHEBYSHEV_DISTANCE;
		} else if (distance == 3) {
			distanceFunction = AgglomerativeHierarchicalBuilder.MANHATTAN_DISTANCE;
		} else if (distance == 4) {
			distanceFunction = AgglomerativeHierarchicalBuilder.MINKOWSKI_DISTANCE;
		}
		String linkType = AgglomerativeHierarchicalBuilder.SINGLE;
		if (link == 2) {
			linkType = AgglomerativeHierarchicalBuilder.COMPLETE;
		} else if (link == 3) {
			linkType = AgglomerativeHierarchicalBuilder.AVERAGE;
		} else if (link == 4) {
			linkType = AgglomerativeHierarchicalBuilder.MEAN;
		} else if (link == 5) {
			linkType = AgglomerativeHierarchicalBuilder.CENTROID;
		} else if (link == 6) {
			linkType = AgglomerativeHierarchicalBuilder.WARD;
		} else if (link == 7) {
			linkType = AgglomerativeHierarchicalBuilder.ADJCOMPLETE;
		} else if (link == 8) {
			linkType = AgglomerativeHierarchicalBuilder.NEIGHBOR_JOINING;
		}
		Document xml_file = FileIO.loadFromFile(xmlFileName);
		MultiLabelInstances dataset = new MultiLabelInstances(dataFileName,
				UtilsXML.numChildNodes(xml_file));
		AgglomerativeHierarchicalBuilder ahb = new AgglomerativeHierarchicalBuilder(
				distanceFunction, linkType);
		LabelsMetaData lmd = ahb.buildLabelHierarchy(dataset);
		ahb.createXMLFile(lmd);
		ahb.saveToXMLFile(outFile);
		//printLMD(lmd);
		System.out.println("Done.");
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
