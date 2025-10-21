package org.data.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL) // Bỏ qua các field null trong JSON
@Data
@Builder
public class ApiResponse<T> {
	private Integer status;
	private String message;
	private LocalDateTime timestamp;
	private T data;
	private Map<String, Object> metadata;
	private List<ErrorDetail> errors;
	private String requestId;

//	public ApiResponse(int status, String message, T data) {
//		this.status = status;
//		this.message = message;
//		this.data = data;
//		this.timestamp = LocalDateTime.now();
//	}

}
