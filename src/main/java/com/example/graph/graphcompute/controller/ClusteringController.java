package com.example.graph.graphcompute.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;

import com.example.graph.graphcompute.clustering.PAMClustering;
import com.example.graph.graphcompute.db.model.DataCenter;
import com.example.graph.graphcompute.db.model.Region;
import com.example.graph.graphcompute.model.DataCent;
import com.example.graph.graphcompute.model.TwoDataCenterValues;
import com.example.graph.graphcompute.model.Zone;
import com.example.graph.graphcompute.repository.DataCenterRepository;
import com.example.graph.graphcompute.repository.RegionRepository;

import smile.stat.hypothesis.KSTest;

@Controller
public class ClusteringController {
	private final int NUM_CLUSTER = 4;
	private String URL_DC;

	private TwoDataCenterValues[] twoDataCenterValuesArray;// two data center name, their latency and bandwidth

	@Autowired
	private DataCenterRepository dataCenterRepository;
	@Autowired
	private RegionRepository regionRepository;

	private Map<String, List<Double>> dataSetValuesMap = new HashMap<>(); // add latency to each dataset
	// store all data center that have values
	private List<String> dcList = new ArrayList<String>();
	// dc1 mapped to dc2 with latency using distance metric (Kolmogorov–Smirnov
	// statistic)
	private Map<String, Map<String, Double>> dataSetToOtherValuesMap = new HashMap<>();
	// zonemap with list of datacenters
	private Map<String, List<String>> zoneDCMap = new HashMap<String, List<String>>();
	// datacenter linked to each zone
	private Map<String, String> dcZoneMap = new HashMap<String, String>();

	public void run() {
		RestTemplate restTemplate = new RestTemplate();

		// Send request with GET method and default Headers.
		this.twoDataCenterValuesArray = restTemplate.getForObject(URL_DC, TwoDataCenterValues[].class);
		// replace datacenter name with its location
		replaceDCNameWithLocation(this.twoDataCenterValuesArray);

		this.dataSetValuesMap = addDCValues(twoDataCenterValuesArray, dataSetValuesMap);
		this.dataSetToOtherValuesMap = computeDistance(this.dataSetValuesMap);

		List<PAMClustering.Cluster> clusterList = cluster(this.dataSetToOtherValuesMap);
		findZone(clusterList); // store maps in zoneDCMap and dcZoneMap

		System.out.println("clustered");
	}

	// change datacenter name with its location
	public void replaceDCNameWithLocation(TwoDataCenterValues[] twoDataCenterValuesArray) {
		for (TwoDataCenterValues twoDataCenterValues : twoDataCenterValuesArray) {
			DataCenter dc1 = dataCenterRepository.findByName(twoDataCenterValues.getDc1());
			Long id1 = dc1.getRegionId();
			Optional<Region> region1 = regionRepository.findById(id1);
			DataCenter dc2 = dataCenterRepository.findByName(twoDataCenterValues.getDc1());
			long id2 = dc2.getRegionId();
			Optional<Region> region2 = regionRepository.findById(id2);

			if (region1.isPresent())
				twoDataCenterValues.setDc1(region1.get().getLocation());
			if (region2.isPresent())
				twoDataCenterValues.setDc1(region2.get().getLocation());
		}
	}

	// store maps in zoneDCMap and dcZoneMap
	public void findZone(List<PAMClustering.Cluster> clusterList) {
		int i = 1;
		for (PAMClustering.Cluster cluster : clusterList) {
			this.zoneDCMap.put("zone" + i, cluster.getDataCenterList());
			for (String dcName : cluster.getDataCenterList())
				this.dcZoneMap.put(dcName, "zone" + i);
			i++;
		}
	}

	// get zone for the provided datacenter
	public DataCent getDC(String dcName) {
		DataCent dc = new DataCent();
		dc.setName(dcName);
		dc.setZoneID(dcZoneMap.get(dcName));
		return dc;
	}

	// get a list of datacenters for the provided zone name
	public Zone getZone(String zoneName) {
		Zone zone = new Zone();
		zone.setName(zoneName);
		zone.setDcList(zoneDCMap.get(zoneName));
		return zone;
	}

	// cluster
	public List<PAMClustering.Cluster> cluster(Map<String, Map<String, Double>> dataSetToOtherValuesMap) {
		PAMClustering clustering = new PAMClustering();
		clustering.setNumCluster(this.NUM_CLUSTER);
		clustering.setDataSetToOtherValuesMap(dataSetToOtherValuesMap);
		return (clustering.run());
	}

	// add values to the hashMap
	public Map<String, List<Double>> addDCValues(TwoDataCenterValues[] twoDataCenterValuesArray,
			Map<String, List<Double>> dataSetValuesMap) {
		for (TwoDataCenterValues twoDataCenterValues : twoDataCenterValuesArray) {
			List<Double> valList = new ArrayList<Double>();
			if (dataSetValuesMap.containsKey(twoDataCenterValues.getDc1()))
				valList = dataSetValuesMap.get(twoDataCenterValues.getDc1());
			valList.add(twoDataCenterValues.getLatency());

			dataSetValuesMap.put(twoDataCenterValues.getDc1(), valList);

			if (!dcList.contains(twoDataCenterValues.getDc1()))
				dcList.add(twoDataCenterValues.getDc1());
		}
		return dataSetValuesMap;
	}

	// compute distance between the datacenters and put in a matrix
	public Map<String, Map<String, Double>> computeDistance(Map<String, List<Double>> dataSetValuesMap) {
		Map<String, Map<String, Double>> dataSetToOtherValuesMap = new HashMap<>();
		for (int i = 0; i < dcList.size(); i++) {
			for (int j = 0; j < dcList.size(); j++) {
				if (i == j)
					continue;
				String ds1 = dcList.get(i);
				String ds2 = dcList.get(j);
				List<Double> ds1ValList = dataSetValuesMap.get(ds1);
				List<Double> ds2ValList = dataSetValuesMap.get(ds2);

				double[] ds1Arr = new double[ds1ValList.size()];
				for (int a = 0; a < ds1ValList.size(); a++)
					ds1Arr[a] = ds1ValList.get(a);

				double[] ds2Arr = new double[ds2ValList.size()];
				for (int a = 0; a < ds2ValList.size(); a++)
					ds2Arr[a] = ds2ValList.get(a);

				KSTest ksTest;
				ksTest = KSTest.test(ds1Arr, ds2Arr);

				Map<String, Double> valMap = new HashMap<>();
				if (dataSetToOtherValuesMap.containsKey(ds1))
					valMap = dataSetToOtherValuesMap.get(ds1);
				valMap.put(ds2, ksTest.d);
				dataSetToOtherValuesMap.put(ds1, valMap);

//				System.out.println(i + ": " + j);
			}

		}
		return dataSetToOtherValuesMap;
	}

	public String getURL_DC() {
		return URL_DC;
	}

	public void setURL_DC(String uRL_DC) {
		URL_DC = uRL_DC;
	}

}
