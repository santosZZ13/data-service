package org.data.external.sofa.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.*;
import org.data.util.utils.DateUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public interface SofaResponse {

	@Builder
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	class Response {
		private List<SofaMatchResponseDetail> events;
		private Boolean hasNextPage;
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	@ToString
	@JsonIgnoreProperties(ignoreUnknown = true)
	class SofaMatchResponseDetail {
		@JsonProperty("id")
		private Integer matchId;
		@JsonProperty("startTimestamp")
		private Long startTimestamp;
		//	@JsonIgnore // Bỏ qua field này khi serialize nếu không cần
		private String startTimeUtc; // Lưu thời gian UTC dưới dạng String
		private TournamentResponse tournament;
		private SeasonResponse season;
		private RoundInfoResponse roundInfo;
		private StatusResponse status;
		private TeamResponse homeTeam;
		private TeamResponse awayTeam;
		private ScoreResponse homeScore;
		private ScoreResponse awayScore;
		private String slug;

		public String getStartTimeUtc() {
			if (startTimestamp != null) {
				ZonedDateTime utcDateTime = Instant.ofEpochSecond(startTimestamp)
						.atZone(ZoneId.of("UTC"));
				return utcDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
			}
			return null;
		}

		public void setStartTimestamp(Long startTimestamp) {
			this.startTimestamp = startTimestamp;
			if (startTimestamp != null) {
				this.startTimeUtc = DateUtils.fromTimestampToUtc(startTimestamp);
			}
		}
	}

	@Builder
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	class RoundInfoResponse {
		private Integer round;
		private String name;
		private Integer cupRoundType; // Thêm trường mới
	}


	@Builder
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	class ScoreResponse {
		private Integer current;
		private Integer display;
		@JsonPropertyDescription("Half Time")
		private Integer period1;
		@JsonPropertyDescription("Second Half")
		private Integer period2;
		private Integer period3;
		@JsonPropertyDescription("Full Time")
		@JsonProperty("normaltime")
		private Integer normalTime;
		@JsonPropertyDescription("Extra Time 1st Half")
		private Integer extra1;
		@JsonPropertyDescription("Extra Time 2nd Half")
		private Integer extra2;
		@JsonPropertyDescription("Overtime")
		private Integer overtime;
		private Integer penalties;
		private Boolean scoreEmpty;
		private Integer aggregated;
	}

	@Builder
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	@ToString
	@JsonIgnoreProperties(ignoreUnknown = true)
	class SeasonResponse {
		private String name;
		private String year;
		private Integer id;
	}

	@Builder
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	class TeamResponse {
		private String name;
		private String slug;
		private String shortName;
		private Integer id;
	}

	@Builder
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	@ToString
	@JsonIgnoreProperties(ignoreUnknown = true)
	class StatusResponse {
		private Integer code;
		private String description;
		private String type;
	}

	@Builder
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	class TournamentResponse {
		private String name;
		private Integer id;
	}
}
