package com.example.blazepersistencesample;

import java.util.List;

import org.slf4j.Logger;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class TestLogUtility {

	private final Logger log;

	public void outputResultList(final String processName, final List<?> resultList) {

		outputProcessStart(processName);
		// @formatter:off
		resultList.stream().forEach(e -> {
			log.info(e.toString());
		});
		// @formatter:on
		outputProcessEnd(processName);
	}

	private void outputProcessStart(final String processName) {
		outputProcessLabel(processName + " [Start]");
	}

	private void outputProcessEnd(final String processName) {
		outputProcessLabel(processName + " [End]");
	}

	private void outputProcessLabel(final String processName) {
		log.info("■{}", processName);
	}
}
