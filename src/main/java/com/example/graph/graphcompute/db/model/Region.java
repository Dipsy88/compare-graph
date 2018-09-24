package com.example.graph.graphcompute.db.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "region")
@EntityListeners(AuditingEntityListener.class)
public class Region {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	@Column(name = "location")
	private String location;
	@Column(name = "cloudprovider_id")
	private int cloudprovider_id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public int getCloudProvider_id() {
		return cloudprovider_id;
	}

	public void setCloudProvider_id(int cloudprovider_id) {
		this.cloudprovider_id = cloudprovider_id;
	}
}
