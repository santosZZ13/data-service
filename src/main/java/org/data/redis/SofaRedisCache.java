package org.data.redis;

import lombok.AllArgsConstructor;
import org.data.external.sofa.model.SofaResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@AllArgsConstructor
public class SofaRedisCache {
	private final StringRedisTemplate redisTemplate;
	private static final String MATCHES_CACHE_KEY = "sofa:matches:date:%s";
	private static final String TEAM_HISTORY_CACHE_KEY = "sofa:team:history:%d";

	public void cacheMatchesByDate(String date, List<SofaResponse.SofaMatchResponseDetail> matches) {
		String key = String.format(MATCHES_CACHE_KEY, date);
		redisTemplate.opsForValue().set(key, matches, Duration.ofHours(24));
	}

	public List<SofaResponse.SofaMatchResponseDetail> getMatchesByDate(String date) {
		String key = String.format(MATCHES_CACHE_KEY, date);
		return (List<SofaResponse.SofaMatchResponseDetail>) redisTemplate.opsForValue().get(key);
	}

	public void cacheTeamHistory(Integer teamId, List<SofaResponse.SofaMatchResponseDetail> history) {
		String key = String.format(TEAM_HISTORY_CACHE_KEY, teamId);
		redisTemplate.opsForValue().set(key, history, Duration.ofDays(7));
	}

	public List<SofaResponse.SofaMatchResponseDetail> getTeamHistory(Integer teamId) {
		String key = String.format(TEAM_HISTORY_CACHE_KEY, teamId);
		return (List<SofaResponse.SofaMatchResponseDetail>) redisTemplate.opsForValue().get(key);
	}

	public void invalidateTeamHistory(Integer teamId) {
		String key = String.format(TEAM_HISTORY_CACHE_KEY, teamId);
		redisTemplate.delete(key);
	}
}
