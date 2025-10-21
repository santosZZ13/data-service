package org.data.external.sofa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.data.dto.ex.GetAnalystDto;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchAnalysis {
	private TeamAnalysis homeTeamAnalysis;
	private TeamAnalysis awayTeamAnalysis;
	private Double over05Index;
	private Double over15Index;
	private Double over25Index;
	private Double bttsIndex;
	private Double firstHalfOver05Index;
	private Double firstHalfOver15Index;
	private Double firstHalfBttsIndex;
	private Integer matchPriority; // Độ ưu tiên giải đấu (từ tournament.priority)
	private List<HeadToHead> headToHead; // Lịch sử đối đầu (mới)
	private String recommendedBet;

	@Builder
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	class HeadToHead {
		private Integer matchId; // ID trận đấu
		private String result; // Kết quả (VD: "3-2")
		private Integer totalGoals; // Tổng bàn thắng
		private Boolean over15; // Trận có tài 1.5?
		private Boolean over25; // Trận có tài 2.5?
		private Boolean btts; // Cả hai đội ghi bàn?
		private Long timestamp; // Thời gian trận đấu
		private Boolean firstHalfOver05; // Có bàn thắng trong hiệp 1?
		private Boolean firstHalfOver15; // Có trên 1.5 bàn trong hiệp 1?
	}
}
