package eu.smartdatalake.simjoin;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import eu.smartdatalake.simjoin.sets.JoinResult;
import eu.smartdatalake.simjoin.sets.SetSimJoinImpl;
import eu.smartdatalake.simjoin.sets.TokenSet;
import eu.smartdatalake.simjoin.sets.TokenSetCollection;
import eu.smartdatalake.simjoin.sets.io.ResultsWriter;
import eu.smartdatalake.simjoin.sets.io.TokenSetCollectionReader;

public class RunSimJoin {

	public static void main(String[] args) {

		String configFile = args.length > 0 ? args[0] : "config.properties";

		try (InputStream config = new FileInputStream(configFile)) {

			/* READ PARAMETERS */
			Properties prop = new Properties();
			prop.load(config);

			// mode and operation
			String mode = prop.getProperty("mode");
			String operation = prop.getProperty("operation");

			// input
			String inputFile = prop.getProperty("input_file");
			String queryFile = prop.getProperty("query_file");
			int queryId = Integer.parseInt(prop.getProperty("query_id"));
			int maxLines = Integer.parseInt(prop.getProperty("max_lines"));

			// file parsing
			int colSets = Integer.parseInt(prop.getProperty("set_column")) - 1;
			int colTokens = Integer.parseInt(prop.getProperty("tokens_column")) - 1;
			String columnDelimiter = prop.getProperty("column_delimiter");
			if (columnDelimiter == null || columnDelimiter.equals(""))
				columnDelimiter = " ";
			String tokenDelimiter = prop.getProperty("token_delimiter");
			if (tokenDelimiter == null || tokenDelimiter.equals(""))
				tokenDelimiter = " ";
			boolean header = Boolean.parseBoolean(prop.getProperty("header"));

			// output
			boolean returnCounts = Boolean.parseBoolean(prop.getProperty("return_counts"));
			String outputFile = prop.getProperty("output_file");

			// similarity
			double simThreshold = Double.parseDouble(prop.getProperty("sim_threshold"));

			// top-k
			int k = Integer.parseInt(prop.getProperty("k"));

			/* EXECUTE THE OPERATION */

			long duration;

			if (mode.equalsIgnoreCase("standard")) {
				JoinResult result;
				ResultsWriter resultsWriter = new ResultsWriter();

				TokenSetCollectionReader reader = new TokenSetCollectionReader();
				SetSimJoinImpl ssjoin = new SetSimJoinImpl();
				TokenSetCollection queryCollection, collection;

				switch (operation) {

				case "search":
					duration = System.nanoTime();
					queryCollection = reader.importFromFile(queryFile, colSets, colTokens, columnDelimiter,
							tokenDelimiter, maxLines, header);
					TokenSetCollection inputCollection = reader.importFromFile(inputFile, colSets, colTokens,
							columnDelimiter, tokenDelimiter, maxLines, header);
					TokenSet querySet = queryCollection.sets[queryId];
					duration = System.nanoTime() - duration;
					System.out.println("Read time: " + duration / 1000000000.0 + " sec.");

					result = ssjoin.rangeSearch(querySet, inputCollection, simThreshold, returnCounts);
					resultsWriter.printJoinResults(result, outputFile);
					break;

				case "self-join":
					duration = System.nanoTime();
					collection = reader.importFromFile(inputFile, colSets, colTokens, columnDelimiter, tokenDelimiter,
							maxLines, header);
					duration = System.nanoTime() - duration;
					System.out.println("Read time: " + duration / 1000000000.0 + " sec.");

					result = ssjoin.rangeSelfJoin(collection, simThreshold, returnCounts);
					resultsWriter.printJoinResults(result, outputFile);
					break;

				case "join":
					duration = System.nanoTime();
					queryCollection = reader.importFromFile(queryFile, colSets, colTokens, columnDelimiter,
							tokenDelimiter, maxLines, header);
					collection = reader.importFromFile(inputFile, colSets, colTokens, columnDelimiter, tokenDelimiter,
							maxLines, header);
					duration = System.nanoTime() - duration;
					System.out.println("Read time: " + duration / 1000000000.0 + " sec.");

					result = ssjoin.rangeJoin(queryCollection, collection, simThreshold, returnCounts);
					resultsWriter.printJoinResults(result, outputFile);
					break;

				case "knn-search":
					duration = System.nanoTime();
					queryCollection = reader.importFromFile(queryFile, colSets, colTokens, columnDelimiter,
							tokenDelimiter, maxLines, header);
					inputCollection = reader.importFromFile(inputFile, colSets, colTokens, columnDelimiter,
							tokenDelimiter, maxLines, header);
					querySet = queryCollection.sets[queryId];
					duration = System.nanoTime() - duration;
					System.out.println("Read time: " + duration / 1000000000.0 + " sec.");

					result = ssjoin.knnSearch(querySet, inputCollection, k);
					resultsWriter.printJoinResults(result, outputFile);
					break;

				case "knn-join":
					duration = System.nanoTime();
					queryCollection = reader.importFromFile(queryFile, colSets, colTokens, columnDelimiter,
							tokenDelimiter, maxLines, header);
					collection = reader.importFromFile(inputFile, colSets, colTokens, columnDelimiter, tokenDelimiter,
							maxLines, header);
					duration = System.nanoTime() - duration;
					System.out.println("Read time: " + duration / 1000000000.0 + " sec.");

					result = ssjoin.knnJoin(queryCollection, collection, k);
					resultsWriter.printJoinResults(result, outputFile);
					break;

				case "self-closest-pairs":
					duration = System.nanoTime();
					collection = reader.importFromFile(inputFile, colSets, colTokens, columnDelimiter, tokenDelimiter,
							maxLines, header);
					duration = System.nanoTime() - duration;
					System.out.println("Read time: " + duration / 1000000000.0 + " sec.");

					result = ssjoin.closestPairsSelfJoin(collection, k);
					resultsWriter.printJoinResults(result, outputFile);
					break;

				case "closest-pairs":
					duration = System.nanoTime();
					queryCollection = reader.importFromFile(queryFile, colSets, colTokens, columnDelimiter,
							tokenDelimiter, maxLines, header);
					collection = reader.importFromFile(inputFile, colSets, colTokens, columnDelimiter, tokenDelimiter,
							maxLines, header);
					duration = System.nanoTime() - duration;
					System.out.println("Read time: " + duration / 1000000000.0 + " sec.");

					result = ssjoin.closestPairsJoin(queryCollection, collection, k);
					resultsWriter.printJoinResults(result, outputFile);
					break;

				default:
					System.out.println("Unknown operation");
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
}