package org.data.external.sofa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.data.dto.ex.GetAnalystDto;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamAnalysis {
	private String teamName;
	private Integer teamId;
	private Integer totalMatchesAnalyzed;
	private List<RecentMatch> recentMatches;
	private TeamStats stats;


	@Builder
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class RecentMatch {
		private Integer matchId; // ID trận đấu
		private String opponent; // Tên đối thủ
		private String result; // Kết quả (VD: "2-1")
		private Boolean isHome; // Đội này đá sân nhà hay sân khách
		private Integer totalGoals; // Tổng bàn thắng
		private Boolean over15; // Trận có tài 1.5?
		private Boolean over25; // Trận có tài 2.5?
		private Boolean scoring; // Đội có ghi bàn?
		private Boolean firstHalfOver05; // Có bàn thắng trong hiệp 1?
		private Boolean firstHalfOver15; // Có trên 1.5 bàn trong hiệp 1?
	}

	@Builder
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class TeamStats {

		private double firstHalfOver05Rate;
		private double firstHalfOver15Rate;
		private double firstHalfScoringRate; // Tỷ lệ trận đội ghi bàn hiệp 1

		private double over05Rate;
		private double over15Rate;
		private double over25Rate;

		private double scoringRate; // Tỷ lệ trận đội ghi bàn

		private double averageGoalsScored;
		private double averageGoalsConceded;

		private double firstHalfAverageGoalsScored;
		private double firstHalfAverageGoalsConceded;

		private int wins;
		private int draws;
		private int losses;
	}
}
