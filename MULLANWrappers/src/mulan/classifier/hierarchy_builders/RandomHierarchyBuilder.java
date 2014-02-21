package mulan.classifier.hierarchy_builders;

import java.security.InvalidParameterException;

import mulan.classifier.meta.HierarchyBuilder;
import mulan.data.LabelsMetaData;
import mulan.data.MultiLabelInstances;

public class RandomHierarchyBuilder extends AHierarchyBuilder {
	
	private HierarchyBuilder builder;

	public RandomHierarchyBuilder() {
		this(2);
	}
	
	public RandomHierarchyBuilder( int partitions ) {
		builder = new HierarchyBuilder(partitions,HierarchyBuilder.Method.Random); 
	}
	
	@Override
	public AHierarchyBuilder createProduct(String... params) {
		int num_partitions = -1;
		if ( params != null && params.length == 1 ) {
			try {
				num_partitions = Integer.parseInt(params[0]);
			}
			catch ( Exception e ) {
				throw new InvalidParameterException("Expeted was a number parameter at position one.");
			}
		}
		if ( num_partitions == -1 )  return new RandomHierarchyBuilder();
		return new RandomHierarchyBuilder(num_partitions);
	}

	@Override
	public MultiLabelInstances buildHierarchy(MultiLabelInstances mlData)
			throws Exception {
		return builder.buildHierarchy(mlData);
	}
        
        @Override
        public LabelsMetaData buildLabelHierarchy(MultiLabelInstances mlData) throws Exception {
            return builder.buildLabelHierarchy(mlData);
        }

}
