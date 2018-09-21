package com.example.graph.graphcompute.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;

import com.example.graph.graphcompute.model.TwoDataCenterValues;

import smile.stat.hypothesis.KSTest;

@Controller
public class GraphComputeController {

	private String URL_DC;

	private TwoDataCenterValues[] twoDataCenterValuesArray;// two data center name, their latency and bandwidth

	private Map<String, List<Double>> dataSetValuesMap = new HashMap<>(); // add latency to each dataset
	// store all data center that have values
	private List<String> dcList = new ArrayList<String>();
	// dc1 mapped to dc2 with latency using distance metric (Kolmogorov–Smirnov
	// statistic)
	private Map<String, Map<String, Double>> dataSetToOtherValuesMap = new HashMap<>();

	public void run() {
		RestTemplate restTemplate = new RestTemplate();

		// Send request with GET method and default Headers.
		twoDataCenterValuesArray = restTemplate.getForObject(URL_DC, TwoDataCenterValues[].class);
		this.dataSetValuesMap = addDCValues(twoDataCenterValuesArray, dataSetValuesMap);
		computeDistance(this.dataSetValuesMap);
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

	public void computeDistance(Map<String, List<Double>> dataSetValuesMap) {
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
					ds1Arr[i] = ds1ValList.get(i);

				double[] ds2Arr = new double[ds2ValList.size()];
				for (int a = 0; a < ds2ValList.size(); a++)
					ds2Arr[i] = ds2ValList.get(i);

				KSTest ksTest;
				ksTest = KSTest.test(ds1Arr, ds2Arr);

				System.out.println(ksTest.d);
			}

		}

	}

	public String getURL_DC() {
		return URL_DC;
	}

	public void setURL_DC(String uRL_DC) {
		URL_DC = uRL_DC;
	}

}
