package asegroup1.api.services.landregistry;

import asegroup1.api.controllers.LandRegistryController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 
 * @author Rikkey Paal
 */
public class MathUtil {

	private final static Logger logger = LogManager.getLogger(LandRegistryController.class);


	public static List<Double> normaliseList(List<Double> numbers) {
		if (numbers == null) {
			return null;
		} else if (numbers.isEmpty()) {
			return new ArrayList<Double>();
		}
		boolean same = true;
		for (Double d : numbers) {
			if (d <= 0) {
				throw new IllegalArgumentException(
						"All parameters must be greater than 0");
			}
			if (!d.equals(numbers.get(0))) {
				same = false;
			}
		}
		if (same) {
			return numbers.stream().map(num -> 0.5)
					.collect(Collectors.toList());
		} else {
			logger.info("Start");
			List<Double> retNum = rescaleList(standardiseList(numbers));

			retNum = makePositive(retNum);

			printList(retNum, "Final");

			return retNum;
		}
	}

	public static void printList(List<Double> retNum, String name) {
		double min, max, avg = 0;
		min = max = retNum.get(0);
		for (Double dum : retNum) {
			if (dum > max) {
				max = dum;
			} else if (dum < min) {
				min = dum;
			}
			avg += dum;
		}

		avg /= retNum.size();

		logger.info(name + ":");
		logger.info("\tMin: " + min);
		logger.info("\tMax: " + max);
		logger.info("\tAVG: " + avg);
	}

	public static List<Double> standardiseList(List<Double> numbers) {

		numbers = numbers.stream().map(Math::log).collect(Collectors.toList());

		// mean standard deviation
		double mean, sd, total = 0;
		for (double num : numbers) {
			total += num;
		}
		mean = total / numbers.size();

		// get standard deviation
		total = 0;
		for (double num : numbers) {
			total += Math.pow(num - mean, 2);
		}
		sd = Math.sqrt(total / numbers.size());

		List<Double> standardise = numbers.stream()
				.map(pricePaid -> (pricePaid - mean) / (sd / 7))
				.collect(Collectors.toList());

		printList(standardise, "Standard");

		List<Double> retList = standardise.stream().map(standardised -> {
			// normalise result
			return (1 / (1 + Math.pow(Math.E, (-1 * standardised))));
		}).collect(Collectors.toList());

		printList(retList, "Norm");
		return retList;
	}

	public static List<Double> rescaleList(List<Double> numbers) {
		double max, min, total = 0;
		max = min = numbers.get(0);

		for (double num : numbers) {
			if (num > max) {
				max = num;
			} else if (num < min) {
				min = num;
			}
			total += num;
		}

		total /= numbers.size();

		final double range = max - min;
		final double minVar = total;
		return numbers.stream().map(pricePaid -> ((pricePaid - minVar) / range))
				.collect(Collectors.toList());
	}

	public static List<Double> makePositive(List<Double> numbers) {
		double min = 0;
		for (double num : numbers) {
			if (num < min) {
				min = num;
			}
		}
		if (min < 0) {
			final double numToAdd = -min;
			return numbers.stream().map(num -> num + numToAdd)
					.collect(Collectors.toList());
		} else {
			return numbers;
		}
	}
}
