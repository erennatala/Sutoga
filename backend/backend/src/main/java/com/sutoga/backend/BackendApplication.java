package com.sutoga.backend;

import com.corundumstudio.socketio.SocketIOServer;
import com.sutoga.backend.controller.RecommendationApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication implements CommandLineRunner {
	@Autowired
	private SocketIOServer socketIOServer;

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);

		// Create an instance of RecommendationApiClient
		RecommendationApiClient recommendationApiClient = new RecommendationApiClient("http://6.tcp.eu.ngrok.io:11084");

		// Make API request
		String userId = "123456";
		String recommendations = recommendationApiClient.getRecommendations(userId);

		// Process the recommendations as needed
		System.out.println("Recommendations: " + recommendations);
	}

	@Override
	public void run(String... args) {
		socketIOServer.start();
	}

}