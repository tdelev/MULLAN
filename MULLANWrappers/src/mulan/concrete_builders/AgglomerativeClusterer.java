package mulan.concrete_builders;

import weka.clusterers.HierarchicalClusterer;
import weka.core.DistanceFunction;
import weka.core.Instances;
import weka.core.SelectedTag;

public class AgglomerativeClusterer extends HierarchicalClusterer {
	public AgglomerativeClusterer(DistanceFunction distanceFunction, String linkType) {
		super();
		setNumClusters(1);
		setDistanceFunction(distanceFunction);
		new SelectedTag(linkType,
				HierarchicalClusterer.TAGS_LINK_TYPE);
	}
	
	public Node getRoot() {
		return m_clusters[0];
	}
	
	public Node[] getClusters() {
		return m_clusters;
	}
	
	public Instances getInstances() {
		return m_instances;
	}
}
