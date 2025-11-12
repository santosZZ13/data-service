package org.data.service;

import org.data.dto.sf.GetScheduledMatchByName;
import org.data.dto.sf.GetScheduledMatchesByName;
import org.data.dto.sf.GetSofaMatchesByDate;
import org.data.dto.sf.SaveScheduledMatchDto;
import org.data.response.ApiResponse;

public interface SofaService {
	/**
	 * Saves scheduled matches.
	 *
	 * @param request the request containing matches to be saved
	 * @return a response indicating the result of the save operation
	 */
	SaveScheduledMatchDto.Response saveScheduledMatches(SaveScheduledMatchDto.Request request);

	/**
	 * Finds matches by name.
	 *
	 * @param name the name of the match to search for
	 * @return a response containing the matches found
	 */
	GetScheduledMatchesByName.Response findMatchesByName(String name);

	GetScheduledMatchByName.Response findMatchByName(String name);

	/**
	 * Gets matches by date.
	 * @param date the date to filter matches
	 * @return a response containing the matches on the specified date
	 */
	ApiResponse<GetSofaMatchesByDate.Response> getMatchesByDate(String date);
}
