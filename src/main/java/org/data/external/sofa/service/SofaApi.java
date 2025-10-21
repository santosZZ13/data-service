package org.data.external.sofa.service;

import lombok.AllArgsConstructor;
import org.data.cache.SofaCacheByDate;
import org.data.cache.SofaCacheId;
import org.data.config.ApiConfig;
import org.data.exception.ExternalServiceException;
import org.data.exception.TeamNotFoundException;
import org.data.external.sofa.model.SofaResponse;
import org.data.util.request.RestClient;
import org.data.util.response.ErrorCodeRegistry;
import org.springframework.http.HttpMethod;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;

@Component
@AllArgsConstructor
public class SofaApi implements SofaApiService {
	private final RestClient<SofaResponse.Response> restClient;
	private final ApiConfig apiConfig;
	private final SofaCacheByDate sofaCacheByDate;
	private final SofaCacheId sofaCacheByTeamId;
	private final ExecutorService executorService;

	private static final String SCHEDULED_EVENTS_PATTERN = "/sport/football/scheduled-events/%s";
	private static final String SCHEDULED_EVENTS_INVERSE_PATTERN = "/sport/football/scheduled-events/%s/inverse";
	private static final String SCHEDULED_EVENTS_TEAM_PATTERN = "/team/%s/events/last/%s";
	private static final String SCHEDULED_EVENTS_TEAM_INVERSE_PATTERN = "/team/%s/events/last/%s";


	@Retryable(
			value = {org.springframework.web.client.RestClientException.class},
			maxAttempts = 4,
			backoff = @Backoff(delay = 1000, multiplier = 1.5)
	)
	@Override
	public List<SofaResponse.SofaMatchResponseDetail> getMatchesByDate(String date) {
		try {

			List<SofaResponse.SofaMatchResponseDetail> cachedMatchesByDate = sofaCacheByDate.getMatchesByDateCache(date);

			if (Objects.isNull(cachedMatchesByDate)) {
				String scheduledEventUrl = apiConfig.getSofaBaseUrl() + String.format(SCHEDULED_EVENTS_PATTERN, date);
				String scheduledEventInverseUrl = apiConfig.getSofaBaseUrl() + String.format(SCHEDULED_EVENTS_INVERSE_PATTERN, date);

				SofaResponse.Response sofaMatchResponse = restClient.execute(scheduledEventUrl, HttpMethod.GET, null, null, SofaResponse.Response.class);
				SofaResponse.Response sofaMatchInverseResponse = restClient.execute(scheduledEventInverseUrl, HttpMethod.GET, null, null, SofaResponse.Response.class);

				List<SofaResponse.SofaMatchResponseDetail> sofaMatchResponseDetail = new ArrayList<>();
				if (!Objects.isNull(sofaMatchResponse) && Objects.nonNull(sofaMatchResponse.getEvents())) {
					sofaMatchResponseDetail.addAll(sofaMatchResponse.getEvents());
				}

				if (Objects.nonNull(sofaMatchInverseResponse) && Objects.nonNull(sofaMatchInverseResponse.getEvents())) {
					sofaMatchResponseDetail.addAll(sofaMatchInverseResponse.getEvents());
				}

				List<SofaResponse.SofaMatchResponseDetail> matchesByDate = sofaMatchResponseDetail.stream()
						.filter(event -> {
							if (event.getStartTimestamp() == null) {
								return false;
							}
							ZonedDateTime eventDateTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(event.getStartTimestamp()), ZoneId.systemDefault());
							return date.equals(eventDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
						})
						.toList();
				sofaCacheByDate.putTeamHistory(date, matchesByDate);
				return matchesByDate;
			} else {
				return cachedMatchesByDate;
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to fetch matches from Sofa API for date: " + date, e);
		}
	}

	@Override
	public Map<Integer, List<SofaResponse.SofaMatchResponseDetail>> getHistoriesByTeamIds(Set<Integer> teamIds) {
		Map<Integer, List<SofaResponse.SofaMatchResponseDetail>> teamHistories = new HashMap<>();
		List<CompletableFuture<Void>> futures = new ArrayList<>();

		for (Integer teamId : teamIds) {
			CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
				try {
					List<SofaResponse.SofaMatchResponseDetail> history = sofaCacheByTeamId.getTeamHistory(teamId);
					if (history == null) {
						history = getMatchesByIdAndLimit(teamId, 10);
						if (history == null) {
							throw new TeamNotFoundException(
									String.format("No history found for team ID: %d", teamId),
									ErrorCodeRegistry.NOT_FOUND_EVENT
							);
						}
						sofaCacheByTeamId.putTeamHistory(teamId, history);
					}
//					else {
//						// Cache hit, sử dụng dữ liệu từ cache
//						teamHistories.put(teamId, history);
//						return;
//					}
					synchronized (teamHistories) {
						teamHistories.put(teamId, history);
					}
				} catch (Exception e) {
					throw new ExternalServiceException(
							String.format("Failed to fetch history for team ID: %d", teamId),
							ErrorCodeRegistry.EXTERNAL_SERVICE_ERROR
					);
				}
			}, executorService);
			futures.add(future);
		}

