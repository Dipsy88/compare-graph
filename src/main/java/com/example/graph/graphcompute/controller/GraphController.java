package com.example.graph.graphcompute.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import com.example.graph.graphcompute.model.AtomVertex;
import com.example.graph.graphcompute.model.BoundEdge;
import com.example.graph.graphcompute.util.Jaccard;

@Controller
public class GraphController {
	final private static Logger logger = LoggerFactory.getLogger(GraphController.class);
	private static String[] list1 = { "singapore", "ws", "bs", "ds2", "ny", "ds1", "ohio" };
	private static String[] list2 = { "ohio", "ds1", "nydd", "ds2", "singapore", "ws", "bs" };

	private static UndirectedGraph<AtomVertex, BoundEdge> structureGraph = new SimpleWeightedGraph<>(BoundEdge.class);
	private static UndirectedGraph<AtomVertex, BoundEdge> structureGraph2 = new SimpleWeightedGraph<>(BoundEdge.class);

	public void run() {
		List<AtomVertex> vertices = new ArrayList<>();

		for (String item : list1) {
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

		logger.info("molecule1: " + structureGraph.toString());
		Set<Integer> fingerprints1 = getFingerprintSet(structureGraph);
//		List<Integer> sketch1 = minHasher.getSketchFromFingerprintSet(fingerprints1);
		logger.info("sketch1: {}", Arrays.deepToString(fingerprints1.toArray()));

		vertices = new ArrayList<>();
		for (String item : list2) {
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

		logger.info("molecule2: " + structureGraph2.toString());
		Set<Integer> fingerprints2 = getFingerprintSet(structureGraph2);
//		List<Integer> sketch2 = minHasher.getSketchFromFingerprintSet(fingerprints2);
		logger.info("sketch2: {}", Arrays.deepToString(fingerprints2.toArray()));

		Long start = System.nanoTime();
		logger.info("Jaccard Distance of fingerprints: {}", Jaccard.calculateSimilarity(fingerprints1, fingerprints2));
		logger.info("took: {} ns", System.nanoTime() - start);
		start = System.nanoTime();
//		logger.info("Similarity of MinHash sketches: {}", Tanimoto.calculateSimilarity(sketch1, sketch2));
		logger.info("took: {} ns", System.nanoTime() - start);
	}

	private static Set<Integer> fingerprintSet;

	public static Set<Integer> getFingerprintSet(UndirectedGraph<AtomVertex, BoundEdge> structureGraph) {
//		if (fingerprintSet != null) {
//			return fingerprintSet;
//		}

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
//		System.out.println(nextPath + ":" + fingerprintSet);
//        fingerprintSet.add(fingerprintAtomChain(nextPath));
		// System.out.println(Arrays.deepToString(nextPath.toArray()));

		List<AtomVertex> neighbors = Graphs.neighborListOf(structureGraph, vertex);
		for (AtomVertex n : neighbors) {
			if (nextPath.size() <= 12 && !nextPath.contains(n)) {
				dfsAllPathTravel(nextPath, vertex, n, structureGraph);
			}
		}
	}
}
