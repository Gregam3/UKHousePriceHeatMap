package asegroup1.api.models.landregistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LandRegistryQueryValues {
	private String name;
	private List<String> values;


	public LandRegistryQueryValues(String name, List<String> values) {
		this.name = name;
		this.values = values;
	}

	public LandRegistryQueryValues(String name, String... values) {
		this(name, new ArrayList<String>(Arrays.asList(values)));
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}


	@Override
	public String toString() {
		StringBuilder valuesBuilder = new StringBuilder("VALUES (?");
		valuesBuilder.append(name).append(") {");
		for (String value : values) {
			valuesBuilder.append("(").append(value).append(")");
		}
		valuesBuilder.append("}");
		return valuesBuilder.toString();
	}

}
