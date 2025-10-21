package org.data.response;

import lombok.Data;

@Data
public class ErrorDetail {
	private String field;
	private String message;
}
