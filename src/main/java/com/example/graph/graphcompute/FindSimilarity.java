package com.example.graph.graphcompute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.springframework.web.client.RestTemplate;

import com.example.graph.graphcompute.model.AtomVertex;
import com.example.graph.graphcompute.model.BoundEdge;
import com.example.graph.graphcompute.model.LocationZone;
import com.example.graph.graphcompute.util.Jaccard;

public class FindSimilarity {
	private static final String URL_DCZONE = "http://localhost:8090/input/dc";

	public static void main(String[] args) {
		Map<String, String> locZoneMap = readLocZone();
		List<UndirectedGraph<AtomVertex, BoundEdge>> structureGraphList = readGraph(locZoneMap);

		// compute similarity between all graph pairs
		for (int i = 0; i < structureGraphList.size(); i++) {
			Set<Integer> fingerprints1 = getFingerprintSet(structureGraphList.get(i));
//			System.out.println("molecule1: " + structureGraphList.get(i).toString());
//			System.out.println("sketch1: {}" + Arrays.deepToString(fingerprints1.toArray()));
			for (int j = i + 1; j < structureGraphList.size(); j++) {
				Set<Integer> fingerprints2 = getFingerprintSet(structureGraphList.get(j));
				double similarity = Jaccard.calculateSimilarity(fingerprints1, fingerprints2) * 100.;
//				System.out.println("molecule2: " + structureGraphList.get(j).toString());
//				System.out.println("sketch1: {}" + Arrays.deepToString(fingerprints2.toArray()));
				System.out.println("Similarity between Graph" + i + " and Graph" + j + " is " + similarity + "%");
			}

		}
	}

	private static Set<Integer> fingerprintSet;

	public static Set<Integer> getFingerprintSet(UndirectedGraph<AtomVertex, BoundEdge> structureGraph) {
		fingerprintSet = new TreeSet<>();
		Set<AtomVertex> atoms = structureGraph.vertexSet();
		for (AtomVertex a : atoms) {
			dfsAllPathTravel(new ArrayList<>(), null, a, structureGraph);
		}
		System.out.println("\n");
		return fingerprintSet;
	}

	private static void dfsAllPathTravel(List<AtomVertex> path, AtomVertex lastVertex, AtomVertex vertex,
			UndirectedGraph<AtomVertex, BoundEdge> structureGraph) {

		List<AtomVertex> nextPath = new ArrayList<>();
		if (lastVertex != null)
			nextPath.add(lastVertex);
		nextPath.add(vertex);

		fingerprintSet.add(Arrays.deepHashCode(nextPath.toArray()));

		List<AtomVertex> neighbors = Graphs.neighborListOf(structureGraph, vertex);
		for (AtomVertex n : neighbors) {
			if (nextPath.size() <= 12 && !nextPath.contains(n)) {
				dfsAllPathTravel(nextPath, vertex, n, structureGraph);
			}
		}
	}

	// read graphs in some format and store
	public static List<UndirectedGraph<AtomVertex, BoundEdge>> readGraph(Map<String, String> locZoneMap) {
		List<UndirectedGraph<AtomVertex, BoundEdge>> structureGraphList = new ArrayList<>();

		// first graph
//		String[] graph1 = { "singapore", "ws", "bs", "ds2", "ny", "ds1", "ohio" };
		String[] graph1 = { "11", "ws", "bs", "ds2", "ny", "ds1", "ohio" };
		graph1 = modifyGraphLocation(graph1, locZoneMap);
		UndirectedGraph<AtomVertex, BoundEdge> structureGraph = new SimpleWeightedGraph<>(BoundEdge.class);
		List<AtomVertex> vertices = new ArrayList<>();
		for (String item : graph1) {
			AtomVertex atomVertex = new AtomVertex(item);
			vertices.add(atomVertex);
			structureGraph.addVertex(atomVertex);
		}
		structureGraph.addEdge(vertices.get(0), vertices.get(1), new BoundEdge(0));
		structureGraph.addEdge(vertices.get(1), vertices.get(2), new BoundEdge(0));
		structureGraph.addEdge(vertices.get(2), vertices.get(3), new BoundEdge(0));
		structureGraph.addEdge(vertices.get(3), vertices.get(4), new BoundEdge(0));
		structureGraph.addEdge(vertices.get(1), vertices.get(5), new BoundEdge(0));
		structureGraph.addEdge(vertices.get(5), vertices.get(6), new BoundEdge(0));

		structureGraphList.add(structureGraph);

		// second graph
//		String[] graph2 = { "ohio", "ds1", "ny", "ds2", "singapore", "ws", "bs" };
		String[] graph2 = { "ohio", "ds1", "ny", "ds2", "22", "ws", "bs" };
		graph2 = modifyGraphLocation(graph2, locZoneMap);
		UndirectedGraph<AtomVertex, BoundEdge> structureGraph2 = new SimpleWeightedGraph<>(BoundEdge.class);
		vertices = new ArrayList<>();
		for (String item : graph2) {
			AtomVertex atomVertex = new AtomVertex(item);
			vertices.add(atomVertex);
			structureGraph2.addVertex(atomVertex);
		}

		structureGraph2.addEdge(vertices.get(0), vertices.get(1), new BoundEdge(0));
		structureGraph2.addEdge(vertices.get(2), vertices.get(3), new BoundEdge(0));
		structureGraph2.addEdge(vertices.get(4), vertices.get(5), new BoundEdge(0));
		structureGraph2.addEdge(vertices.get(5), vertices.get(6), new BoundEdge(0));
		structureGraph2.addEdge(vertices.get(5), vertices.get(1), new BoundEdge(0));
		structureGraph2.addEdge(vertices.get(3), vertices.get(6), new BoundEdge(0));

		structureGraphList.add(structureGraph2);
		return structureGraphList;
	}

	public static String[] modifyGraphLocation(String[] graph, Map<String, String> locZoneMap) {
		String[] retGraph = new String[graph.length];
		for (int i = 0; i < graph.length; i++) {
			String val = graph[i];
			if (locZoneMap.containsKey(val))
				val = locZoneMap.get(val);
			retGraph[i] = val;
		}
		return retGraph;
	}

	public static Map<String, String> readLocZone() {
		Map<String, String> locZoneMap = new HashMap<String, String>();

		RestTemplate restTemplate = new RestTemplate();
		// Send request with GET method and default Headers
		LocationZone[] locationZoneArray;
		locationZoneArray = restTemplate.getForObject(URL_DCZONE, LocationZone[].class);

		for (LocationZone locationZone : locationZoneArray)
			locZoneMap.put(locationZone.getId(), locationZone.getZone());

		return locZoneMap;
	}

}
