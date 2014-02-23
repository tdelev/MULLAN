package mulan.classifier.hierarchy_builders;

import java.security.InvalidParameterException;

import mulan.classifier.meta.HierarchyBuilder;
import mulan.data.LabelsMetaData;
import mulan.data.MultiLabelInstances;

public class ClusteringHierarchyBuilder extends AHierarchyBuilder {

	private HierarchyBuilder builder;

	private static final String id = "Clustering";

	public ClusteringHierarchyBuilder() {
		this(2);
	}

	public ClusteringHierarchyBuilder(int partitions) {
		builder = new HierarchyBuilder(partitions,
				HierarchyBuilder.Method.BalancedClustering);
	}

	@Override
	public AHierarchyBuilder createProduct(String... params) {
		int num_partitions = -1;
		if (params != null && params.length == 1) {
			try {
				num_partitions = Integer.parseInt(params[0]);
			} catch (Exception e) {
				throw new InvalidParameterException(
						"Expeted was a number parameter at position one.");
			}
		}
		if (num_partitions == -1)
			return new RandomHierarchyBuilder();
		return new ClusteringHierarchyBuilder(num_partitions);
	}

	@Override
	public MultiLabelInstances buildHierarchy(MultiLabelInstances mlData)
			throws Exception {
		return builder.buildHierarchy(mlData);
	}

	@Override
	public LabelsMetaData buildLabelHierarchy(MultiLabelInstances mlData)
			throws Exception {
		return builder.buildLabelHierarchy(mlData);
	}

}
