package org.data.dto.common.lottery;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PredictedResultDto {
	private long roundId;
	private String predict;
	private String result;
	private String status;
	private String specialPrize;
	private String resultSpecialPrize;
	private Double betAmount;

	private List<String> listedNumbers;
	private List<String> listedNumbersSpecialPrize;

	private Double profit;
	private Double loss;
	private Double win;
}

