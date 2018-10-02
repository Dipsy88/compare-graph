package com.example.graph.graphcompute.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.SimpleWeightedGraph;

import com.example.graph.graphcompute.model.AtomVertex;
import com.example.graph.graphcompute.model.BoundEdge;

public class ReadGraphs {
	private List<String[]> graphList = new ArrayList<>();
	private List<UndirectedGraph<AtomVertex, BoundEdge>> structureGraphList = new ArrayList<>();

	public void run(Map<String, String> locZoneMap) {
		readGraph(locZoneMap);
	}

	// read graphs in some format and store
	public void readGraph(Map<String, String> locZoneMap) {
		List<UndirectedGraph<AtomVertex, BoundEdge>> structureGraphList = new ArrayList<>();

		// first graph
		String[] graph1 = { "singapore", "ws", "bs", "ds2", "ny", "ds1", "ohio" };
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
		String[] graph2 = { "ohio", "ds1", "nydd", "ds2", "singapore", "ws", "bs" };
		graph2 = modifyGraphLocation(graph1, locZoneMap);
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

	}

	public String[] modifyGraphLocation(String[] graph, Map<String, String> locZoneMap) {
		String[] retGraph = new String[graph.length];
		for (int i = 0; i < graph.length; i++) {
			String val = graph[i];
			if (locZoneMap.containsKey(val))
				val = locZoneMap.get(val);
			retGraph[i] = val;
		}
		return retGraph;
	}

}
