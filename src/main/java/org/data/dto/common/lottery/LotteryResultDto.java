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
public class LotteryResultDto {
	private long roundId;
	private int lotteryId;
	private long roundTime;
//	private long closeTime;
	private String specialPrize;
	private String resultSpecialPrize;

}
