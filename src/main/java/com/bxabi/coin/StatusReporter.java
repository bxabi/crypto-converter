package com.bxabi.coin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusReporter {

	@GetMapping(path = "/status")
	public String isRunning() {
		return "Running";
	}
}
