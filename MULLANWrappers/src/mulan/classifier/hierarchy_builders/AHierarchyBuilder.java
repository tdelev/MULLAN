package mulan.classifier.hierarchy_builders;

import java.security.InvalidParameterException;

import mulan.data.LabelsMetaData;
import mulan.data.MultiLabelInstances;

public abstract class AHierarchyBuilder {
	
	public abstract AHierarchyBuilder createProduct( String... params ) throws InvalidParameterException;
	
	public abstract MultiLabelInstances buildHierarchy(MultiLabelInstances mlData) throws Exception;
        
        public abstract LabelsMetaData buildLabelHierarchy(MultiLabelInstances mlData) throws Exception;
	
}
