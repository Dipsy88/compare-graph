package com.example.graph.graphcompute.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.graph.graphcompute.db.model.Region;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {

}
