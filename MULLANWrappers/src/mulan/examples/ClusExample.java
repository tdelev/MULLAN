package mulan.examples;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import mulan.classifier.hierarchy_builders.AHierarchyBuilderFactory;
import mulan.data.converter.ConverterHOMERMULANtoClus;
import weka.core.Utils;

public class ClusExample {

	public static void main(String[] args) throws Exception {
		String settings_file_name = args[0];
		String updated_settings_file_name = settings_file_name
				+ "some_random_suffix";
		Scanner in = new Scanner(new FileInputStream(settings_file_name + ".s"));
		BufferedWriter out = new BufferedWriter(new FileWriter(
				updated_settings_file_name + ".s"));

		String train_file_name = settings_file_name;
		String updated_train_file_name = train_file_name + "some_random_suffix";
		boolean use_test = false;
		String test_file_name = settings_file_name;
		String updated_test_file_name = test_file_name + "some_random_suffix";
		boolean use_prune = false;
		String prune_file_name = settings_file_name;
		String updated_prune_file_name = prune_file_name + "some_random_suffix";

		// we create a new settings file and update the file_names (because the
		// original ones are invalid, we first have to convert them)
		while (in.hasNext()) {
			String line = in.nextLine();
			if (line.startsWith("File")) {
				String file_name = line.substring(line.indexOf('=') + 2);
				try {
					Integer.parseInt(file_name);
				} catch (Exception e) {
					train_file_name = file_name;
					train_file_name = train_file_name.substring(0,
							train_file_name.indexOf('.'));
					updated_train_file_name = train_file_name
							+ "some_random_suffix" + ".arff";
					line = line.substring(0, line.indexOf('=') + 2);
					line += updated_train_file_name;
				}
			}
			if (line.startsWith("TestSet")) {
				String file_name = line.substring(line.indexOf('=') + 2);
				try {
					Integer.parseInt(file_name);
				} catch (Exception e) {
					test_file_name = file_name;
					test_file_name = test_file_name.substring(0,
							test_file_name.indexOf('.'));
					updated_test_file_name = test_file_name
							+ "some_random_suffix" + ".arff";
					line = line.substring(0, line.indexOf('=') + 2);
					line += updated_test_file_name;
					use_test = true;
				}
				System.out.println(updated_test_file_name);

			}
			if (line.startsWith("PruneSet")) {
				String file_name = line.substring(line.indexOf('=') + 2);
				try {
					Integer.parseInt(file_name);
				} catch (Exception e) {
					use_prune = true;
					prune_file_name = file_name;
					prune_file_name = prune_file_name.substring(0,
							prune_file_name.indexOf('.'));
					updated_prune_file_name = prune_file_name
							+ "some_random_suffix" + ".arff";

					line = line.substring(0, line.indexOf('=') + 2);
					line += updated_prune_file_name;
				}
			}
			out.write(line + "\n");
		}
		out.flush();
		out.close();
		in.close();

		// we check for some arguments that were passed by the command line
		// these are to be used by the converter
		int load_hier_xml_pos = Utils.getOptionPos('L', args);
		String load_hier_xml_name = test_file_name + "H";

		int load_xml_pos = Utils.getOptionPos('X', args);
		String load_xml_name = test_file_name;

		int num_class_labels_pos = Utils.getOptionPos('N', args);
		int num_class_labels = -1;

		int build_method_pos = Utils.getOptionPos('M', args);
		String build_method = "BalancedClustering";
		String params[] = { "2" };

		int save_hierarchy_pos = Utils.getOptionPos('S', args);
		String save_hier_xml_name = test_file_name + "H";
		// we check for consistencies
		if (load_hier_xml_pos == -1) {
			// since there is no hierarchy file provided we will have to build
			// the hierarchy ourselves
			// for that we need either the number of class labels or an xml file
			// that lists all the class labels explicitly, but not both
			if (load_xml_pos == -1 && num_class_labels_pos == -1) {
				System.out
						.println("You must tell me the number of class attributes so I can build the class hierarchy or provide an xml with an already built hierarchy or provide an xml that simply lists all the class labels.");
				throw new Exception(
						"You must tell me the number of class attributes so I can build the class hierarchy or provide an xml with an already built hierarchy or provide an xml that simply lists all the class labels.");
			}
			if (load_xml_pos != -1 && num_class_labels_pos != -1) {
				System.out
						.println("You must tell me the number of class attributes so I can build the class hierarchy OR provide an xml that simply lists all the class labels. NOT BOTH");
				throw new Exception(
						"You must tell me the number of class attributes so I can build the class hierarchy OR provide an xml that simply lists all the class labels. NOT BOTH");
			}
			if (num_class_labels_pos != -1) {
				try {
					num_class_labels = Integer.parseInt(getArgument(args,
							num_class_labels_pos));
					if (num_class_labels < 2)
						throw new Exception(
								"The number of class labels must be greater then 2.");
				} catch (Exception e) {
					if (e instanceof NumberFormatException) {
						System.out
								.println("Wrong number format of the number of class labels option.");
						e = new Exception(
								"Wrong number format of the number of class labels option.");
					} else {
						System.out.println(e.getMessage());
					}
					throw e;
				}
			} else {
				String temp = getArgument(args, load_xml_pos);
				if (!temp.isEmpty())
					load_xml_name = temp;
			}
			// we have a way of finding what the class labels are
			// now we need to check if any specific hierarchy build method was
			// chosen, otherwise we go with the default one
			String temp[] = getArguments(args, build_method_pos, 1000);
			temp = removeEmptyStrings(temp);
			if (temp.length > 0)
				build_method = temp[0];
			if (!AHierarchyBuilderFactory.getInstance().isRegistered(
					build_method)) {
				System.out
						.println("The hierarchy build method does not exist or is not registered with the hierarchy builder factory.");
				throw new Exception(
						"The hierarchy build method  does not exist or is not registered with the hierarchy builder factory.");
			}
			// we copy over the parameters
			if (temp.length > 1) {
				params = Arrays.copyOfRange(temp, 1, temp.length);
			}
			// we check whether the user specified a file name to which to save
			// the built hierarchy
			String t = getArgument(args, save_hierarchy_pos);
			if (!t.isEmpty())
				save_hier_xml_name = t;
		} else {
			// we can't generate and load the hierarchy in one go, we must
			// choose only one option
			if (save_hierarchy_pos != -1 || load_xml_pos != -1
					|| num_class_labels_pos != -1 || build_method_pos != -1) {
				System.out
						.println("Please determine whether you will import an already built hierarchy or you will use the MULAN HierarchyBuilder");
				throw new Exception(
						"Please determine whether you will import an already built hierarchy or you will use the MULAN HierarchyBuilder");
			}
			// we check there is a name specified for the hierarchy xml file,
			// else we go with the default one
			String temp = getArgument(args, load_hier_xml_pos);
			if (!temp.isEmpty())
				load_hier_xml_name = temp;
		}
		// now we copy over all the valid converter arguments
		ArrayList<String> converter_args = new ArrayList<String>();
		if (load_hier_xml_pos != -1) {
			converter_args.add("-L");
			converter_args.add(load_hier_xml_name);
		}
		if (load_xml_pos != -1) {
			converter_args.add("-X");
			converter_args.add(load_xml_name);
		}
		if (num_class_labels_pos != -1) {
			converter_args.add("-N");
			converter_args.add(Integer.toString(num_class_labels));
		}
		if (build_method_pos != -1) {
			converter_args.add("-M");
			converter_args.add(build_method);
			converter_args.addAll(Arrays.asList(params));
		}
		// we set the input_file_name, and then tell the converter to save our
		// converted file and our hierarchy
		String hierarchy_file_name = save_hier_xml_name;
		converter_args.add(0, train_file_name);
		converter_args.add("-C");
		converter_args.add(updated_train_file_name.substring(0,
				updated_train_file_name.indexOf('.')));
		converter_args.add("-S");
		converter_args.add(save_hier_xml_name);
		ConverterHOMERMULANtoClus.main(converter_args
				.toArray(new String[converter_args.size()]));
		// if we have a training set we have to convert it as well, instead of
		// building the hierarchy twice, we use the hierarchy xml
		if (use_test) {
			converter_args = new ArrayList<String>();
			converter_args.add(test_file_name);
			converter_args.add("-L");
			converter_args.add(save_hier_xml_name);
			converter_args.add("-C");
			converter_args.add(updated_test_file_name.substring(0,
					updated_test_file_name.indexOf('.')));
			ConverterHOMERMULANtoClus.main(converter_args
					.toArray(new String[converter_args.size()]));

		}
		// if we have a training set we have to convert it as well, instead of
		// building the hierarchy twice, we use the hierarchy xml
		if (use_prune) {
			converter_args = new ArrayList<String>();
			converter_args.add(prune_file_name);
			converter_args.add("-L");
			converter_args.add(save_hier_xml_name);
			converter_args.add("-C");
			converter_args.add(updated_prune_file_name.substring(0,
					updated_prune_file_name.indexOf('.')));
			ConverterHOMERMULANtoClus.main(converter_args
					.toArray(new String[converter_args.size()]));

		}
		// we get all the clus arguments and set the settings file name, then we
		// run clus
		int clus_args_pos = Utils.getOptionPos("-clus", args);
		String clus_args[] = removeEmptyStrings(getArguments(args,
				clus_args_pos, 1100));
		clus_args = Arrays.copyOf(clus_args, clus_args.length + 1);
		for (int i = 1; i < clus_args.length; ++i)
			clus_args[i] = clus_args[i - 1];
		clus_args[0] = updated_settings_file_name;
		// Clus.main(clus_args);
	}

