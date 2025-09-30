package org.data.dto.lottery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public interface DeleteLotteryResultPredictDto {
//	@Builder
//	@Data
//	@AllArgsConstructor
//	@NoArgsConstructor
//	class Request {
//		private int lotteryId;
//		private long roundId;
//	}

	@Builder
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	class Response {
		String message;
	}
}
