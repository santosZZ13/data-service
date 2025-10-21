package org.data.repository;

import org.data.dto.common.sofa.SofaMatchDto;
import org.data.external.sofa.model.SofaResponse;

import java.util.List;

public interface SofaRepository {
	void saveToDB(List<SofaResponse.SofaMatchResponseDetail> matchesDto);

	List<SofaMatchDto> findSofaScheduledMatchesByName(String name);

	List<SofaMatchDto> findSofaMatchByName(String name);

	List<SofaResponse.SofaMatchResponseDetail> getMatchesByDate(String date);
}
