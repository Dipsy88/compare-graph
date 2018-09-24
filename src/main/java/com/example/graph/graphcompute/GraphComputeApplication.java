package com.example.graph.graphcompute;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;

import com.example.graph.graphcompute.controller.ClusteringController;
import com.example.graph.graphcompute.model.GetPropertyValues;

@SpringBootApplication
@EnableAsync
public class GraphComputeApplication {

	@Autowired
	ClusteringController clusteringController;

	private static GetPropertyValues propValues = new GetPropertyValues(); // store config

	public static void main(String[] args) {
		SpringApplication.run(GraphComputeApplication.class, args);
	}

	// To run multiple threads
	@Bean
	public TaskExecutor taskExecutor() {
		return new SimpleAsyncTaskExecutor(); // Or use another one of your liking
	}

	@Bean
	public CommandLineRunner schedulingRunner(TaskExecutor executor) {
		return new CommandLineRunner() {
			public void run(String... args) throws Exception {
				propValues.readValues(); // read config file

				clusteringController.setURL_DC(propValues.getUrlDC());
				clusteringController.run();
			}
		};
	}

}
