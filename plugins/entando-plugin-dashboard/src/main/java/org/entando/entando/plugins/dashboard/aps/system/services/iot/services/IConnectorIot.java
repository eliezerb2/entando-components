package org.entando.entando.plugins.dashboard.aps.system.services.iot.services;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.entando.entando.plugins.dashboard.aps.system.services.dashboardconfig.model.DashboardConfigDto;
import org.entando.entando.plugins.dashboard.aps.system.services.dashboardconfig.model.DatasourcesConfigDto;
import org.entando.entando.plugins.dashboard.aps.system.services.iot.model.IDashboardDatasourceDto;
import org.entando.entando.plugins.dashboard.aps.system.services.iot.model.MeasurementConfig;
import org.entando.entando.plugins.dashboard.aps.system.services.iot.model.MeasurementTemplate;
import org.entando.entando.plugins.dashboard.aps.system.services.storage.IotMessage;
import org.entando.entando.web.common.model.PagedMetadata;
import org.entando.entando.web.common.model.RestListRequest;

import com.agiletec.aps.system.exception.ApsSystemException;

public interface IConnectorIot {

  /**
   * Returns true is the implementation of this processor fits the provided widget code
   */
  boolean supports(String connectorType);

  boolean pingDevice(IDashboardDatasourceDto dashboardDatasourceDto) throws IOException;

  List<DatasourcesConfigDto> getAllDevices(DashboardConfigDto dashboardConfigDto);

  void saveMeasurementTemplate(IDashboardDatasourceDto dashboardDatasource)
      throws ApsSystemException;

  void saveDeviceMeasurement(IDashboardDatasourceDto dashboardDatasourceDto,
      String measurementBody);

  PagedMetadata<IotMessage> getMeasurements(IDashboardDatasourceDto dashboardSitewhereDatasourceDto,
      Date startDate, Date endDate, RestListRequest restListRequest) throws RuntimeException;

  MeasurementConfig getMeasurementConfig(IDashboardDatasourceDto dto);

  MeasurementTemplate getDeviceMeasurementSchema(IDashboardDatasourceDto dto);

  String getServerType();
}