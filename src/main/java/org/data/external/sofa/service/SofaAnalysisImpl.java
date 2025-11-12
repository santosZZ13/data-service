package org.data.external.sofa.service;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.data.dto.ex.GetAnalystDto;
import org.data.external.sofa.analysis.StatsAnalysis;
import org.data.external.sofa.model.MatchAnalysis;
import org.data.external.sofa.model.SofaResponse;
import org.data.external.sofa.model.TeamAnalysis;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.data.util.analyzer.MatchAnalyzer.fetchHeadToHead;

@Component
@AllArgsConstructor
@Log4j2
public class SofaAnalysisImpl implements SofaAnalysis {

	private final StatsAnalysis statsAnalysis;


	@Override
	public TeamAnalysis getTeamAnalysis(Integer teamId, List<SofaResponse.SofaMatchResponseDetail> histories) {
		TeamAnalysis teamAnalysis = null;
		try {
			if (histories == null || histories.isEmpty()) {
				teamAnalysis = TeamAnalysis.builder()
						.teamId(teamId)
						.totalMatchesAnalyzed(0)
//						.wins(0)
//						.losses(0)
//						.draws(0)
						.build();
			} else {
				TeamAnalysis.TeamStats teamStas = statsAnalysis.getTeamStas(histories, teamId);
				List<TeamAnalysis.RecentMatch> recentMatches = statsAnalysis.getRecentMatches(histories, teamId);
				teamAnalysis = TeamAnalysis.builder()
//						.teamName() //TODO
						.teamId(teamId)
						.totalMatchesAnalyzed(histories.size())
						.stats(teamStas)
						.recentMatches(recentMatches)
						.build();
			}
			return teamAnalysis;
		} catch (RuntimeException e) {
			log.warn("Error in getting the analysis for teamId: {}", teamId);
			throw new RuntimeException(String.format("Error in getting the analysis for teamId: %s", teamId));
		}
	}

	@Override
	public MatchAnalysis getMatchAnalysis(TeamAnalysis homeTeamAnalysis, TeamAnalysis awayTeamAnalysis) {

		int sofaHomeId = homeTeamAnalysis.getTeamId();
		int sofaAwayId = awayTeamAnalysis.getTeamId();


		Double over05Index = calculateOver05Index(homeTeamAnalysis, awayTeamAnalysis);
		Double over15Index = calculateOver15Index(homeTeamAnalysis, awayTeamAnalysis);
		Double over25Index = calculateOver25Index(homeTeamAnalysis, awayTeamAnalysis);
		Double bttsIndex = calculateBttsIndex(homeTeamAnalysis, awayTeamAnalysis);
		Double firstHalfOver05Index = calculateFirstHalfOver05Index(homeTeamAnalysis, awayTeamAnalysis);
		Double firstHalfOver15Index = calculateFirstHalfOver15Index(homeTeamAnalysis, awayTeamAnalysis);
		Double firstHalfBttsIndex = calculateFirstHalfBttsIndex(homeTeamAnalysis, awayTeamAnalysis);
		String recommendedBet = determineRecommendedBet(over15Index, over25Index, bttsIndex, firstHalfOver05Index);

		List<SofaResponse.SofaMatchResponseDetail> headToHead = fetchHeadToHead(
				homeTeamAnalysis.getTeamId(),
				awayTeamAnalysis.getTeamId()
		);

		if (headToHead.isEmpty()) {
			log.warn("No head-to-head data found for teams {} vs {}", sofaHomeId, sofaAwayId);
		}

		return MatchAnalysis.builder()
//				.match(match)
				.homeTeamAnalysis(homeTeamAnalysis)
				.awayTeamAnalysis(awayTeamAnalysis)
				.over05Index(over05Index)
				.over15Index(over15Index)
				.over25Index(over25Index)
				.bttsIndex(bttsIndex)
				.firstHalfOver05Index(firstHalfOver05Index)
				.firstHalfOver15Index(firstHalfOver15Index)
				.firstHalfBttsIndex(firstHalfBttsIndex)
//					.matchPriority(match.getTournamentId()) // Giả định tournamentId
//					.headToHead(headToHeadDtos)
				.recommendedBet(recommendedBet)
				.build();
	}


	/**
	 * Tính chỉ số Over 0.5
	 */
	public static Double calculateOver05Index(TeamAnalysis home, TeamAnalysis away) {
		return 0.6 * (home.getStats().getScoringRate() + away.getStats().getScoringRate()) / 2 +
				0.4 * (home.getStats().getAverageGoalsScored() + away.getStats().getAverageGoalsScored());
	}

	/**
	 * Tính chỉ số Over 1.5
	 */
	public static Double calculateOver15Index(TeamAnalysis home, TeamAnalysis away) {
		return 0.6 * (home.getStats().getOver15Rate() + away.getStats().getOver15Rate()) / 2 +
				0.4 * (home.getStats().getAverageGoalsScored() + away.getStats().getAverageGoalsScored());
	}

	/**
	 * Tính chỉ số Over 2.5
	 */
	public static Double calculateOver25Index(TeamAnalysis home, TeamAnalysis away) {
		return 0.6 * (home.getStats().getOver25Rate() + away.getStats().getOver25Rate()) / 2 +
				0.4 * (home.getStats().getAverageGoalsScored() + away.getStats().getAverageGoalsScored());
	}

	/**
	 * Tính chỉ số BTTS
	 */
	public static Double calculateBttsIndex(TeamAnalysis home, TeamAnalysis away) {
		return 0.6 * (home.getStats().getScoringRate() * away.getStats().getScoringRate()) +
				0.4 * (home.getStats().getAverageGoalsScored() + away.getStats().getAverageGoalsScored());
	}

	/**
	 * Tính chỉ số Over 0.5 hiệp 1
	 */
	public static Double calculateFirstHalfOver05Index(TeamAnalysis home, TeamAnalysis away) {
		return 0.6 * (home.getStats().getFirstHalfScoringRate() + away.getStats().getFirstHalfScoringRate()) / 2 +
				0.4 * (home.getStats().getFirstHalfAverageGoalsScored() + away.getStats().getFirstHalfAverageGoalsScored());
	}

	/**
	 * Tính chỉ số Over 1.5 hiệp 1
	 */
	public static Double calculateFirstHalfOver15Index(TeamAnalysis home, TeamAnalysis away) {
		return 0.6 * (home.getStats().getFirstHalfOver15Rate() + away.getStats().getFirstHalfOver15Rate()) / 2 +
				0.4 * (home.getStats().getFirstHalfAverageGoalsScored() + away.getStats().getFirstHalfAverageGoalsScored());
	}

	/**
	 * Tính chỉ số BTTS hiệp 1
	 */
	public static Double calculateFirstHalfBttsIndex(TeamAnalysis home, TeamAnalysis away) {
		return 0.6 * (home.getStats().getFirstHalfScoringRate() * away.getStats().getFirstHalfScoringRate()) +
				0.4 * (home.getStats().getFirstHalfAverageGoalsScored() + away.getStats().getFirstHalfAverageGoalsScored());
	}

	/**
	 * Xác định gợi ý cược
	 */
	public static String determineRecommendedBet(Double over15Index, Double over25Index, Double bttsIndex, Double firstHalfOver05Index) {
		if (over15Index > 0.7) return "Over 1.5";
		if (over25Index > 0.65) return "Over 2.5";
		if (bttsIndex > 0.7) return "BTTS";
		if (firstHalfOver05Index > 0.8) return "First Half Over 0.5";
		return "No Bet";
	}
}
