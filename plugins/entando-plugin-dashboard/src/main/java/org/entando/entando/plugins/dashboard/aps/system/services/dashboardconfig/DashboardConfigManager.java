/*
 *
 *  * Copyright 2019-Present Entando Inc. (http://www.entando.com) All rights reserved.
 *  *
 *  * This library is free software; you can redistribute it and/or modify it under
 *  * the terms of the GNU Lesser General Public License as published by the Free
 *  * Software Foundation; either version 2.1 of the License, or (at your option)
 *  * any later version.
 *  *
 *  * This library is distributed in the hope that it will be useful, but WITHOUT
 *  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  * details.
 *
 */

package org.entando.entando.plugins.dashboard.aps.system.services.dashboardconfig;

import org.entando.entando.plugins.dashboard.aps.system.services.dashboardconfig.event.DashboardConfigChangedEvent;

import java.util.ArrayList;
import java.util.List;

import com.agiletec.aps.system.common.FieldSearchFilter;
import com.agiletec.aps.system.common.AbstractService;
import com.agiletec.aps.system.exception.ApsSystemException;
import com.agiletec.aps.system.services.keygenerator.IKeyGeneratorManager;

import org.entando.entando.plugins.dashboard.aps.system.services.dashboardconfig.model.DatasourcesConfigDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.agiletec.aps.system.common.model.dao.SearcherDaoPaginatedResult;

public class DashboardConfigManager extends AbstractService implements IDashboardConfigManager {

  private static final Logger logger = LoggerFactory.getLogger(DashboardConfigManager.class);

  @Override
  public void init() throws Exception {
    logger.debug("{} ready.", this.getClass().getName());
  }

  @Override
  public DashboardConfig getDashboardConfig(int id) throws ApsSystemException {
    DashboardConfig dashboardConfig = null;
    try {
      dashboardConfig = this.getDashboardConfigDAO().loadDashboardConfig(id);
    } catch (Throwable t) {
      logger.error("Error loading dashboardConfig with id '{}'", id, t);
      throw new ApsSystemException("Error loading dashboardConfig with id: " + id, t);
    }
    return dashboardConfig;
  }

  @Override
  public List<Integer> getDashboardConfigs() throws ApsSystemException {
    List<Integer> dashboardConfigs = new ArrayList<Integer>();
    try {
      dashboardConfigs = this.getDashboardConfigDAO().loadDashboardConfigs();
    } catch (Throwable t) {
      logger.error("Error loading DashboardConfig list", t);
      throw new ApsSystemException("Error loading DashboardConfig ", t);
    }
    return dashboardConfigs;
  }

  @Override
  public List<Integer> searchDashboardConfigs(FieldSearchFilter filters[]) throws ApsSystemException {
    List<Integer> dashboardConfigs = new ArrayList<Integer>();
    try {
      dashboardConfigs = this.getDashboardConfigDAO().searchDashboardConfigs(filters);
    } catch (Throwable t) {
      logger.error("Error searching DashboardConfigs", t);
      throw new ApsSystemException("Error searching DashboardConfigs", t);
    }
    return dashboardConfigs;
  }

  @Override
  public void addDashboardConfig(DashboardConfig dashboardConfig) throws ApsSystemException {
    try {
      int key = this.getKeyGeneratorManager().getUniqueKeyCurrentValue();
      dashboardConfig.setId(key);
      this.getDashboardConfigDAO().insertDashboardConfig(dashboardConfig);
      this.notifyDashboardConfigChangedEvent(dashboardConfig, DashboardConfigChangedEvent.INSERT_OPERATION_CODE);
    } catch (Throwable t) {
      logger.error("Error adding DashboardConfig", t);
      throw new ApsSystemException("Error adding DashboardConfig", t);
    }
  }

  @Override
  public void updateDashboardConfig(DashboardConfig dashboardConfig) throws ApsSystemException {
    try {
      this.getDashboardConfigDAO().updateDashboardConfig(dashboardConfig);
      this.notifyDashboardConfigChangedEvent(dashboardConfig, DashboardConfigChangedEvent.UPDATE_OPERATION_CODE);
    } catch (Throwable t) {
      logger.error("Error updating DashboardConfig", t);
      throw new ApsSystemException("Error updating DashboardConfig " + dashboardConfig, t);
    }
  }

  @Override
  public void deleteDashboardConfig(int id) throws ApsSystemException {
    try {
      DashboardConfig dashboardConfig = this.getDashboardConfig(id);
      this.getDashboardConfigDAO().removeDashboardConfig(id);
      this.notifyDashboardConfigChangedEvent(dashboardConfig, DashboardConfigChangedEvent.REMOVE_OPERATION_CODE);
    } catch (Throwable t) {
      logger.error("Error deleting DashboardConfig with id {}", id, t);
      throw new ApsSystemException("Error deleting DashboardConfig with id:" + id, t);
    }
  }


  private void notifyDashboardConfigChangedEvent(DashboardConfig dashboardConfig, int operationCode) {
    DashboardConfigChangedEvent event = new DashboardConfigChangedEvent();
    event.setDashboardConfig(dashboardConfig);
    event.setOperationCode(operationCode);
    this.notifyEvent(event);
  }

  @SuppressWarnings("rawtypes")
  public SearcherDaoPaginatedResult<DashboardConfig> getDashboardConfigs(FieldSearchFilter[] filters) throws ApsSystemException {
    SearcherDaoPaginatedResult<DashboardConfig> pagedResult = null;
    try {
      List<DashboardConfig> dashboardConfigs = new ArrayList<>();
      int count = this.getDashboardConfigDAO().countDashboardConfigs(filters);

      List<Integer> dashboardConfigNames = this.getDashboardConfigDAO().searchDashboardConfigs(filters);
      for (Integer dashboardConfigName : dashboardConfigNames) {
        dashboardConfigs.add(this.getDashboardConfig(dashboardConfigName));
      }
      pagedResult = new SearcherDaoPaginatedResult<DashboardConfig>(count, dashboardConfigs);
    } catch (Throwable t) {
      logger.error("Error searching dashboardConfigs", t);
      throw new ApsSystemException("Error searching dashboardConfigs", t);
    }
    return pagedResult;
  }

  @Override
  public SearcherDaoPaginatedResult<DashboardConfig> getDashboardConfigs(List<FieldSearchFilter> filters) throws ApsSystemException {
    FieldSearchFilter[] array = null;
    if (null != filters) {
      array = filters.toArray(new FieldSearchFilter[filters.size()]);
    }
    return this.getDashboardConfigs(array);
  }

  @Override
  public DatasourcesConfigDto getDatasourceByDatasourcecodeAndDashboard(
      int dashboardId, String datasourceCode) {
    
    
    return this.getDashboardConfigDAO().loadDatasourceConfigByDatasourceCodeAndDashboardConfig(dashboardId,
        datasourceCode);
  }


  protected IKeyGeneratorManager getKeyGeneratorManager() {
    return _keyGeneratorManager;
  }

  public void setKeyGeneratorManager(IKeyGeneratorManager keyGeneratorManager) {
    this._keyGeneratorManager = keyGeneratorManager;
  }

  public void setDashboardConfigDAO(IDashboardConfigDAO dashboardConfigDAO) {
    this._dashboardConfigDAO = dashboardConfigDAO;
  }

  protected IDashboardConfigDAO getDashboardConfigDAO() {
    return _dashboardConfigDAO;
  }

  private IKeyGeneratorManager _keyGeneratorManager;
  private IDashboardConfigDAO _dashboardConfigDAO;
}
