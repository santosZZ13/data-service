package org.data.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.AllArgsConstructor;
import org.data.external.sofa.model.SofaResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;
@Component
@AllArgsConstructor
public class SofaCacheByDate {
	private final Cache<String, List<SofaResponse.SofaMatchResponseDetail>> matchesByDate;

	public SofaCacheByDate() {
		this.matchesByDate = Caffeine.newBuilder()
				.expireAfterWrite(1, TimeUnit.HOURS) // Cache hết hạn sau 1 giờ
				.maximumSize(1000) // Giới hạn 1000 đội
				.build();
	}

	public List<SofaResponse.SofaMatchResponseDetail> getMatchesByDateCache(String date) {
		return matchesByDate.getIfPresent(date);
	}

	public void putTeamHistory(String date, List<SofaResponse.SofaMatchResponseDetail> matches) {
		matchesByDate.put(date, matches);
	}
}
