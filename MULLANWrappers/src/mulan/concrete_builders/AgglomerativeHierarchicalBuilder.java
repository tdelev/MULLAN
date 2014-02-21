/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mulan.concrete_builders;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
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
import mulan.classifier.meta.ConstrainedKMeans;
import mulan.classifier.meta.HierarchyBuilder;
import static mulan.classifier.meta.HierarchyBuilder.Method.BalancedClustering;
import static mulan.classifier.meta.HierarchyBuilder.Method.Clustering;
import mulan.data.DataUtils;
import mulan.data.InvalidDataFormatException;
import mulan.data.LabelNode;
import mulan.data.LabelNodeImpl;
import mulan.data.LabelsMetaData;
import mulan.data.LabelsMetaDataImpl;
import mulan.data.MultiLabelInstances;
import org.w3c.dom.Element;
import weka.clusterers.EM;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

/**
 *
 * @author Gore
 */
public class AgglomerativeHierarchicalBuilder extends HierarchyBuilder{

    AgglomerativeHierarchicalBuilder(){
        super(0, null);
    }
    
    
    
    public MultiLabelInstances buildHierarchy(MultiLabelInstances mlData) throws Exception {
        LabelsMetaData labelsMetaData = buildLabelHierarchy(mlData);
        return HierarchyBuilder.createHierarchicalDataset(mlData, labelsMetaData);
    }

    /**
     * Builds a hierarhy of labels on top of the labels of a flat multi-label
     * dataset, by recursively partitioning the labels into a specified number
     * of partitions.
     *
     * @param mlData the multiLabel data on with the new hierarchy will be built
     * @return a hierarchy of labels
     * @throws java.lang.Exception
     */
    public LabelsMetaData buildLabelHierarchy(MultiLabelInstances mlData) throws Exception {
        LabelsMetaDataImpl metaData = new LabelsMetaDataImpl();

        

        return metaData;
    }

    

    
/*
    private ArrayList<String>[] clustering(int clusters, List<String> labels, MultiLabelInstances mlData, boolean balanced) {
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
            for (int k=0; k<labelIndices.length; k++) {
                if (mlData.getDataSet().attribute(labelIndices[k]).name().equals(labels.get(i))) {
                    index = labelIndices[k];
                }
            }
            double[] values = new double[numInstances];
            for (int j = 0; j < numInstances; j++) {
                values[j] = mlData.getDataSet().instance(j).value(index);
            }
            Instance newInstance = DataUtils.createInstance(mlData.getDataSet().instance(0), 1, values);
            transposed.add(newInstance);
        }

        if (!balanced) {
            EM clusterer = new EM();
            try {
                // cluster the labels
                clusterer.setNumClusters(clusters);
                System.out.println("clustering");
                clusterer.buildClusterer(transposed);
                // return the clustering
                for (int i = 0; i < labels.size(); i++) {
                    childrenLabels[clusterer.clusterInstance(transposed.instance(i))].add(labels.get(i));
                }
            } catch (Exception ex) {
                Logger.getLogger(HierarchyBuilder.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            ConstrainedKMeans clusterer = new ConstrainedKMeans();
            try {
                // cluster the labels
                clusterer.setMaxIterations(20);
                clusterer.setNumClusters(clusters);
                System.out.println("balanced clustering");
                clusterer.buildClusterer(transposed);
                // return the clustering
                for (int i = 0; i < labels.size(); i++) {
                    childrenLabels[clusterer.clusterInstance(transposed.instance(i))].add(labels.get(i));
                }
            } catch (Exception ex) {
                Logger.getLogger(HierarchyBuilder.class.getName()).log(Level.SEVERE, null, ex);
            }
        }


        return childrenLabels;
    }

    private ArrayList<String>[] randomPartitioning(int partitions, List<String> labels) {
        ArrayList<String>[] childrenLabels = new ArrayList[partitions];
        for (int i = 0; i < partitions; i++) {
            childrenLabels[i] = new ArrayList<String>();
        }

        Random rnd = new Random();
        while (!labels.isEmpty()) {
            for (int i = 0; i < partitions; i++) {
                if (labels.isEmpty()) {
                    break;
                }
                String rndLabel = labels.remove(rnd.nextInt(labels.size()));
                childrenLabels[i].add(rndLabel);
            }
        }
        return childrenLabels;
    }
*/
    
}
