/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package mulan.data.converter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import mulan.classifier.hierarchy_builders.AHierarchyBuilderFactory;
import mulan.data.InvalidDataFormatException;
import mulan.data.LabelsMetaDataImpl;
import mulan.data.MultiLabelInstances;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Utils;

/**
 * Converts an original MULAN dataset with HOMER hierarchy to CLUS compatible
 * dataset.
 * 
 * @param destinationFileName
 *            the destination file name (-train.arff and -test.arff)
 * @throws java.lang.Exception
 */
public class ConverterHOMERMULANtoClus implements OptionHandler {
	
	/**
	 * a set containg all the available options for this converter
	 */
	private static Set<Option> available_options;
	
	@Override
	public Enumeration<Option> listOptions() {
		return Collections.enumeration(available_options);
	}
	
	static {
		available_options = new HashSet<Option>();
		available_options.add(new Option("Use an .xml to load an already built class hierarchy.", "existing hierarchy", 1, "-L"));
		available_options.add(new Option("Use an xml to load the class labels. 1st argument: name of the .xml file. Defaulted as input_file_name.", "load class labels from xml", 1, "-X"));
		available_options.add(new Option("Number of class labels directly. 1st argument: number of class labels. The number must be greater then 2 and smaller then total number of attributes in the input file.", "number of class labels", 1, "-N"));
		available_options.add(new Option("Hierarchy builder method to use. 1st argument: hierarchy builder method to use, 2nd argument: the first parametar that should be passed directly to the builder  and so one. You can pass any number of parametars this way (if there are more parameters then the builder expects however you might get an error). Defaulted as Balanced clustering with parameter 2.", "hierarchy builder method", 1, "-M"));
		available_options.add(new Option("Save the hierarchy built by MULAN as an .xml file. 1st argument: the name of the .xml file. Defaulted as input_file_name+\"H\".", "save built hierarchy", 1, "-S"));
		available_options.add(new Option("Convert the input data file by changing all the class labels into one hierarchical class label, and save it as a new data file. 1st argument: the name of the new data file. Defaulted as input_file_name+\"H\".", "convert&output the data file", 1, "-C"));
		available_options.add(new Option("Print all the available options.", "help", 0, "-H"));
		available_options.add(new Option("Use a test file (the hierarchy will be concluded from the train file). 1st argument: the name of the test arff file, defaulted as { input_file_name.contains(\"train\") input_file_name.replaceAll(\"train\",\"test\"); else input_file_name+=\"-test\" } 2nd argument: the name of the converted test file. Defaulted as test_file_name+H", "train file", 2, "-T"));
	}

	/** 
	 * the dataset we are working on (converting)
	 */
	private MultiLabelInstances dataset;
	/**
	 * information about the meta (AND the class) labels
	 */
	private LabelsMetaDataImpl metaLabels;
	/**
	 * the options that determine the effects of executing convert
	 */
	private String options[];
	
	public ConverterHOMERMULANtoClus( ) { /*DEFAULT CONSTRUCTOR*/}
	
	public ConverterHOMERMULANtoClus( String options[] ) {
		this.options = options;
	}
	
	/**
	 * @return the options
	 */
	public String[] getOptions() {
		return options;
	}

	/**
	 * @param options the options to set
	 */
	public void setOptions(String[] options) {
		this.options = options;
	}

	private void loadDatasetFromArffFileValidateAgainstHierarchyXML ( String data_file_name ,String xml_file_name ) throws SAXException, IOException, Exception {
		Document xml_file = FileIO.loadFromFile(xml_file_name + ".xml");
		dataset = new MultiLabelInstances(data_file_name+".arff",UtilsXML.numChildNodes(xml_file));
		metaLabels = UtilsXML.createLabelsMetaData(xml_file);
	}
	
	private void loadDatasetFromArffFileValidateAgainstFlatXML ( String data_file_name , String xml_file_name ) throws InvalidDataFormatException {
		dataset = new MultiLabelInstances(data_file_name+".arff",xml_file_name + ".xml");
	}
	
	private void loadDatasetFromArffFileValidateAgainstNumClassLabels ( String data_file_name , int num_class_labels ) throws InvalidDataFormatException {
		dataset = new MultiLabelInstances(data_file_name+".arff",num_class_labels);	
	}

