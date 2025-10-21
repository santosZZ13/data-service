package org.data.external.sofa.analysis;

import org.data.external.sofa.model.MatchAnalysis;
import org.data.external.sofa.model.SofaResponse;
import org.data.external.sofa.model.TeamAnalysis;

import java.util.List;

public interface StatsAnalysis {
	TeamAnalysis.TeamStats getTeamStas(List<SofaResponse.SofaMatchResponseDetail> histories, Integer teamId);
	List<TeamAnalysis.RecentMatch> getRecentMatches(List<SofaResponse.SofaMatchResponseDetail> histories, Integer teamId);
}
