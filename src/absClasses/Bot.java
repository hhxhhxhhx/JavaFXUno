package absClasses;

import java.util.ArrayList;
import java.util.Collections;

public abstract class Bot {
	
	private static ArrayList<String> nameList;
	private static int index = 0;
	protected String name;

	protected Bot() {
		if (nameList == null) 
			generateNameList();
		name = nameList.get(index);
		index++;
	}
	
	private void generateNameList() {
		nameList = new ArrayList<String>();
		String pref = "AI ";
		nameList.add(pref + "John");
		nameList.add(pref + "Bob");
		nameList.add(pref + "Michelle");
		nameList.add(pref + "Rachel");
		nameList.add(pref + "Donald");
		nameList.add(pref + "Kacie");
		nameList.add(pref + "Jack");
		nameList.add(pref + "Juliet");
		nameList.add(pref + "Supreme Leader");
		nameList.add(pref + "IA");
		Collections.shuffle(nameList);
	}
	
	public abstract void takeTurn(Game game) throws Exception;
	
	public String getName() {
		return this.name;
	}
}