	/**
	 * Get all the arguments from the arguments array that occur after a
	 * specified position, and before another flag (a flag is a string starting
	 * with '-'). If the number of expected arguments is larger then the missing
	 * arguments will be defaulted as empty strings.
	 * 
	 * @param args
	 *            - an array of all the arguments
	 * @param pos
	 *            - the position before the first possible relevant argument
	 * @param num_args
	 *            - the expected number of arguments (can be greater then the
	 *            real number of arguments). Any arguments that are not expected
	 *            are ignored.
	 * @return an array of string containing the valid arguments that occur
	 *         after a given position and before another flag i.e.
	 *         subarray(args,pos+1,nextFlag(args,pos+1)) - if we assume that
	 *         nextFlag returns the position of a flag argument ^ ^ start end
	 */
	private static String[] getArguments(String[] args, int pos, int num_args) {
		String res[] = new String[num_args];
		for (int i = 0; i < num_args; ++i)
			res[i] = "";
		int i = 0;
		try {
			while (pos != -1 && args[pos + 1].charAt(0) != '-') {
				res[i++] = args[++pos];
			}
		} catch (Exception e) {
		}
		return res;
	}

	/**
	 * Gets a single argument proceeding a flag at position pos. If no such
	 * argument exists an empty string is returned.
	 * 
	 * @param arguments
	 *            - the arguments that we are processing
	 * @param pos
	 *            - the position of the flag argument
	 * @return a single argument that occurs right after the pos index in the
	 *         arguments array. Empty string if such element does not exist.
	 */
	private static String getArgument(String[] arguments, int pos) {
		return getArguments(arguments, pos, 1)[0];
	}

	/**
	 * Removes all empty string that occur at the end of a string array.
	 * Actually, it removes all strings in the array that occur after the first
	 * empty string in the array.
	 * 
	 * @param strings
	 *            - the array of string we are processing
	 * @return a sub-array of the string parameter that has all empty string
	 *         from the arguments array cut off.
	 */
	private static String[] removeEmptyStrings(String[] strings) {
		int count = 0;
		for (String s : strings)
			if (s != null && !s.isEmpty())
				++count;
		return Arrays.copyOf(strings, count);
	}

}
