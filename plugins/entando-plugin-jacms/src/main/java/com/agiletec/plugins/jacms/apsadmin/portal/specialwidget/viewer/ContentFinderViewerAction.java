/*
 * Copyright 2015-Present Entando Inc. (http://www.entando.com) All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
package com.agiletec.plugins.jacms.apsadmin.portal.specialwidget.viewer;

import com.agiletec.aps.system.common.entity.model.EntitySearchFilter;
import com.agiletec.aps.system.common.model.dao.SearcherDaoPaginatedResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.entando.entando.aps.system.services.widgettype.IWidgetTypeManager;
import org.entando.entando.aps.system.services.widgettype.WidgetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.agiletec.aps.system.services.group.Group;
import com.agiletec.aps.system.services.page.IPage;
import com.agiletec.aps.system.services.page.IPageManager;
import com.agiletec.plugins.jacms.apsadmin.content.ContentFinderAction;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Classe Action che cerca i contenuti per la configurazione dei widget di tipo "Pubblica contenuto singolo".
 *
 * @author E.Santoboni
 */
public class ContentFinderViewerAction extends ContentFinderAction {
    
    private static final Logger _logger = LoggerFactory.getLogger(ContentFinderViewerAction.class);
    
    private String pageCode;
    private int frame = -1;
    private String widgetTypeCode;
    
    private String contentId;
    private String modelId;
    
    private IPageManager pageManager;
    private IWidgetTypeManager widgetTypeManager;
    
    @Override
    public SearcherDaoPaginatedResult<String> getPaginatedContentsId(Integer limit) {
        SearcherDaoPaginatedResult<String> result = null;
		try {
			List<String> allowedGroups = this.getContentGroupCodes();
            EntitySearchFilter[] filters = this.getFilters();
            if (null != limit) {
                filters = ArrayUtils.add(filters, this.getPagerFilter(limit));
            }
			result = this.getContentManager().getPaginatedPublicContentsId(null, false, filters, allowedGroups);
		} catch (Exception e) {
			_logger.error("error loading paginated contents", e);
			throw new RuntimeException("error loading paginated contents", e);
		}
		return result;
    }
	
    @Deprecated
	@Override
    public List<String> getContents() {
        List<String> result = null;
        try {
            List<String> allowedGroups = this.getContentGroupCodes();
            result = this.getContentManager().loadPublicContentsId(null, this.getFilters(), allowedGroups);
        } catch (Throwable t) {
            _logger.error("Error searching contents", t);
            throw new RuntimeException("Error searching contents", t);
        }
        return result;
    }

    /**
     * Esegue l'operazione di richiesta associazione di un contenuto alla showlet. La richiesta viene effettuata
     * nell'interfaccia di ricerca risorse e viene redirezionata alla action che gestisce la configurazione della
     * showlet di pubblicazione contenuto.
     *
     * @return Il codice del risultato dell'azione.
     */
    public String joinContent() {
        return SUCCESS;
    }
    
    @Override
    protected List<String> getContentGroupCodes() {
        List<String> allowedGroups = new ArrayList<>();
        allowedGroups.add(Group.FREE_GROUP_NAME);
        IPage currentPage = this.getCurrentPage();
        if (null != currentPage) {
            allowedGroups.add(currentPage.getGroup());
            Set<String> extraGroups = currentPage.getMetadata().getExtraGroups();
            if (null != extraGroups) {
                allowedGroups.addAll(extraGroups);
            }
        } else {
            _logger.error("Null current page - code{}", this.getPageCode());
        }
        return allowedGroups;
    }

    /**
     * Check if the current user can access the specified page.
     *
     * @param page The page to check against the current user.
     * @return True if the user has can access the given page, false otherwise.
     */
    public boolean isUserAllowed(IPage page) {
        if (page == null) {
            return false;
        }
        String pageGroup = page.getGroup();
        return this.isCurrentUserMemberOf(pageGroup);
    }

    /**
     * Returns the 'bread crumbs' targets.
     *
     * @param pageCode The code of the page being represented in the bread crumbs path.
     * @return The bread crumbs targets requested.
     */
    public List<IPage> getBreadCrumbsTargets(String pageCode) {
        IPage page = this.getPageManager().getDraftPage(pageCode);
        if (null == page) {
            return null;
        }
        List<IPage> pages = new ArrayList<>();
        this.getSubBreadCrumbsTargets(pages, page);
        return pages;
    }
    
    private void getSubBreadCrumbsTargets(List<IPage> pages, IPage current) {
        pages.add(0, current);
        IPage parent = (current.isOnlineInstance())
                ? this.getPageManager().getOnlinePage(current.getParentCode())
                : this.getPageManager().getDraftPage(current.getParentCode());
        if (parent != null && !parent.getCode().equals(current.getCode())) {
            this.getSubBreadCrumbsTargets(pages, parent);
        }
    }
    
    @Deprecated
    public WidgetType getShowletType(String typeCode) {
        return this.getWidgetType(typeCode);
    }
    
    public WidgetType getWidgetType(String typeCode) {
        return this.getWidgetTypeManager().getWidgetType(typeCode);
    }
    
    public IPage getCurrentPage() {
        return this.getPageManager().getDraftPage(this.getPageCode());
    }
    
    public String getPageCode() {
        return pageCode;
    }
    
    public void setPageCode(String pageCode) {
        this.pageCode = pageCode;
    }
    
    public int getFrame() {
        return frame;
    }
    
    public void setFrame(int frame) {
        this.frame = frame;
    }
    
    @Deprecated
    public String getShowletTypeCode() {
        return this.getWidgetTypeCode();
    }
    
    @Deprecated
    public void setShowletTypeCode(String widgetTypeCode) {
        this.setWidgetTypeCode(widgetTypeCode);
    }
    
    public String getWidgetTypeCode() {
        return widgetTypeCode;
    }
    
    public void setWidgetTypeCode(String widgetTypeCode) {
        this.widgetTypeCode = widgetTypeCode;
    }
    
    public String getContentId() {
        return contentId;
    }
    
    public void setContentId(String contentId) {
        this.contentId = contentId;
    }
    
    public String getModelId() {
        return modelId;
    }
    
    public void setModelId(String modelId) {
        this.modelId = modelId;
    }
    
    protected IPageManager getPageManager() {
        return pageManager;
    }
    
    public void setPageManager(IPageManager pageManager) {
        this.pageManager = pageManager;
    }
    
    public IWidgetTypeManager getWidgetTypeManager() {
        return widgetTypeManager;
    }
    
    public void setWidgetTypeManager(IWidgetTypeManager widgetTypeManager) {
        this.widgetTypeManager = widgetTypeManager;
    }
    
}
