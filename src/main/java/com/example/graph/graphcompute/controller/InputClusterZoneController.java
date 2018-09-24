package com.example.graph.graphcompute.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.graph.graphcompute.model.DataCenter;
import com.example.graph.graphcompute.model.Zone;

@RestController
@RequestMapping("/input")
public class InputClusterZoneController {

	@Autowired
	ClusteringController clusteringController;

	@GetMapping("/dc/{dc}")
	public DataCenter retrieveDataCenter(@PathVariable String dc) {
		return (clusteringController.getDC(dc));
	}

	@GetMapping("/zone/{zone}")
	public Zone retrieveZone(@PathVariable String zone) {
		return (clusteringController.getZone(zone));
	}

}