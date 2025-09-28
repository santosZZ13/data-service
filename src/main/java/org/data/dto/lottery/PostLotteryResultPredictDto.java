package org.data.dto.lottery;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.data.dto.common.lottery.LotteryDto;

import java.util.List;

public interface PostLotteryResultPredictDto {
	@Builder
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	class Request {
		private LotteryResultData data;
	}


	@Builder
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	class LotteryResultData {
		private long phaseId;
		private String startTime;
		private String endTime;

		private Double balance;
		private Double targetBalance;
		private Double currentBalance;

		private int total;
		private int totalJoined;
		private long totalBet;
		private int totalWin;
		private int totalLose;



		private Double moneyWin;
		private Double moneyLose;
		private int profit;


		private List<LotteryDto> results;
	}

	@Builder
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	class Response {
		private String message;
	}
}
