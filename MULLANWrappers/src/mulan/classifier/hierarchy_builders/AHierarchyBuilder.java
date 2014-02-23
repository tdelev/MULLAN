package mulan.classifier.hierarchy_builders;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import weka.core.Instances;
import weka.core.converters.ArffSaver;

import mulan.data.LabelNode;
import mulan.data.LabelsMetaData;
import mulan.data.MultiLabelInstances;

public abstract class AHierarchyBuilder {
	
	public abstract AHierarchyBuilder createProduct(String... params)
			throws InvalidParameterException;

	public abstract MultiLabelInstances buildHierarchy(
			MultiLabelInstances mlData) throws Exception;

	public abstract LabelsMetaData buildLabelHierarchy(
			MultiLabelInstances mlData) throws Exception;
	
}
