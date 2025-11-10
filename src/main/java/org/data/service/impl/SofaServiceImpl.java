package org.data.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.data.dto.sf.GetScheduledMatchByName;
import org.data.dto.sf.GetScheduledMatchesByName;
import org.data.dto.sf.GetSofaMatchesByDate;
import org.data.dto.sf.SaveScheduledMatchDto;
import org.data.external.sofa.model.MatchAnalysis;
import org.data.external.sofa.model.SofaResponse;
import org.data.external.sofa.model.TeamAnalysis;
import org.data.external.sofa.service.SofaAnalysis;
import org.data.external.sofa.service.SofaApiService;
import org.data.kafka.message.TeamHistoriesJob;
import org.data.redis.SofaRedisCache;
import org.data.repository.SofaRepository;
import org.data.response.ApiResponse;
import org.data.service.SofaService;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Log4j2
public class SofaServiceImpl implements SofaService {

	private final SofaRepository sofaRepository;
	private final SofaApiService sofaApiService;
	private final SofaAnalysis sofaAnalysis;
	private final KafkaTemplate<String, TeamHistoriesJob> kafkaTemplate;
	private final SofaRedisCache sofaRedisCache;

	@Override
	public SaveScheduledMatchDto.Response saveScheduledMatches(SaveScheduledMatchDto.Request request) {
//		sofaScheduledMatchRepository.saveSofaScheduledMatches(request.getMatches());
//		return SaveScheduledMatchDto.Response.builder()
//				.message("Scheduled matches saved successfully")
//				.build();
		return null;
	}

	@Override
	public GetScheduledMatchesByName.Response findMatchesByName(String name) {
		return GetScheduledMatchesByName.Response.builder()
				.matches(sofaRepository.findSofaScheduledMatchesByName(name))
				.build();
	}

	@Override
	public GetScheduledMatchByName.Response findMatchByName(String name) {
		return GetScheduledMatchByName.Response.builder()
				.matches(sofaRepository.findSofaMatchByName(name))
				.build();
	}

	@Override
	public ApiResponse<GetSofaMatchesByDate.Response> getMatchesByDate(String date) {

		// Try to get from Redis cache first
		List<SofaResponse.SofaMatchResponseDetail> matchesByDate = sofaRedisCache.getMatchesByDate(date);
		if (Objects.isNull(matchesByDate)) {
			matchesByDate = sofaRepository.getMatchesByDate(date);
			if (Objects.nonNull(matchesByDate) && !matchesByDate.isEmpty()) {
				// Store in Redis cache for future requests
				sofaRedisCache.cacheMatchesByDate(date, matchesByDate);
//				sofaRepository.saveToDB(matchesByDate);
			}
		}
		matchesByDate = matchesByDate.subList(0, 10); // For testing, limit to first 10 matches
		Set<Integer> teamIds = extractTeamIds(matchesByDate);
		List<Integer> missingTeamIds = checkMissingHistories(teamIds); // DB/cache

		GetSofaMatchesByDate.Response response;
		UUID requestId = UUID.randomUUID();

		if (missingTeamIds.isEmpty()) {
			// Full hit - analyze and return results
			response = buildFullResponse(matchesByDate, requestId, "completed");
		} else {
			// Trigger Kafka job to fetch missing histories
			// kafkaTemplate.send("team-histories-fetch", new TeamHistoriesJob(missingTeamIds, UUID.randomUUID().toString()));
			// Gửi 1 message chứa toàn bộ missingTeamIds
			TeamHistoriesJob job = TeamHistoriesJob.builder()
					.requestId(requestId)
					.date(date)
					.teamIds(missingTeamIds) // List 500-1000 IDs
					.build();
			kafkaTemplate.send("team-histories-fetch", job);
			// Return partial response
			response = partialResponse(matchesByDate, requestId, "processing");
		}
		return ApiResponse.<GetSofaMatchesByDate.Response>builder()
				.data(response)
				.message("Success")
				.build();
	}


	private Set<Integer> extractTeamIds(List<SofaResponse.SofaMatchResponseDetail> matches) {
		return matches.stream()
				.flatMap(match -> Set.of(match.getHomeTeam().getId(), match.getAwayTeam().getId()).stream())
				.collect(Collectors.toSet());
	}

	private List<Integer> checkMissingHistories(Set<Integer> teamIds) {
		return teamIds.stream()
				.filter(teamId -> {
					List<SofaResponse.SofaMatchResponseDetail> cached = sofaRedisCache.getTeamHistory(teamId);
					if (cached != null) {
						return false;
					}
//					Optional<TeamHistoryEntity> db = teamHistoryRepository.findByTeamId(teamId);
//					if (db.isPresent() && db.get().getLastUpdated().isAfter(LocalDateTime.now().minusHours(6))) {
//						sofaRedisCache.cacheTeamHistory(teamId, db.get().getHistory());
//						return false;
//					}
					return true;
				})
				.collect(Collectors.toList());
	}

	private GetSofaMatchesByDate.Response partialResponse(
			List<SofaResponse.SofaMatchResponseDetail> matchesByDate,
			UUID requestId,
			String status
	) {

		AtomicInteger countEnded = new AtomicInteger();

		List<GetSofaMatchesByDate.SofaMatchDetail> details = matchesByDate.stream()
				.map(match -> {

					if (match.getStatus().getType().equals("finished")) {
						countEnded.getAndIncrement();
					}

					return GetSofaMatchesByDate.SofaMatchDetail.builder()
							.match(match)
							.homeTeamAnalysis(null)
							.awayTeamAnalysis(null)
							.matchAnalysis(null)
							.build();
				})
				.toList();

		return GetSofaMatchesByDate.Response.builder()
				.matchDetails(details) // Empty details since analysis is pending
				.ended(countEnded.get())
				.size(matchesByDate.size())
				.requestId(requestId.toString())
				.status(status)
				.build();
	}


	private GetSofaMatchesByDate.Response buildFullResponse(
			List<SofaResponse.SofaMatchResponseDetail> matches,
			UUID requestId,
			String status
	) {

		List<GetSofaMatchesByDate.SofaMatchDetail> details = matches.stream()
				.map(match -> {
					Integer homeId = match.getHomeTeam().getId();
					Integer awayId = match.getAwayTeam().getId();

					List<SofaResponse.SofaMatchResponseDetail> homeHist = sofaRedisCache.getTeamHistory(homeId);
					List<SofaResponse.SofaMatchResponseDetail> awayHist = sofaRedisCache.getTeamHistory(awayId);

					TeamAnalysis homeAnalysis = sofaAnalysis.getTeamAnalysis(homeId, homeHist);
					TeamAnalysis awayAnalysis = sofaAnalysis.getTeamAnalysis(awayId, awayHist);
					MatchAnalysis matchAnalysis = sofaAnalysis.getMatchAnalysis(homeAnalysis, awayAnalysis);

					return GetSofaMatchesByDate.SofaMatchDetail.builder()
							.match(match)
							.homeTeamAnalysis(homeAnalysis)
							.awayTeamAnalysis(awayAnalysis)
							.matchAnalysis(matchAnalysis)
							.build();
				})
				.collect(Collectors.toList());

		int ended = (int) matches.stream().filter(m -> "finished".equals(m.getStatus().getType())).count();

		return GetSofaMatchesByDate.Response.builder()
				.requestId(requestId.toString())
				.status(status)
				.size(matches.size())
				.ended(ended)
				.matchDetails(details)
				.build();
	}
}
