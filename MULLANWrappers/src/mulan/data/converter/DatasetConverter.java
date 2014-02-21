package mulan.data.converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mulan.data.LabelNode;
import mulan.data.LabelsMetaData;
import mulan.data.LabelsMetaDataImpl;
import mulan.data.MultiLabelInstances;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;
import weka.core.UnsupportedClassTypeException;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;

public class DatasetConverter {
	/**
	 * map for the paths in the tree
	 */
	private Map<String, String> pathsInTree;
	/**
	 *  set containing all the Paths
	 */
	private Set<String> allPathsNode;
	/**
	 *  we cache the index_of_the-first_class label for later usage
	 */
	private int index_of_first_class_label;
	
	/**
	 * we use this to do the conversion
	 */
	private LabelsMetaDataImpl labels_meta_data;
	
	/**
	 * @return the labels_meta_data
	 */
	public LabelsMetaData getLabels_meta_data() {
		return labels_meta_data;
	}

	/**
	 * Performs a deep copy of the Instances then appends a new String
	 * attribute. For every instance in the dataset calculates the value for the
	 * new attribute and sets it accordingly. Only works with Sparse and Dense
	 * instances.
	 * @param instances - the dataset we will expand by adding a hierarchical attribute
	 * @return a new dataset that has a hierarchical attribute
	 */
	private Instances copyInstancesAndAppendHierarhicalAttribute ( Instances instances ) {
		// copy existing attributes
		ArrayList<Attribute> atts = new ArrayList<Attribute>(
				index_of_first_class_label);
		for (int i = 0; i < index_of_first_class_label; i++) {
			atts.add(instances.attribute(i));
		}
		List<String> labelValues = null;
		// generate the hierarchical attribute & add it to the dataset
		String paths = "";
		for (String path : allPathsNode) {
			if (!paths.isEmpty())
				paths += "," + path;
			else
				paths = path;
		}
		Attribute hierarchical_attribute = new Attribute("class hierarchical "
				+ paths, labelValues);
		atts.add(hierarchical_attribute);
		// create the new dataset & set its attributes
		Instances result = new Instances("hierarchical", atts,
				instances.numInstances());
		// copy over all the instances from the old dataset, whilst appending
		// the value for the new attribute
		for (int i = 0; i < instances.numInstances(); i++) {
			try {
				Instance instance = instances.instance(i);
				String hierarchicalLabel = generateMetaLabel(instance);
				Instance new_instance = copyInstanceAndAppendEmptyAttributeSlot(
						instance, index_of_first_class_label);
				new_instance.setDataset(result);
				new_instance
						.setValue(hierarchical_attribute, hierarchicalLabel);
				result.add(new_instance);
			} catch (UnsupportedClassTypeException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * Generates a meta label for a given instance. The meta label is in CLUS
	 * specific format. The meta label encapsulates information from all the
	 * relevant labels of this instance.
	 * @param instance - a instance for which we generate the label
	 * @return the generated meta label
	 */
	private String generateMetaLabel(Instance instance) {
		String result = "";
		for (int i = instance.numValues() - 1; i >= 0
				&& instance.index(i) >= index_of_first_class_label; --i) {
			if (instance.value(instance.attributeSparse(i)) != 0)
				if (i != instance.numValues() - 1) {
					result += "@"
							+ pathsInTree.get(instance.attributeSparse(i)
									.name());
				} else {
					result = pathsInTree
							.get(instance.attributeSparse(i).name());
				}
		}
		if (result.isEmpty())
			result = "all";
		return result;
	}
	
	/**
	 * Performs a deep copy of the instance and then appends a new empty
	 * attribute slot at the end. Only works with sparse and dense instances
	 * from weka. This functions ignores all the attributes (&their values)
	 * which have an index greater then max_index.
	 * @param instance - the instance copied
	 * @param max_index - all attributes who have indices greater than this number are
	 *            ignored, and will not be included in the returned instances
	 * @return a deep copy of the instance containing one more empty attribute
	 *         value slot
	 * @throws UnsupportedClassTypeException - if the Instance parameter is not of type weka.core.SparseInstance or weka.core.DenseInstance
	 */
	private Instance copyInstanceAndAppendEmptyAttributeSlot(Instance instance,
			int max_index) throws UnsupportedClassTypeException {
		Instance result;
		// we need to watch out for different things when copying sparse and
		// dense instances
		if (instance instanceof SparseInstance) {
			// CLUS doesn't seem to support Sparse
			// ARFF file formats, so this code will not be used, instead we will transform all SparseInstance`s to DenseInstance`s
			/**
				 //count the relevant labels and use only them 
				 ArrayList<Integer>relevant_indices = new ArrayList<Integer>(instance.numValues());
				 for ( int k = 0 ; k < instance.numValues() ; ++k ) 
				 	if ( instance.index(k) < max_index ) relevant_indices.add(k);  
				 //copy all the relevant info into the new arrays double
				 double new_instance_att_values[] = new double[relevant_indices.size()];
				 int new_instance_att_indices[] = new int[relevant_indices.size()]; 
				 int c = 0;for ( int k : relevant_indices ) { 
				 	new_instance_att_values[c] = instance.valueSparse(k);
				 	new_instance_att_indices[c++] = instance.index(k); 
				 } 
				 //set the index for the last attribute
				 new_instance_att_indices[new_instance_att_indices.length-1] = max_index; 
				 //create the sparse instance 
				 result = new SparseInstance(instance.weight(),new_instance_att_values,new_instance_att_indices,new_instance_att_indices.length+1);
			**/
			//count the relevant labels and use only them 
			
			ArrayList<Integer> relevant_indices = new ArrayList<Integer>(
					instance.numValues());
			for (int k = 0; k < instance.numValues(); ++k) {
				if (instance.index(k) < max_index)
					relevant_indices.add(k);
			}
			// copy all the relevant info into the new arrays
			double new_instance_att_values[] = new double[max_index + 1];
			for (int k : relevant_indices) {
				new_instance_att_values[instance.index(k)] = instance
						.valueSparse(k);
			}
			// set the index for the last attribute
			// create the dense instance
			result = new DenseInstance(instance.weight(),
					new_instance_att_values);
		} else if (instance instanceof DenseInstance) {
			// copy all relevant values to a new array
			double new_instance_att_values[] = Arrays.copyOf(
					instance.toDoubleArray(), max_index + 1);
			// create the dense instance
			result = new DenseInstance(instance.weight(),
					new_instance_att_values);
		} else {
			throw new UnsupportedClassTypeException(
					"Can only work with classes: "
							+ DenseInstance.class.toString() + " and "
							+ SparseInstance.class.toString());
		}
		return result;
	}
	
	
        
        /**
	 * Replaces all missing values in the dataset by their means and modes.
	 * @param dataset - the dataset we want to replace the missing values on
	 * @throws Exception - when???
	 */
	/*
        private void replaceMissingValues(MultiLabelInstances dataset)
			throws Exception {
		ReplaceMissingValues replaceMissingFilter = new ReplaceMissingValues();
		replaceMissingFilter.setInputFormat(dataset.getDataSet());
		dataset.setDataSet(Filter.useFilter(dataset.getDataSet(),
				replaceMissingFilter));
	}
        */
        
	/**
	 * Generates all the paths in the hierarchy, and saves them to the class variables:
	 * 	  pathsInTree
	 *    allPathsNode
	 */
	private void generatePaths(LabelsMetaDataImpl metaLabels ) {
		pathsInTree = (Map<String, String>) GeneratePathsInHierarchy(metaLabels);
		allPathsNode = GenerateAllPathsNode(metaLabels);
	}

	/**
	 * Generates a Set<String> that contains all the possible paths in the class hierarchy.
	 * The hierarchy is contained in metaLabels.
	 * @param metaLabels - the description for the class labels hierarchy
	 * @return a set with all possible paths in the hierarchy
	 */
	private Set<String> GenerateAllPathsNode(LabelsMetaDataImpl metaLabels) {
		Set<String> paths = new HashSet<String>();
		paths.add("all");
		for (LabelNode node : metaLabels.getRootLabels())
			recursivePathsNode(node, paths,
					"all/" + node.getName().replaceAll(" ", ""));
		return paths;
	}

	/**
	 * Recursively iterates over all the children for a given node and adds,
	 * their paths to the allPathsNode set.
	 * @param momentNode - the current node we want to add
	 * @param paths - the set of paths found so far, we add new paths here
	 * @param momentPath - the path we took to get to the moment node
	 */
	private void recursivePathsNode(LabelNode momentNode, Set<String> paths,
			String momentPath) {
		paths.add(momentPath);
		for (LabelNode nodeChild : momentNode.getChildren())
			recursivePathsNode(nodeChild, paths, momentPath + "/"
					+ nodeChild.getName().replaceAll(" ", ""));
	}
	
	/**
	 * Generates a Map<String,String> that contains for keys class labels
	 * and for values the path to get to that class label. 
	 * The key-value pairs in the map are built on the data in the metaLabels herarchy.
	 * @param metaLabels - the description for the class labels hierarchy
	 * @return a map with key-value pairs being leaf-class labels and their respective paths both as Strings
	 */
	private Map<String, String> GeneratePathsInHierarchy(
			LabelsMetaDataImpl metaLabels) {
		Map<String, String> paths = new HashMap<String, String>();
		for (LabelNode node : metaLabels.getRootLabels())
			recursivePaths(node, paths,
					"all/" + node.getName().replaceAll(" ", ""));
		return paths;
	}

	/**
	 * Recursively iterates over all the children nodes for a given node and
	 * maps their paths in the pathsInTree map parameter.
	 * Only leaf nodes are mapped.
	 * @param momentNode - the current node we are processing
	 * @param paths - the set of paths found so far, we add new paths here
	 * @param momentPath - the path we took to get to the moment node
	 */
	private void recursivePaths(LabelNode momentNode, Map<String, String> paths,
			String momentPath) {
		if (!momentNode.hasChildren())
			paths.put(momentNode.getName(), momentPath);
		else
			for (LabelNode nodeChild : momentNode.getChildren())
				recursivePaths(nodeChild, paths, momentPath + "/"
						+ nodeChild.getName().replaceAll(" ", ""));
	}
	
	public Instances convertMulanToClus ( MultiLabelInstances mulan_dataset ) throws Exception {
		//replaceMissingValues(mulan_dataset);
		index_of_first_class_label = mulan_dataset.getDataSet().numAttributes()-mulan_dataset.getNumLabels();
		generatePaths(labels_meta_data);
		return copyInstancesAndAppendHierarhicalAttribute(mulan_dataset.getDataSet());
	}

	public DatasetConverter( LabelsMetaDataImpl labels_meta_data ) {
		this.labels_meta_data = labels_meta_data;
	}

}