	/**
	 * Get all the arguments from the arguments array that occur after a specified position, and before another
	 * flag (a flag is a string starting with '-'). 
	 * If the number of expected arguments is larger then the missing arguments will be defaulted as empty strings.
	 * @param args - an array of all the arguments
	 * @param pos - the position before the first possible relevant argument
	 * @param num_args - the expected number of arguments (can be greater then the real number of arguments). Any arguments that are not expected are ignored.
	 * @return an array of string containing the valid arguments that occur after a given position and before another flag
	 * i.e. subarray(args,pos+1,nextFlag(args,pos+1)) - if we assume that nextFlag returns the position of a flag argument
	 * 	                    ^           ^
	 *                    start        end 
	 */
	private String[] getArguments(String[] args, int pos , int num_args) {
		String res[] = new String[num_args];
		for ( int i = 0 ; i < num_args ; ++i ) res[i] = "";
		int i = 0;
		try {
			while ( pos != -1 && args[pos+1].charAt(0) != '-' ) {
				res[i++] = args[++pos];
			}
		}
		catch ( Exception e ) {	}
		return res;
	}
	
	/**
	 * Gets a single argument proceeding a flag at position pos.
	 * If no such argument exists an empty string is returned.
	 * @param arguments - the arguments that we are processing
	 * @param pos - the position of the flag argument
	 * @return a single argument that occurs right after the pos index in the arguments array.
	 * Empty string if such element does not exist.
	 */
	private String getArgument(String[] arguments, int pos ) {
		return getArguments(arguments, pos, 1)[0];
	}
	
	/**
	 * Removes all empty string that occur at the end of a string array.
	 * Actually, it removes all strings in the array that occur after the first empty string in the array.
	 * @param strings - the array of string we are processing 
	 * @return a sub-array of the string parameter that has all empty string from the arguments array cut off.
	 */
	private String[] removeEmptyStrings ( String[] strings ) {
		int count = 0;
		for ( String s : strings ) if ( s != null && !s.isEmpty() ) ++count;
		return Arrays.copyOf(strings,count);
	}
	
