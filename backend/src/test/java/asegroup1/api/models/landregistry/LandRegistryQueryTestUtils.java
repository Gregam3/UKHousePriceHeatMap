package asegroup1.api.models.landregistry;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import asegroup1.api.models.landregistry.LandRegistryQuery.Aggrigation;
import asegroup1.api.models.landregistry.LandRegistryQuery.PropertyType;
import asegroup1.api.models.landregistry.LandRegistryQuery.Selectable;

public class LandRegistryQueryTestUtils {

	public static final long randomSeed = 8312595207343625996L;

	static String generateRandomString() {
		return "Random: " + new Random(randomSeed).nextLong();
	}

	static LandRegistryQuery genLandRegistryQuery(boolean constraintContent) {
		return new LandRegistryQuery(constraintContent ? genLandRegistryQueryConstraint() : genLandRegistryQuery(true), genLandRegistryQueryGroup(), genLandRegistryQuerySelect());
	}

	static LandRegistryData genLandRegistryData() {
		LandRegistryData data = new LandRegistryData();
		data.setNewBuild(true);
		data.setPricePaid(new Random(LandRegistryQueryTestUtils.randomSeed).nextInt(Integer.MAX_VALUE));
		data.setPrimaryHouseName(LandRegistryQueryTestUtils.generateRandomString());
		data.setStreetName(LandRegistryQueryTestUtils.generateRandomString());
		data.setTownName(LandRegistryQueryTestUtils.generateRandomString());
		data.setPropertyType(PropertyType.terraced);
		return data;
	}

	static LandRegistryQueryConstraint genLandRegistryQueryConstraint() {
		LandRegistryQueryConstraint constraint = new LandRegistryQueryConstraint(genLandRegistryData());
		constraint.setMaxDate(LocalDate.now());
		constraint.setMinDate(LocalDate.now().minusYears(5));
		constraint.setMinPricePaid(150000);
		constraint.setPostcodes(getRandomPostCodes());
		return constraint;
	}

	static LandRegistryQuerySelect genLandRegistryQuerySelect() {
		LandRegistryQuerySelect select = new LandRegistryQuerySelect();
		for (Entry<Selectable, Aggrigation> entry : genRandomSelectableAggrigations().entrySet()) {
			select.addSelectValue(entry.getKey(), entry.getValue());
		}
		return select;
	}

	static LinkedHashMap<Selectable, Aggrigation> genRandomSelectableAggrigations() {
		ArrayList<Selectable> selectableSet = new ArrayList<>(EnumSet.allOf(Selectable.class));
		ArrayList<Aggrigation> aggrigationSet = new ArrayList<>(EnumSet.allOf(Aggrigation.class));
		Random rand = new Random(LandRegistryQueryTestUtils.randomSeed);

		LinkedHashMap<Selectable, Aggrigation> map = new LinkedHashMap<>();

		int iterations = rand.nextInt(selectableSet.size() - 4) + 3;
		for (int i = 0; i < iterations; i++) {
			Selectable selectable = selectableSet.remove(rand.nextInt(selectableSet.size()));
			Aggrigation aggrigation = aggrigationSet.get(rand.nextInt(aggrigationSet.size()));
			map.put(selectable, aggrigation);
		}
		return map;
	}

	static String[] getRandomPostCodes() {
		return new String[] { "OX14 1WH", "L18 9SN", "TN27 8JG", "PL8 2EE" };
	}

	static List<Selectable> genRandomSelectables(Set<Selectable> options) {
		Random r = new Random(LandRegistryQueryTestUtils.randomSeed);
		ArrayList<Selectable> unSelected = new ArrayList<>(options);
		ArrayList<Selectable> selected = new ArrayList<>();
		int reps = r.nextInt(options.size() - 1) + 1;
		for (int i = 0; i < reps; i++) {
			selected.add(unSelected.remove(r.nextInt(unSelected.size())));
		}

		return selected;
	}

	static List<Selectable> genRandomSelectables() {
		return genRandomSelectables(EnumSet.allOf(Selectable.class));
	}

	static String[] buildRandomSelectablesArray(List<Selectable> select) {
		return select.stream().map(v -> v.toString()).collect(Collectors.toList()).toArray(new String[select.size()]);
	}

	static LandRegistryQueryGroup genLandRegistryQueryGroup() {
		return new LandRegistryQueryGroup(buildRandomSelectablesArray(genRandomSelectables()));
	}

	/* REGEX */

	static String buildQuerySelectRegex(boolean ignoreAggrigation) {
		String start = "SELECT";

		List<String> options = new ArrayList<>();
		for (Selectable selectable : EnumSet.allOf(Selectable.class)) {
			String str = selectable.toString();
			options.add(str);
			options.add(str.substring(0, 1).toUpperCase() + str.substring(1));
		}

		String optionList = regexOptionalList(options);
		String option = "\\?" + optionList;

		ArrayList<Aggrigation> aggrigations = new ArrayList<Aggrigation>(EnumSet.allOf(Aggrigation.class));
		aggrigations.remove(Aggrigation.NONE);
		
		String aggrigationList = regexOptionalList(runOnList(aggrigations, v -> v.toString()));

		String aggrigationOption = regexAddWithDelim("\\s*", "\\(", aggrigationList, "\\(", option, "\\)", "AS", option, "\\)");
		String aggrigationOptions = regexOptionalList(option, aggrigationOption);

		String regex = regexAddWithDelim("\\s*", start, "(", (ignoreAggrigation ? option : aggrigationOptions), ")+");
		
		return regex;
	}

