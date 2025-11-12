package org.data.kafka.message;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class TeamHistoriesJob {
	private UUID requestId;
	private String date;
	private List<Integer> teamIds;  // List teamIds cần fetch
}