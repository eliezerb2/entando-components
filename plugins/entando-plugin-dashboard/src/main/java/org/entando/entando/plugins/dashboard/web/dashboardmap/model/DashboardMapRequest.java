/*
 *
 * <Your licensing text here>
 *
 */
package org.entando.entando.plugins.dashboard.web.dashboardmap.model;


import javax.validation.constraints.*;
import org.hibernate.validator.constraints.NotBlank;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.entando.entando.web.common.json.JsonDateDeserializer;
import org.entando.entando.web.common.json.JsonDateSerializer;

public class DashboardMapRequest {

    @NotNull(message = "dashboardMap.id.notBlank")	
	private int id;

	@Size(max = 50, message = "string.size.invalid")
	@NotBlank(message = "dashboardMap.widgetId.notBlank")
	private String widgetId;


	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

	public String getWidgetId() {
		return widgetId;
	}
	public void setWidgetId(String widgetId) {
		this.widgetId = widgetId;
	}


}