	/*
	 */
	public void convert () {
		try {
			boolean help = Utils.getFlag('H', options);
			if ( help ) {
				//print all the options, and usage specifics
				System.out.println("You need to speciffy the input data file as first argument.");
				Enumeration<Option> opts = listOptions();
				System.out.println("All available options follow:");
				System.out.println("                        NAME  FLAG   ARGS   DESCRIPTION  ");
				while ( opts.hasMoreElements() ) {
					Option op = (Option)opts.nextElement();
					System.out.println(
							String.format("%28s %4s  %4s     %s",op.name(),op.synopsis(),op.numArguments(),op.description()));
				}
			}
			else {
				//check if the input_file_name is entered
				String input_file_name;
				try {
					input_file_name = options[0];
					input_file_name.charAt(0);
				}
				catch ( Exception e ) {
					System.out.println("You must speciffy the input data file as first argument. Refer to help for more details.");
					throw new Exception("You must speciffy the input data file as first argument. Refer to help for more details.");
				}
				//check all other options for conflicts
				int load_hier_xml_pos =  Utils.getOptionPos('L', options);
				String load_hier_xml_name = input_file_name+"H";
				
				int load_xml_pos =  Utils.getOptionPos('X', options);
				String load_xml_name = input_file_name;
				
				int num_class_labels_pos = Utils.getOptionPos('N', options);
				int num_class_labels = -1;
				
				int build_method_pos = Utils.getOptionPos('M', options);
				String build_method = "BalancedClustering";
				String params[] = { "2" };
				
				int convert_and_save = Utils.getOptionPos('C', options);
				String output_file_name = input_file_name+"H";
				
				int save_hierarchy_pos = Utils.getOptionPos('S', options);
				String save_hier_xml_name = input_file_name+"H";
				
				int use_test_file_pos = Utils.getOptionPos('T',options);
				String test_input_file = input_file_name.contains("train")?input_file_name.replaceAll("train", "test"):input_file_name+"-test";
				String test_output_file = test_input_file+"H";
				
				if ( load_hier_xml_pos == -1 ) {
					//since there is no hierarchy file provided we will have to build the hierarchy ourselves
					//for that we need either the number of class labels or an xml file that lists all the class labels explicitly, but not both
					if ( load_xml_pos == -1 && num_class_labels_pos == -1 ) {
							System.out.println("You must tell me the number of class attributes so I can build the class hierarchy or provide an xml with an already built hierarchy or provide an xml that simply lists all the class labels.");
							throw new Exception("You must tell me the number of class attributes so I can build the class hierarchy or provide an xml with an already built hierarchy or provide an xml that simply lists all the class labels.");	
					}
					if ( load_xml_pos != -1 && num_class_labels_pos != -1 ) {
						System.out.println("You must tell me the number of class attributes so I can build the class hierarchy OR provide an xml that simply lists all the class labels. NOT BOTH");
						throw new Exception("You must tell me the number of class attributes so I can build the class hierarchy OR provide an xml that simply lists all the class labels. NOT BOTH");	
					}
					if ( num_class_labels_pos != -1 ) {
						try {
							num_class_labels = Integer.parseInt(getArgument(options,num_class_labels_pos));
							if ( num_class_labels < 2 ) throw new Exception("The number of class labels must be greater then 2.");
						}
						catch (Exception e) {
							if ( e instanceof NumberFormatException ) {
								System.out.println("Wrong number format of the number of class labels option.");
								e = new Exception("Wrong number format of the number of class labels option.");
							}
							else {
								System.out.println(e.getMessage());
							}
							throw e;
						}
					}
					else {
						String temp = getArgument(options, load_xml_pos);
						if ( ! temp.isEmpty() ) load_xml_name = temp;
					}
					//we have a way of finding what the class labels are
					//now we need to check if any specific hierarchy build method was chosen, otherwise we go with the default one
					String temp[] = getArguments(options, build_method_pos,1000);
					temp = removeEmptyStrings(temp);
					if ( temp.length > 0 ) build_method = temp[0];
					if ( !AHierarchyBuilderFactory.getInstance().isRegistered(build_method) ) {
						System.out.println("The hierarchy build method does not exist or is not registered with the hierarchy builder factory.");
						throw new Exception("The hierarchy build method  does not exist or is not registered with the hierarchy builder factory.");
					}
					//we copy over the parameters
					if ( temp.length > 1 ) {
					params = Arrays.copyOfRange(temp,1,temp.length);
					}
					//we check whether the user specified a file name to which to save the built hierarchy
					String t = getArgument(options, save_hierarchy_pos);
					if ( ! t.isEmpty() )save_hier_xml_name = t;
				}
				else {
					//we can't generate and load the hierarchy in one go, we must choose only one option
					if ( save_hierarchy_pos != -1 || load_xml_pos != -1 || num_class_labels_pos != -1 
						|| build_method_pos != -1 ) {
						System.out.println("Please determine whether you will import an already built hierarchy or you will use the MULAN HierarchyBuilder");
						throw new Exception("Please determine whether you will import an already built hierarchy or you will use the MULAN HierarchyBuilder");
					}
					//we check there is a name specified for the hierarchy xml file, else we go with the default one
					String temp = getArgument(options, load_hier_xml_pos);
					if ( ! temp.isEmpty() ) load_hier_xml_name = temp;
				}
				//we check whether the output file name was specified, else we go with default
				String temp = getArgument(options, convert_and_save);
				if ( ! temp.isEmpty() ) output_file_name = temp;
				//we check whether the test file name was specified
				temp = getArguments(options, use_test_file_pos, 2)[0];
				if ( ! temp.isEmpty() ) {
					test_input_file = temp;
					test_output_file = test_input_file+"H";
					temp = getArguments(options, use_test_file_pos, 2)[1];
					if ( ! temp.isEmpty() ) test_output_file = temp;
				}
				//we have gathered all the data from the options and stored them on local variables, from now on we assume there are no conflicts between the options
				if ( load_hier_xml_pos == -1 ) {
					if (  load_xml_pos == -1 ) loadDatasetFromArffFileValidateAgainstNumClassLabels(input_file_name, num_class_labels);
					else loadDatasetFromArffFileValidateAgainstFlatXML(input_file_name, load_xml_name);
					metaLabels = (LabelsMetaDataImpl) AHierarchyBuilderFactory.getInstance().createAHierarchyBuilder(build_method, params).buildLabelHierarchy(dataset);
					if ( save_hierarchy_pos != -1 ) FileIO.saveToXMLFile(save_hier_xml_name,UtilsXML.createXMLFile(metaLabels));
				}
				else loadDatasetFromArffFileValidateAgainstHierarchyXML(input_file_name, load_hier_xml_name);
				if ( convert_and_save != -1 ) {
					DatasetConverter converter = new DatasetConverter(metaLabels);
					Instances clus_dataset = converter.convertMulanToClus(dataset);
					FileIO.saveDataSet(output_file_name, clus_dataset);
					if ( use_test_file_pos != -1 ) {
						loadDatasetFromArffFileValidateAgainstNumClassLabels(test_input_file, dataset.getNumLabels());
						Instances clus_test_dataset = converter.convertMulanToClus(dataset);
						FileIO.saveDataSet(test_output_file, clus_test_dataset);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		ConverterHOMERMULANtoClus c = new ConverterHOMERMULANtoClus(args);
		c.convert();
	}

}
