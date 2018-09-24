package com.example.graph.graphcompute.model;

import java.util.ArrayList;
import java.util.List;

public class Zone {
	private String name;
	private List<String> dcList = new ArrayList<String>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getDcList() {
		return dcList;
	}

	public void setDcList(List<String> dcList) {
		this.dcList = dcList;
	}

}
