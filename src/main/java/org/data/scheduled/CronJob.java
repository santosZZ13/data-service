package org.data.scheduled;


import org.springframework.scheduling.annotation.Scheduled;

public class CronJob {
	@Scheduled(cron = "0 0 3 * * ?")
	public void prewarmToday() {
//		getMatchesByDate(today);
	}
}
