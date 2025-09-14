package org.data.external.ex.modal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@lombok.Data
@AllArgsConstructor
@NoArgsConstructor
public class ExBetJerseyResponse {
	private String base;
	private String sleeve;
	private String style;
	private String styleColor;
	private String shirtType;
	private String sleeveDetails;
}
