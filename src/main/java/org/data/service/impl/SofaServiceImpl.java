package org.data.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.data.dto.ex.GetAnalystDto;
import org.data.dto.sf.GetScheduledMatchByName;
import org.data.dto.sf.GetScheduledMatchesByName;
import org.data.dto.sf.GetSofaMatchesByDate;
import org.data.dto.sf.SaveScheduledMatchDto;
import org.data.external.sofa.model.MatchAnalysis;
import org.data.external.sofa.model.SofaResponse;
import org.data.external.sofa.model.TeamAnalysis;
import org.data.external.sofa.service.SofaAnalysis;
import org.data.external.sofa.service.SofaApiService;
import org.data.repository.SofaRepository;
import org.data.response.ApiResponse;
import org.data.service.SofaService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Log4j2
public class SofaServiceImpl implements SofaService {

	private final SofaRepository sofaRepository;
	private final SofaApiService sofaApiService;
	private final SofaAnalysis sofaAnalysis;

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

		List<SofaResponse.SofaMatchResponseDetail> matchesByDate = sofaRepository.getMatchesByDate(date);
		matchesByDate = matchesByDate.subList(0, 10);
		Set<Integer> teamIds = matchesByDate
				.stream()
				.flatMap(match -> Set.of(match.getHomeTeam().getId(), match.getAwayTeam().getId()).stream())
				.collect(Collectors.toSet());

		Map<Integer, List<SofaResponse.SofaMatchResponseDetail>> teamHistories = sofaApiService.getHistoriesByTeamIds(teamIds);
		List<MatchAnalysis> analyzedMatches = new ArrayList<>();


		for (SofaResponse.SofaMatchResponseDetail sofaDetail : matchesByDate) {

			Integer sofaHomeId = sofaDetail.getHomeTeam().getId();
			Integer sofaAwayId = sofaDetail.getAwayTeam().getId();

			if (sofaHomeId == null || sofaAwayId == null) {
				log.warn("Missing team IDs for match: {} vs {}", sofaDetail.getHomeTeam().getId(), sofaDetail.getAwayTeam().getId());
				continue;
			}

			List<SofaResponse.SofaMatchResponseDetail> historiesForHome = teamHistories.getOrDefault(sofaHomeId, List.of());
			List<SofaResponse.SofaMatchResponseDetail> historiesForAway = teamHistories.getOrDefault(sofaAwayId, List.of());

			TeamAnalysis homeAnalysis = sofaAnalysis.getTeamAnalysis(sofaHomeId, historiesForHome);
			TeamAnalysis awayAnalysis = sofaAnalysis.getTeamAnalysis(sofaAwayId, historiesForAway);
			MatchAnalysis matchAnalysis = sofaAnalysis.getMatchAnalysis(homeAnalysis, awayAnalysis);

			analyzedMatches.add(matchAnalysis);
		}


		AtomicInteger ended = new AtomicInteger();

		matchesByDate.forEach(
				match -> {
					if (match.getStatus().getType().equals("finished")) {
						ended.getAndIncrement();
					}
				}
		);

		GetSofaMatchesByDate.Response response = GetSofaMatchesByDate.Response.builder()
				.matches(matchesByDate)
				.ended(ended.get())
				.size(matchesByDate.size())
				.build();

		return ApiResponse.<GetSofaMatchesByDate.Response>builder()
				.data(response)
				.message("Matches retrieved successfully")
				.build();
	}
}
