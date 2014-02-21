package mulan.classifier.hierarchy_builders;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

public class AHierarchyBuilderFactory {
	
	private static volatile AHierarchyBuilderFactory instance;

	private HashMap<String,AHierarchyBuilder> registered_hierarchy_builders;

	public boolean registerAHierarchyBuilder(String aHierarchyBuilderID, AHierarchyBuilder hb)    {
		if ( registered_hierarchy_builders.containsKey(aHierarchyBuilderID) ) return false;
		registered_hierarchy_builders.put(aHierarchyBuilderID, hb);
		return true;
	}

	public AHierarchyBuilder createAHierarchyBuilder(String aHierarchyBuilderID, String... params){
		if ( isRegistered(aHierarchyBuilderID) ){
                        System.out.println(aHierarchyBuilderID);
			return registered_hierarchy_builders.get(aHierarchyBuilderID).createProduct(params);
                }
		else return null;
	}
	
	private AHierarchyBuilderFactory() throws IOException {
		  registered_hierarchy_builders = new HashMap<String,AHierarchyBuilder>();
		  Scanner in = new Scanner(new FileInputStream("HierarchyBuilders.txt"));
		  while ( in.hasNext() ) {
			  String key = in.next();
			  String klass_name = in.next();
			  try {
				registerAHierarchyBuilder(key, (AHierarchyBuilder) Class.forName(klass_name).newInstance());
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  }
	}
	
	public static AHierarchyBuilderFactory getInstance() {
		if ( instance == null ) {
			synchronized (AHierarchyBuilderFactory.class) {
				if ( instance == null )
					try {
						instance = new AHierarchyBuilderFactory();
					} catch (IOException e) {
						
					}
			}
		}
		return instance;
	}
	
	public boolean isRegistered( String aHierarchyBuilderID ) {
		return registered_hierarchy_builders.containsKey(aHierarchyBuilderID);
	}
	
}
