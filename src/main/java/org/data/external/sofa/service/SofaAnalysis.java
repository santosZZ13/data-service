package org.data.external.sofa.service;


import org.data.external.sofa.model.MatchAnalysis;
import org.data.external.sofa.model.SofaResponse;
import org.data.external.sofa.model.TeamAnalysis;

import java.util.List;

public interface SofaAnalysis {
	TeamAnalysis getTeamAnalysis(Integer teamId, List<SofaResponse.SofaMatchResponseDetail> histories);

	MatchAnalysis getMatchAnalysis(TeamAnalysis homeTeamAnalysis, TeamAnalysis awayTeamAnalysis);
}
