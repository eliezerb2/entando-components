package org.entando.entando.plugins.dashboard.aps.system.services.iot.services;

import com.agiletec.aps.system.exception.ApsSystemException;

import org.entando.entando.plugins.dashboard.aps.system.services.iot.model.MeasurementTemplate;

public interface IMeasurementTemplateService {
  
  void save(MeasurementTemplate measurement) throws ApsSystemException;

  MeasurementTemplate getById(String id) throws ApsSystemException;

  MeasurementTemplate getByDashboardIdAndDatasourceCode(int dashboardId, String datasourceCode)
      throws ApsSystemException;
}