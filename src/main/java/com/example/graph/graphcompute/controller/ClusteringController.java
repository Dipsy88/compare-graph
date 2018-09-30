package com.example.graph.graphcompute.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;

import com.example.graph.graphcompute.clustering.AffinityPropagation;
import com.example.graph.graphcompute.clustering.PAMClustering;
import com.example.graph.graphcompute.db.model.DataCenter;
import com.example.graph.graphcompute.db.model.Region;
import com.example.graph.graphcompute.model.DataCent;
import com.example.graph.graphcompute.model.TwoDataCenterValues;
import com.example.graph.graphcompute.model.ValCount;
import com.example.graph.graphcompute.model.Zone;
import com.example.graph.graphcompute.repository.DataCenterRepository;
import com.example.graph.graphcompute.repository.RegionRepository;

import smile.stat.hypothesis.KSTest;

@Controller
public class ClusteringController {
	private final int NUM_CLUSTER = 4;
	private String URL_DC;

	private TwoDataCenterValues[] twoDataCenterValuesArray;

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
		Map<String, Map<String, Double>> twoDCMap = new HashMap<>();
		twoDCMap = readDC();

		// do clustering
		// 1) PAM clustering
		List<PAMClustering.Cluster> clusterPAM = cluster(twoDCMap);
		System.out.println(clusterPAM);

		// 2) affinity propagation
		// first convert to 2D matrix
		List<String> dataPoints = getListDP(twoDCMap);
		double[][] matrix = convertMatrix(twoDCMap, dataPoints);
		AffinityPropagation affinityPropagation = new AffinityPropagation(matrix);
		List<AffinityPropagation.ClusterIds> clusterIdList = affinityPropagation.run();

		System.out.println("Affinity clustering");
		for (AffinityPropagation.ClusterIds clusterId : clusterIdList) {
			String text = "";
			for (Integer item : clusterId.getDataCenterIdList()) {
				text += dataPoints.get(item) + " ";
			}
			System.out.println(text);
		}

//		List<PAMClustering.Cluster> clusterList = cluster(this.dataSetToOtherValuesMap);
//		findZone(clusterPAM); // store maps in zoneDCMap and dcZoneMap
		findZone(clusterIdList, dataPoints);

