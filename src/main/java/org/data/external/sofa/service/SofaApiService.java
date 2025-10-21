package org.data.external.sofa.service;

import org.data.external.sofa.model.SofaResponse;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SofaApiService {
	List<SofaResponse.SofaMatchResponseDetail> getMatchesByIdAndLimit(Integer teamId, Integer limit);

	List<SofaResponse.SofaMatchResponseDetail> getMatchesByDate(String date);

	Map<Integer, List<SofaResponse.SofaMatchResponseDetail>> getHistoriesByTeamIds(Set<Integer> teamIds);
}
