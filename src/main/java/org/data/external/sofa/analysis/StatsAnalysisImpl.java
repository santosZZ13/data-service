package org.data.external.sofa.analysis;

import org.data.dto.ex.GetAnalystDto;
import org.data.external.sofa.model.SofaResponse;
import org.data.external.sofa.model.TeamAnalysis;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class StatsAnalysisImpl implements StatsAnalysis {
	@Override
	public TeamAnalysis.TeamStats getTeamStas(List<SofaResponse.SofaMatchResponseDetail> histories, Integer teamId) {

		int size = histories.size();

		// Tính tỷ lệ trận đội ghi > 1.5 bàn
		double over15Rate = histories.stream()
				.filter(match -> {
					boolean isHomeTeam = match.getHomeTeam().getId().equals(teamId);
					int teamGoals = isHomeTeam ?
							(getHomeScore(match).getCurrent() != null ? getHomeScore(match).getCurrent() : 0) :
							(getAwayScore(match).getCurrent() != null ? getAwayScore(match).getCurrent() : 0);
					return teamGoals > 1.5;
				})
				.count() / (double) size;

		// Tính tỷ lệ trận đội ghi > 2.5 bàn
		double over25Rate = histories.stream()
				.filter(match -> {
					boolean isHomeTeam = match.getHomeTeam().getId().equals(teamId);
					int teamGoals = isHomeTeam ?
							(getHomeScore(match).getCurrent() != null ? getHomeScore(match).getCurrent() : 0) :
							(getAwayScore(match).getCurrent() != null ? getAwayScore(match).getCurrent() : 0);
					return teamGoals > 2.5;
				})
				.count() / (double) size;

		// Tính tỷ lệ trận đội ghi ít nhất 1 bàn
		double scoringRate = histories.stream()
				.filter(match -> {
					boolean isHomeTeam = match.getHomeTeam().getId().equals(teamId);
					int teamGoals = isHomeTeam ?
							(getHomeScore(match).getCurrent() != null ? getHomeScore(match).getCurrent() : 0) :
							(getAwayScore(match).getCurrent() != null ? getAwayScore(match).getCurrent() : 0);
					return teamGoals > 0;
				})
				.count() / (double) size;

		// Tính tỷ lệ trận đội ghi ít nhất 1 bàn hiệp 1
		double firstHalfOver05Rate = histories.stream()
				.filter(match -> {
					boolean isHomeTeam = match.getHomeTeam().getId().equals(teamId);
					int teamGoals = isHomeTeam ?
							(getHomeScore(match).getPeriod1() != null ? getHomeScore(match).getPeriod1() : 0) :
							(getAwayScore(match).getPeriod1() != null ? getAwayScore(match).getPeriod1() : 0);
					return teamGoals > 0.5;
				})
				.count() / (double) size;

		// Tính tỷ lệ trận đội ghi > 1.5 bàn hiệp 1
		double firstHalfOver15Rate = histories.stream()
				.filter(match -> {
					boolean isHomeTeam = match.getHomeTeam().getId().equals(teamId);
					int teamGoals = isHomeTeam ?
							(getHomeScore(match).getPeriod1() != null ? getHomeScore(match).getPeriod1() : 0) :
							(getAwayScore(match).getPeriod1() != null ? getAwayScore(match).getPeriod1() : 0);
					return teamGoals > 1.5;
				})
				.count() / (double) size;

		// Tính tỷ lệ trận đội ghi ít nhất 1 bàn hiệp 1
		double firstHalfScoringRate = histories.stream()
				.filter(match -> {
					boolean isHomeTeam = match.getHomeTeam().getId().equals(teamId);
					int teamGoals = isHomeTeam ?
							(getHomeScore(match).getPeriod1() != null ? getHomeScore(match).getPeriod1() : 0) :
							(getAwayScore(match).getPeriod1() != null ? getAwayScore(match).getPeriod1() : 0);
					return teamGoals > 0;
				})
				.count() / (double) size;

		// Trung bình bàn ghi
		double averageGoalsScored = histories.stream()
				.mapToDouble(match -> {
					boolean isHomeTeam = match.getHomeTeam().getId().equals(teamId);
					return isHomeTeam ?
							(getHomeScore(match).getCurrent() != null ? getHomeScore(match).getCurrent() : 0) :
							(getAwayScore(match).getCurrent() != null ? getAwayScore(match).getCurrent() : 0);
				})
				.average()
				.orElse(0.0);

		// Trung bình bàn thủng
		double averageGoalsConceded = histories.stream()
				.mapToDouble(match -> {
					boolean isHomeTeam = match.getHomeTeam().getId().equals(teamId);
					return isHomeTeam ?
							(getAwayScore(match).getCurrent() != null ? getAwayScore(match).getCurrent() : 0) :
							(getHomeScore(match).getCurrent() != null ? getHomeScore(match).getCurrent() : 0);
				})
				.average()
				.orElse(0.0);

		// Trung bình bàn ghi hiệp 1
		double firstHalfAverageGoalsScored = histories.stream()
				.mapToDouble(match -> {
					boolean isHomeTeam = match.getHomeTeam().getId().equals(teamId);
					return isHomeTeam ?
							(getHomeScore(match).getPeriod1() != null ? getHomeScore(match).getPeriod1() : 0) :
							(getAwayScore(match).getPeriod1() != null ? getAwayScore(match).getPeriod1() : 0);
				})
				.average()
				.orElse(0.0);

		// Trung bình bàn thủng hiệp 1
		double firstHalfAverageGoalsConceded = histories.stream()
				.mapToDouble(match -> {
					boolean isHomeTeam = match.getHomeTeam().getId().equals(teamId);
					return isHomeTeam ?
							(getAwayScore(match).getPeriod1() != null ? getAwayScore(match).getPeriod1() : 0) :
							(getHomeScore(match).getPeriod1() != null ? getHomeScore(match).getPeriod1() : 0);
				})
				.average()
				.orElse(0.0);

		// Tính thắng, hòa, thua
		int wins = (int) histories.stream()
				.filter(match -> {
					boolean isHomeTeam = match.getHomeTeam().getId().equals(teamId);
					int homeScore = getHomeScore(match).getCurrent() != null ? getHomeScore(match).getCurrent() : 0;
					int awayScore = getAwayScore(match).getCurrent() != null ? getAwayScore(match).getCurrent() : 0;
					return isHomeTeam ? homeScore > awayScore : awayScore > homeScore;
				})
				.count();
		int draws = (int) histories.stream()
				.filter(match -> {
					int homeScore = getHomeScore(match).getCurrent() != null ? getHomeScore(match).getCurrent() : 0;
					int awayScore = getAwayScore(match).getCurrent() != null ? getAwayScore(match).getCurrent() : 0;
					return homeScore == awayScore;
				})
				.count();
		int losses = size - wins - draws;

		return TeamAnalysis.TeamStats.builder()
				.over15Rate(over15Rate)
				.over25Rate(over25Rate)
				.scoringRate(scoringRate)
				.firstHalfOver05Rate(firstHalfOver05Rate)
				.firstHalfOver15Rate(firstHalfOver15Rate)
				.firstHalfScoringRate(firstHalfScoringRate)
				.averageGoalsScored(averageGoalsScored)
				.averageGoalsConceded(averageGoalsConceded)
				.firstHalfAverageGoalsScored(firstHalfAverageGoalsScored)
				.firstHalfAverageGoalsConceded(firstHalfAverageGoalsConceded)
				.wins(wins)
				.draws(draws)
				.losses(losses)
				.build();
	}

	@Override
	public List<TeamAnalysis.RecentMatch> getRecentMatches(List<SofaResponse.SofaMatchResponseDetail> histories, Integer teamId) {
		return histories.stream()
				.limit(5)
				.map(match -> {
					boolean isHome = match.getHomeTeam().getId().equals(teamId);
					int teamGoals = isHome ?
							(getHomeScore(match).getCurrent() != null ? getHomeScore(match).getCurrent() : 0) :
							(getAwayScore(match).getCurrent() != null ? getAwayScore(match).getCurrent() : 0);
					return TeamAnalysis.RecentMatch.builder()
							.matchId(match.getMatchId())
							.opponent(isHome ? match.getAwayTeam().getName() : match.getHomeTeam().getName())
							.result((getHomeScore(match).getCurrent() != null ? getHomeScore(match).getCurrent() : 0) + "-" +
									(getAwayScore(match).getCurrent() != null ? getAwayScore(match).getCurrent() : 0))
							.isHome(isHome)
							.totalGoals(getTotalGoals(match))
							.over15(teamGoals > 1.5)
							.over25(teamGoals > 2.5)
							.scoring(teamGoals > 0)
							.firstHalfOver05((isHome ?
									(getHomeScore(match).getPeriod1() != null ? getHomeScore(match).getPeriod1() : 0) :
									(getAwayScore(match).getPeriod1() != null ? getAwayScore(match).getPeriod1() : 0)) > 0.5)
							.firstHalfOver15((isHome ?
									(getHomeScore(match).getPeriod1() != null ? getHomeScore(match).getPeriod1() : 0) :
									(getAwayScore(match).getPeriod1() != null ? getAwayScore(match).getPeriod1() : 0)) > 1.5)
							.build();
				})
				.collect(Collectors.toList());
	}

	/**
	 * Helper method để lấy tổng số bàn thắng
	 */
	public static int getTotalGoals(SofaResponse.SofaMatchResponseDetail match) {
		if (match == null) {
			return 0;
		}
		int homeScore = getHomeScore(match).getCurrent() != null ? getHomeScore(match).getCurrent() : 0;
		int awayScore = getAwayScore(match).getCurrent() != null ? getAwayScore(match).getCurrent() : 0;
		return homeScore + awayScore;
	}

	/**
	 * Helper method để lấy score đội nhà
	 */
	public static SofaResponse.ScoreResponse getHomeScore(SofaResponse.SofaMatchResponseDetail match) {
		return match.getHomeScore() != null ? match.getHomeScore() : new SofaResponse.ScoreResponse();
	}

	/**
	 * Helper method để lấy score đội khách
	 */
	public static SofaResponse.ScoreResponse getAwayScore(SofaResponse.SofaMatchResponseDetail match) {
		return match.getAwayScore() != null ? match.getAwayScore() : new SofaResponse.ScoreResponse();
	}

	/**
	 * Giả lập lấy lịch sử trận đấu của đội
	 */
	public static List<SofaResponse.SofaMatchResponseDetail> fetchTeamHistory(Integer teamId) {
		return new ArrayList<>();
	}

	/**
	 * Giả lập lấy lịch sử đối đầu
	 */
	public static List<SofaResponse.SofaMatchResponseDetail> fetchHeadToHead(Integer homeTeamId, Integer awayTeamId) {
		return new ArrayList<>();
	}
}
