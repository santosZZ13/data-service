package org.data.dto.sf;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.data.external.sofa.model.MatchAnalysis;
import org.data.external.sofa.model.SofaResponse;
import org.data.external.sofa.model.TeamAnalysis;

import java.util.List;

public interface GetSofaMatchesByDate {

	@Builder
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	class Request {
		private String date;
	}

	@Builder
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	class Response {
		private int size;
		private int ended;
		private List<SofaMatchDetail> matchDetails;
	}

	@Builder
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	class SofaMatchDetail {
		SofaResponse.SofaMatchResponseDetail match;
		TeamAnalysis homeTeamAnalysis;
		TeamAnalysis awayTeamAnalysis;
		MatchAnalysis matchAnalysis;
	}
}
