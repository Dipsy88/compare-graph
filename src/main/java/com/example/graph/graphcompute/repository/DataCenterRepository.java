package com.example.graph.graphcompute.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.graph.graphcompute.db.model.DataCenter;

@Repository
public interface DataCenterRepository extends JpaRepository<DataCenter, Long> {

	DataCenter findByName(String name);

}