	static String buildQueryGroupRegex(List<Selectable> selectables) {
		String start = "GROUP BY";
		String groupOptions = regexOptionalList(runOnList(selectables, v -> v.toString()));
		String groupOption = "\\?" + groupOptions;
		return regexAddWithDelim("\\s*", start, "(", groupOption, ")+");
	}

	private static <E, T> List<E> runOnList(List<T> data, Function<T, E> operation) {
		List<E> list = new ArrayList<>();
		data.forEach(v -> list.add(operation.apply(v)));
		return list;
	}
	
	static String buildQueryConstraintRegex() {
		String delimeter = "\\s*";
		// value regex parts
		String valueReference = "\\?\\w+";
		String valueString = "\"[^\"]*\"";
		String valueInteger = "\\d+";
		String valueBoolean = regexOptionalList("true", "false");

		String yearReg = "\\d{4}";
		String monthReg = regexOptionalList("0[1-9]", "1[0-2]");
		String dayReg = regexOptionalList("0[1-9]", "[1-2]\\d", "3[0,1]");
		String valueCalendar = regexAddWithDelim("\\-", yearReg, monthReg, dayReg);

		// declaration segments
		String namespace = "\\w+:\\w+";
		String value = regexOptionalList(valueReference, valueString, valueInteger, valueCalendar, namespace, valueBoolean);
		String advancedNameSpace = namespace + "(/" + namespace + ")?";

		// declaration
		String partialDeclaration = regexAddWithDelim(delimeter, advancedNameSpace, value);


		String partialDeclarationListStart = regexAddWithDelim(delimeter, partialDeclaration, ";");
		String partialDeclarationList = regexAddWithDelim(delimeter, "(", partialDeclarationListStart, ")*", partialDeclaration);



		String declaration = regexAddWithDelim("\\s+", valueReference, partialDeclarationList);
		String fullDeclaration = regexAddWithDelim(delimeter, declaration, "\\.");

		String optionalDeclaration = regexAddWithDelim(delimeter, "OPTIONAL", "\\{", declaration, "\\}");



		// declaration list

		String declarationListOptions = regexOptionalList(fullDeclaration, optionalDeclaration);
		String declarationList = regexAddWithDelim(delimeter, "(", declarationListOptions, ")+");


		// FILTER

		// range filter
		String filterConstraint = "[<>]";
		String filterCast = "\\^\\^" + namespace;
		String filterValue = value + "(" + filterCast + ")?";
		String rangeFilter = regexAddWithDelim(delimeter, valueReference, filterConstraint, filterValue);

		// regex filter
		String regexFilterValue = "\".*\"";
		String regexFilter = regexAddWithDelim(delimeter, "REGEX", "\\(", valueReference, ",", regexFilterValue, "\\)");

		String filterOption = regexOptionalList(rangeFilter, regexFilter);
		String filterOptionListStart = regexAddWithDelim(delimeter, "(", filterOption, "&&)*");
		String filterOptionList = regexAddWithDelim(delimeter, filterOptionListStart, filterOption);

		String filter = regexAddWithDelim(delimeter, "FILTER", "\\(", "(" + filterOptionList + ")?", "\\)");
		String optionalFilter = "(" + filter + ")?";

		String queryRegex = regexAddWithDelim(delimeter, declarationList, optionalFilter);


		return queryRegex;
	}

	static String buildQueryRegexInternal(int depth) {
		if (depth > 0) {
			return regexAddWithDelim("\\s*", buildQuerySelectRegex(false), "WHERE", "\\{", buildQueryRegexInternal(depth - 1), "\\}",
					"(" + buildQueryGroupRegex(new ArrayList<>(EnumSet.allOf(Selectable.class))) + ")?");
		} else {
			return regexAddWithDelim("\\s*", buildQuerySelectRegex(false), "WHERE", "\\{", buildQueryConstraintRegex(), "\\}",
					"(" + buildQueryGroupRegex(new ArrayList<>(EnumSet.allOf(Selectable.class))) + ")?");
		}
	}

	private static String regexAddWithDelim(String delimeter, String... parts) {
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < parts.length - 1; i++) {
			str.append(parts[i] + delimeter);
		}
		str.append(parts[parts.length - 1]);
		return str.toString();
	}

	private static String regexOptionalList(String... options) {
		StringBuilder str = new StringBuilder("(");
		for (String option : options) {
			str.append("(" + option + ")|");
		}
		str.deleteCharAt(str.length() - 1);
		str.append(")");
		return str.toString();
	}

	private static String regexOptionalList(List<String> options) {
		return regexOptionalList(options.toArray(new String[options.size()]));
	}
}
