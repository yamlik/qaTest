import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.*;
/*
 Init list with given tasks , compare task in json list with inited list for task comparison
 Exit when
 1. Invalid json string
 2. Missing json key, tasks, name, depends on
 3. number of tasks less or more than defined number
 4. tasks not in sequence compare to inited list
 5. when hash set found duplicated depends on value
 6. when depends on contain task that not allowed for current task and show allowed tasks. Eg. compile appear in depends on for check_build_script
 7. when depends on contain task that same name as current task 
 */
public class tidyCode {
	
	public static void verifyJson(String jsonString) {
		try {
			new JSONObject(jsonString);
		} catch(JSONException e ){
			System.out.println("Error: Invalid json String");
			System.exit(0);
		}
		
		JSONObject holder = new JSONObject(jsonString);
		if(!holder.has("tasks")) {System.out.println("Error: Missing tasks key"); System.exit(0);}
		
		JSONArray holder2= holder.getJSONArray("tasks");
		for(int i =0; i <holder2.length(); i++) {
			if(!holder2.getJSONObject(i).has("name") || !holder2.getJSONObject(i).has("depends_on")) {
				System.out.println("Error:Missing task name or depends_on");
				System.exit(0);
			}	
		}
	}

	public static void main(String[] args ) {
		
 		String input_string ="{\"tasks\": [\r\n"
				+ "{\r\n"
				+ "\"name\" : \"check_build_script\",\r\n"
				+ "\"depends_on\" : []\r\n"
				+ "},\r\n"
				+ "{\r\n"
				+ "\"name\" : \"lint\",\r\n"
				+ "\"depends_on\" : [\"check_build_script\"]\r\n"
				+ "},\r\n"
				+ "{\r\n"
				+ "\"name\" : \"compile\",\r\n"
				+ "\"depends_on\" : [\"lint\"]\r\n"
				+ "},\r\n"
				+ "{\r\n"
				+ "\"name\" : \"package\",\r\n"
				+ "\"depends_on\" : [\"compile\",\"lint\", \"check_build_script\"]\r\n"
				+ "},\r\n"
				+ "{\r\n"
				+ "\"name\" : \"test\",\r\n"
				+ "\"depends_on\" : [\"package\"]\r\n"
				+ "}\r\n"
				+ "]}";
  

		//init default list that decide total task for a complete flow
		List<String> static_stage_list =  new ArrayList<String>();
		static_stage_list.add("check_build_script");
		static_stage_list.add("lint");
		static_stage_list.add("compile");
		static_stage_list.add("package");
		static_stage_list.add("test");
		final int total_stages_number= static_stage_list.size(); 
		
		//handle invalid json, missing required key
		verifyJson(input_string);
		JSONObject holder = new JSONObject(input_string);
		JSONArray holder2= holder.getJSONArray("tasks");
		
		//Check total tasks that must have
		if(holder2.length() < total_stages_number || holder2.length() > total_stages_number) { 
			System.out.println("Error: Shouldnt less than or more than " + Integer.toString(total_stages_number));
			System.exit(0);
		}
		
		if(holder2.length() == total_stages_number) {
			List<String> stage_list = new ArrayList<String>();
			for(int i =0; i <holder2.length(); i++) {
				System.out.println("Name: " + holder2.getJSONObject(i).get("name").toString());
				stage_list.add(holder2.getJSONObject(i).get("name").toString());
				if(stage_list.get(i).toString().equals(static_stage_list.get(i).toString())) {
					//same stage confirmed, check depends on
					//declare list to store depend on
					List<String> depends_check = new ArrayList<String>();
					//create hash set to check duplicate value in depends on 
					Set<String> hashset = new HashSet<String>();
					for (int k = 0; k < holder2.getJSONObject(i).getJSONArray("depends_on").length(); k++){ 
					  depends_check.add(holder2.getJSONObject(i).getJSONArray("depends_on").getString(k));
					} 
					   
					for(String str: depends_check) {
					  if(!str.equalsIgnoreCase(holder2.getJSONObject(i).get("name").toString())) {
					    System.out.println("Depends check: "+ str);
						boolean dupFlag = hashset.add(str);
						if(!dupFlag){ 
							System.out.println("Error: " + str + " duplicated");
							System.exit(0);
						} else {
							System.out.println(str + " not duplicate"); 
						   if(!static_stage_list.subList(0, i).contains(str)) {
							   System.out.println("Error: This stage " + str + " is not allowed for current stage: " + holder2.getJSONObject(i).get("name").toString());
							   System.out.print("Allowed stages are: ");
							   for(String allowed_stages: static_stage_list.subList(0, i)) {
								   System.out.print(allowed_stages + " ");
							   }
							   System.out.println("");
							   System.exit(0);
						   }
						}
					  } else { 
						 System.out.println("Error: depends on cant have task having same name as task name");
						 System.exit(0);
					  }
				   }  
			     } else { 
			    	 System.out.println("Error: task name or current task is not in defined sequence");
			    	 System.exit(0);
			     }
				System.out.println("Result: Valid Stage\n");
			  }
		}

	}
}