		try {
			CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
		} catch (CompletionException e) {
			if (e.getCause() instanceof ExternalServiceException || e.getCause() instanceof TeamNotFoundException) {
				throw (RuntimeException) e.getCause();
			}

			throw new ExternalServiceException(
					ErrorCodeRegistry.EXTERNAL_SERVICE_ERROR,
					"Failed to fetch team histories",
					e
			);
		}
		return teamHistories;
	}


	@Retryable(
			value = {org.springframework.web.client.RestClientException.class},
			maxAttempts = 4,
			backoff = @Backoff(delay = 1000, multiplier = 1.5)
	)
	@Override
	public List<SofaResponse.SofaMatchResponseDetail> getMatchesByIdAndLimit(Integer teamId, Integer limit) {
		try {
			String scheduledEventTeamUrl = apiConfig.getSofaBaseUrl() + String.format(SCHEDULED_EVENTS_TEAM_PATTERN, teamId, 0);
			String scheduledEventTeamInverseUrl = apiConfig.getSofaBaseUrl() + String.format(SCHEDULED_EVENTS_TEAM_INVERSE_PATTERN, teamId, 0);
			// Gọi API cho endpoint 1
			SofaResponse.Response sofaMatchResponse = restClient.execute(
					scheduledEventTeamUrl,
					HttpMethod.GET,
					null,
					null,
					SofaResponse.Response.class
			);
			return sofaMatchResponse.getEvents().stream()
					.filter(match -> match.getStatus() != null && Objects.equals(match.getStatus().getType(), "finished"))
					.filter(match -> match.getStartTimestamp() != null)
					.sorted(Comparator.comparingLong(SofaResponse.SofaMatchResponseDetail::getStartTimestamp).reversed())
					.limit(limit == null ? Integer.MAX_VALUE : limit)
					.toList();
		} catch (Exception e) {
			throw new RuntimeException("Failed to fetch matches from Sofa API for team Id: " + teamId, e);
		}
	}


	@Recover
	public List<SofaResponse.SofaMatchResponseDetail> recoverGetSofaMatchByDate(org.springframework.web.client.RestClientException e, String date) {
		throw new ExternalServiceException(
				ErrorCodeRegistry.EXTERNAL_SERVICE_ERROR,
				"Failed to fetch matches from Sofa API for date: " + date,
				e
		);
	}

	@Recover
	public List<SofaResponse.SofaMatchResponseDetail> recoverGetSofaTeamId(org.springframework.web.client.RestClientException e, Integer teamId, Integer limit) {
		throw new ExternalServiceException(
				ErrorCodeRegistry.EXTERNAL_SERVICE_ERROR,
				String.format("Failed to fetch history for team ID: %d", teamId),
				e
		);
	}
}