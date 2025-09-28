package org.data.persistent.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "lottery_prediction")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PredictionLotteryEntity {
	private String id;
	private long phaseId;

	private Double balance;
	private Double currentBalance;
	private Double targetBalance;

	private LocalDateTime startTime;
	private LocalDateTime endTime;

	private int total;
	private int totalJoined;
	private long totalBet;
	private int totalWin;
	private int totalLose;

	private Double moneyWin;
	private Double moneyLose;
	private Double profit;

	private List<LotteryPredictionEntity> results;
	private LocalDateTime created;
	private LocalDateTime updated;

	@Builder
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class LotteryPredictionEntity {
		private LotteryResultEntity lotteryResultEntity;
		private PredictedResultEntity predictedResultEntity;
	}


	@Builder
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class LotteryResultEntity {
		private long roundId;
		private int lotteryID;
		private long roundTime;
		private String specialPrize;
		private String resultSpecialPrize;
	}

	@Builder
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class PredictedResultEntity {
		private long roundId;
		private String predict;
		private String result;
		private String status;
		private String specialPrize;
		private String resultSpecialPrize;
		private List<String> listedNumbersSpecialPrize;
		private List<String> listedNumbers;
		private Double betAmount;
		private Double profit;
		private Double loss;
		private Double win;
	}

}
