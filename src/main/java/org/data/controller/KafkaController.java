package org.data.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KafkaController {

	@Autowired
	private KafkaProducer kafkaProducer;

	@PostMapping("/publish")
	public void sendMessageToKafkaTopic(@RequestParam("message") String message) {
		this.kafkaProducer.sendMessage(message);
	}
}