		System.out.println("clustered");
	}

	// put data in a two dimensional matrix
	public static double[][] convertMatrix(Map<String, Map<String, Double>> dataSetMap, List<String> dataPoints) {
		double ret[][] = new double[dataSetMap.size()][dataSetMap.size()];
		for (int i = 0; i < dataPoints.size(); i++) {
			for (int j = 0; j < dataPoints.size(); j++) {
				if (i == j) {
					ret[i][j] = 0;
					continue;
				}
				// connection to other datapoint exists
				if (dataSetMap.get(dataPoints.get(i)).containsKey(dataPoints.get(j)))
					ret[i][j] = dataSetMap.get(dataPoints.get(i)).get(dataPoints.get(j));
				// connection does not exist
				// check if other way connection exits
				else {
					if (dataSetMap.get(dataPoints.get(j)).containsKey(dataPoints.get(i)))
						ret[i][j] = dataSetMap.get(dataPoints.get(j)).get(dataPoints.get(i));
					else
						ret[i][j] = -1;
				}
			}
		}
		return ret;
	}

	// get a list of data centers
	public static List<String> getListDP(Map<String, Map<String, Double>> dataSetMap) {
		List<String> dpList = new ArrayList<String>();
		for (String key : dataSetMap.keySet())
			dpList.add(key);
		return dpList;
	}

	// get request to read the latency, bandwidth and return in a form of 2d array
	public Map<String, Map<String, Double>> readDC() {
		RestTemplate restTemplate = new RestTemplate();
		// two data center name, their latency and bandwidth
		TwoDataCenterValues[] twoDataCenterValuesArray;
		// Send request with GET method and default Headers.
		twoDataCenterValuesArray = restTemplate.getForObject(URL_DC, TwoDataCenterValues[].class);
		// replace datacenter name with its location
		// two data center name, their latency and bandwidth
		TwoDataCenterValues[] twoDataCenterLocationArray;
		twoDataCenterLocationArray = replaceDCNameWithLocation(twoDataCenterValuesArray);

		// store in an array
		return calculateLatency(twoDataCenterLocationArray);
	}

	// return the latency and bandwidth in form of 2d array
	public Map<String, Map<String, Double>> calculateLatency(TwoDataCenterValues[] twoDataCenterLocationArray) {
		Map<String, Map<String, ValCount>> dcValMap = new HashMap<>();
		for (TwoDataCenterValues twoDataCenter : twoDataCenterLocationArray) {
			Map<String, ValCount> toMap = new HashMap<>();
			String dc1Name = twoDataCenter.getDc1();
			String dc2Name = twoDataCenter.getDc2();
			double latency = twoDataCenter.getLatency();
			// key exists then get to
			if (dcValMap.containsKey(dc1Name))
				toMap = dcValMap.get(dc1Name);
			ValCount valCount = new ValCount();
			// to for the key exists already
			if (toMap.containsKey(dc2Name))
				valCount = toMap.get(dc2Name);
			valCount.increaseVal(latency);

			toMap.put(dc2Name, valCount);
			dcValMap.put(dc1Name, toMap);
		}

		Map<String, Map<String, Double>> retMap = new HashMap<>();
		// convert to double val for latency
		for (Map.Entry<String, Map<String, ValCount>> entry : dcValMap.entrySet()) {
			Map<String, ValCount> entryVal = entry.getValue();

			Map<String, Double> toMap = new HashMap<>();
			for (Map.Entry<String, ValCount> toEntry : entryVal.entrySet()) {
				ValCount valCount = toEntry.getValue();
				double latency = valCount.getLatency() / valCount.getCount();
				toMap.put(toEntry.getKey(), latency);
			}
			retMap.put(entry.getKey(), toMap);
		}
		return retMap;
	}

	// change datacenter name with its location
	public TwoDataCenterValues[] replaceDCNameWithLocation(TwoDataCenterValues[] twoDataCenterValuesArray) {
		for (TwoDataCenterValues twoDataCenterValues : twoDataCenterValuesArray) {
			DataCenter dc1 = dataCenterRepository.findByName(twoDataCenterValues.getDc1());
			Long id1 = dc1.getRegionId();
			Optional<Region> region1 = regionRepository.findById(id1);
			DataCenter dc2 = dataCenterRepository.findByName(twoDataCenterValues.getDc2());
			long id2 = dc2.getRegionId();
			Optional<Region> region2 = regionRepository.findById(id2);

			if (region1.isPresent())
				twoDataCenterValues.setDc1(region1.get().getLocation());
			if (region2.isPresent())
				twoDataCenterValues.setDc2(region2.get().getLocation());
		}
		return twoDataCenterValuesArray;
	}

	// store maps in zoneDCMap and dcZoneMap
	// PAM clustering
	public void findZone(List<PAMClustering.Cluster> clusterList) {
		int i = 1;
		for (PAMClustering.Cluster cluster : clusterList) {
			this.zoneDCMap.put("zone" + i, cluster.getDataCenterList());
			for (String dcName : cluster.getDataCenterList())
				this.dcZoneMap.put(dcName, "zone" + i);
			i++;
		}
	}

	// store maps in zoneDCMap and dcZoneMap
	// Affinity Propagation
	public void findZone(List<AffinityPropagation.ClusterIds> clusterIdList, List<String> dataPoints) {
		int i = 1;
		for (AffinityPropagation.ClusterIds clusterId : clusterIdList) {
			List<String> itemList = new ArrayList<String>();
			for (Integer item : clusterId.getDataCenterIdList()) {
				String text = dataPoints.get(item);
				itemList.add(text);
				this.dcZoneMap.put(text, "zone" + i);
			}
			this.zoneDCMap.put("zone" + i, itemList);
			i++;
		}
	}

	// get zone for the locations
	// PAM clustering
	public DataCent getDC(String dcName) {
		DataCent dc = new DataCent();
		dc.setName(dcName);
		dc.setZoneID(dcZoneMap.get(dcName));
		return dc;
	}

	// get zone for the locations
	// Affinity propagation
	public DataCent getDCAffProp(String locName) {
		DataCent dc = new DataCent();

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
