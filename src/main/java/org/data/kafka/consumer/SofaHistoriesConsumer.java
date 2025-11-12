package org.data.kafka.consumer;

import lombok.AllArgsConstructor;
import org.data.cache.SofaCacheId;
import org.data.external.sofa.model.SofaResponse;
import org.data.external.sofa.model.TeamAnalysis;
import org.data.external.sofa.service.SofaAnalysis;
import org.data.external.sofa.service.SofaApiService;
import org.data.kafka.message.TeamHistoriesJob;
import org.data.redis.SofaRedisCache;
import org.data.repository.SofaRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@AllArgsConstructor
public class SofaHistoriesConsumer {

	private final SofaApiService sofaApiService;

	private final SofaAnalysis sofaAnalysis;

	private final SofaRepository sofaRepository; // To save to DB

	private final SofaCacheId sofaCacheId;  // To cache

	private final SofaRedisCache sofaRedisCache; // To cache in Redis

	private final SimpMessagingTemplate messagingTemplate;  // Để push WebSocket (nếu có)

	@KafkaListener(topics = "team-histories-fetch", groupId = "sofa-histories-group")
	public void processHistories(TeamHistoriesJob job) {
		try {
			List<Integer> teamIds = job.getTeamIds();
			Set<Integer> uniqueTeamIds = new HashSet<>(teamIds);

			Map<Integer, List<SofaResponse.SofaMatchResponseDetail>> histories = sofaApiService.getHistoriesByTeamIds(uniqueTeamIds);

			// Analyze per team
			Map<Integer, TeamAnalysis> teamAnalyses = new HashMap<>();

			for (Integer teamId : job.getTeamIds()) {
				List<SofaResponse.SofaMatchResponseDetail> teamHistory = histories.get(teamId);
				TeamAnalysis analysis = sofaAnalysis.getTeamAnalysis(teamId, teamHistory);
				teamAnalyses.put(teamId, analysis);
				// Save to DB/cache
//				sofaCacheId.putTeamHistory(teamId, teamHistory);
				// Giả sử saveTeamAnalysisToDB(analysis);
				sofaRedisCache.cacheTeamHistory(teamId, teamHistory);
			}

			// Update matches in DB with analysis (dựa trên date)
			// Ví dụ: Fetch matches by date từ DB, attach analysis, save back

			// Push notification via WebSocket
			if (messagingTemplate != null) {
				messagingTemplate.convertAndSend("/topic/analysis/" + job.getRequestId(), "Analysis completed");
			}

		} catch (Exception e) {
			// Handle error: Log, retry, dead-letter queue
		}
	}
}
