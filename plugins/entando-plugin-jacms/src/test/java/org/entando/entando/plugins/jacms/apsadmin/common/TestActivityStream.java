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
package org.entando.entando.plugins.jacms.apsadmin.common;

import com.agiletec.aps.system.SystemConstants;
import com.agiletec.aps.system.services.lang.ILangManager;
import com.agiletec.aps.system.services.page.IPageManager;
import com.agiletec.aps.system.services.role.Permission;
import com.agiletec.aps.system.services.user.UserDetails;
import com.agiletec.apsadmin.ApsAdminBaseTestCase;
import com.agiletec.apsadmin.system.ApsAdminSystemConstants;
import org.entando.entando.aps.system.services.actionlog.model.ActivityStreamInfo;
import com.agiletec.plugins.jacms.aps.system.JacmsSystemConstants;
import com.agiletec.plugins.jacms.aps.system.services.content.IContentManager;
import com.agiletec.plugins.jacms.aps.system.services.content.model.Content;
import com.agiletec.plugins.jacms.apsadmin.content.AbstractContentAction;
import com.agiletec.plugins.jacms.apsadmin.content.ContentActionConstants;

import com.opensymphony.xwork2.Action;
import java.util.Date;

import java.util.List;
import java.util.Properties;

import org.entando.entando.aps.system.services.actionlog.ActionLoggerTestHelper;
import org.entando.entando.aps.system.services.actionlog.IActionLogManager;
import org.entando.entando.aps.system.services.actionlog.model.ActionLogRecord;
import org.entando.entando.aps.system.services.actionlog.model.ActionLogRecordSearchBean;
import org.entando.entando.aps.system.services.actionlog.model.ActivityStreamSeachBean;

/**
 * @author E.Santoboni
 */
public class TestActivityStream extends ApsAdminBaseTestCase {

    private IActionLogManager actionLoggerManager;
    private IPageManager pageManager = null;
    private ILangManager langManager = null;
    private IContentManager contentManager = null;
    private ActionLoggerTestHelper helper;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.init();
        this.helper.cleanRecords();
    }

    public void testSaveNewContent_1() throws Throwable {
        Content content = this.contentManager.loadContent("ART1", false);
        String contentOnSessionMarker = AbstractContentAction.buildContentOnSessionMarker(content, ApsAdminSystemConstants.ADD);
        content.setId(null);
        String contentId = null;
        try {
            this.getRequest().getSession().setAttribute(ContentActionConstants.SESSION_PARAM_NAME_CURRENT_CONTENT_PREXIX + contentOnSessionMarker, content);
            this.initContentAction("/do/jacms/Content", "save", contentOnSessionMarker);
            this.setUserOnSession("admin");
            String result = this.executeAction();
            synchronized (this) {
                this.wait(1000);
            }
            assertEquals(Action.SUCCESS, result);
            contentId = content.getId();
            assertNotNull(this.contentManager.loadContent(contentId, false));
            super.waitThreads(IActionLogManager.LOG_APPENDER_THREAD_NAME_PREFIX);

            ActionLogRecordSearchBean searchBean = this.helper.createSearchBean("admin", null, null, null, null, null);
            List<Integer> ids = this.actionLoggerManager.getActionRecords(searchBean);
            assertEquals(1, ids.size());

            UserDetails currentUser = (UserDetails) super.getRequest().getSession().getAttribute(SystemConstants.SESSIONPARAM_CURRENT_USER);
            List<Integer> activityStream = this.actionLoggerManager.getActivityStream(currentUser);
            assertEquals(activityStream.size(), ids.size());
            assertEquals(activityStream.get(0), ids.get(0));

            ActionLogRecord record = this.actionLoggerManager.getActionRecord(ids.get(0));
            assertEquals("/do/jacms/Content", record.getNamespace());
            assertEquals("save", record.getActionName());
            ActivityStreamInfo asi = record.getActivityStreamInfo();
            assertNotNull(asi);
            assertEquals(1, asi.getActionType());
            assertEquals(Permission.CONTENT_EDITOR, asi.getLinkAuthPermission());
            assertEquals(content.getMainGroup(), asi.getLinkAuthGroup());
            assertEquals("edit", asi.getLinkActionName());
            assertEquals("/do/jacms/Content", asi.getLinkNamespace());
            //assertEquals(1, asi.getLinkParameters().size());
            Properties parameters = asi.getLinkParameters();
            assertEquals(1, parameters.size());
            assertEquals(contentId, parameters.getProperty("contentId"));
        } catch (Throwable t) {
            throw t;
        } finally {
            this.contentManager.deleteContent(content);
            assertNull(this.contentManager.loadContent(contentId, false));
        }
    }

    public void testSaveNewContent_2() throws Throwable {
        Content content = this.contentManager.loadContent("EVN41", false);//"coach" group
        String contentOnSessionMarker = AbstractContentAction.buildContentOnSessionMarker(content, ApsAdminSystemConstants.ADD);
        content.setId(null);
        String contentId = null;
        try {
            this.getRequest().getSession().setAttribute(ContentActionConstants.SESSION_PARAM_NAME_CURRENT_CONTENT_PREXIX + contentOnSessionMarker, content);
            this.initContentAction("/do/jacms/Content", "save", contentOnSessionMarker);
            this.setUserOnSession("admin");
            String result = this.executeAction();
            synchronized (this) {
                this.wait(1000);
            }
            assertEquals(Action.SUCCESS, result);
            contentId = content.getId();
            assertNotNull(this.contentManager.loadContent(contentId, false));
            super.waitThreads(IActionLogManager.LOG_APPENDER_THREAD_NAME_PREFIX);

            ActionLogRecordSearchBean searchBean = this.helper.createSearchBean("admin", null, null, null, null, null);
            List<Integer> ids = this.actionLoggerManager.getActionRecords(searchBean);
            assertEquals(1, ids.size());

            UserDetails editorCustomers = super.getUser("editorCustomers");
            List<Integer> activityStreamCustomerUser = this.actionLoggerManager.getActivityStream(editorCustomers);
            assertEquals(0, activityStreamCustomerUser.size());

            UserDetails editorCoach = super.getUser("editorCoach");
            List<Integer> activityStreamCoachUser = this.actionLoggerManager.getActivityStream(editorCoach);
            assertEquals(1, activityStreamCoachUser.size());
        } catch (Throwable t) {
            throw t;
        } finally {
            this.contentManager.deleteContent(content);
            assertNull(this.contentManager.loadContent(contentId, false));
        }
    }

    public void testActivityStreamSearchBean() throws Throwable {
        Content content = this.contentManager.loadContent("EVN41", false);//"coach" group
        String contentOnSessionMarker = AbstractContentAction.buildContentOnSessionMarker(content, ApsAdminSystemConstants.ADD);
        content.setId(null);
        String contentId = null;
        Date dateBeforeSave = new Date();
        try {
            this.getRequest().getSession().setAttribute(ContentActionConstants.SESSION_PARAM_NAME_CURRENT_CONTENT_PREXIX + contentOnSessionMarker, content);
            this.initContentAction("/do/jacms/Content", "save", contentOnSessionMarker);
            this.setUserOnSession("admin");
            String result = this.executeAction();
            synchronized (this) {
                this.wait(1000);
            }
            assertEquals(Action.SUCCESS, result);
            contentId = content.getId();
            assertNotNull(this.contentManager.loadContent(contentId, false));
            super.waitThreads(IActionLogManager.LOG_APPENDER_THREAD_NAME_PREFIX);
            Date firstDate = new Date();
            ActionLogRecordSearchBean searchBean = this.helper.createSearchBean("admin", null, null, null, null, null);
            List<Integer> ids = this.actionLoggerManager.getActionRecords(searchBean);
            assertEquals(1, ids.size());

            ActivityStreamSeachBean activityStreamSeachBean = new ActivityStreamSeachBean();
            activityStreamSeachBean.setEndCreation(firstDate);
            List<Integer> activityStreamEndDate = this.actionLoggerManager.getActivityStream(activityStreamSeachBean);
            assertEquals(1, activityStreamEndDate.size());

            activityStreamSeachBean = new ActivityStreamSeachBean();
            activityStreamSeachBean.setEndUpdate(dateBeforeSave);
            List<Integer> activityStreamDateBeforeSave = this.actionLoggerManager.getActivityStream(activityStreamSeachBean);
            assertEquals(0, activityStreamDateBeforeSave.size());

            this.getRequest().getSession().setAttribute(ContentActionConstants.SESSION_PARAM_NAME_CURRENT_CONTENT_PREXIX + contentOnSessionMarker, content);
            this.initContentAction("/do/jacms/Content", "save", contentOnSessionMarker);
            this.setUserOnSession("admin");
            result = this.executeAction();
            synchronized (this) {
                this.wait(1000);
            }
            assertEquals(Action.SUCCESS, result);
            contentId = content.getId();
            assertNotNull(this.contentManager.loadContent(contentId, false));
            super.waitThreads(IActionLogManager.LOG_APPENDER_THREAD_NAME_PREFIX);

            activityStreamSeachBean = new ActivityStreamSeachBean();
            activityStreamSeachBean.setStartUpdate(dateBeforeSave);
            activityStreamSeachBean.setEndUpdate(firstDate);
            List<Integer> activityStreamBetweenSave = this.actionLoggerManager.getActivityStream(activityStreamSeachBean);
            assertEquals(1, activityStreamBetweenSave.size());

            activityStreamSeachBean = new ActivityStreamSeachBean();
            activityStreamSeachBean.setStartUpdate(dateBeforeSave);
            activityStreamSeachBean.setEndUpdate(new Date());
            List<Integer> activityStreamBetweenSave2 = this.actionLoggerManager.getActivityStream(activityStreamSeachBean);
            assertEquals(2, activityStreamBetweenSave2.size());
        } catch (Throwable t) {
            throw t;
        } finally {
            this.contentManager.deleteContent(content);
            assertNull(this.contentManager.loadContent(contentId, false));
        }
    }

    public void testLastUpdate() throws Throwable {
        Content content = this.contentManager.loadContent("EVN41", false);//"coach" group
        String contentOnSessionMarker = AbstractContentAction.buildContentOnSessionMarker(content, ApsAdminSystemConstants.ADD);
        content.setId(null);
        String contentId = null;
        try {
            this.getRequest().getSession().setAttribute(ContentActionConstants.SESSION_PARAM_NAME_CURRENT_CONTENT_PREXIX + contentOnSessionMarker, content);
            this.initContentAction("/do/jacms/Content", "save", contentOnSessionMarker);
            this.setUserOnSession("admin");
            String result = this.executeAction();
            synchronized (this) {
                this.wait(1000);
            }
            assertEquals(Action.SUCCESS, result);
            contentId = content.getId();
            assertNotNull(this.contentManager.loadContent(contentId, false));
            super.waitThreads(IActionLogManager.LOG_APPENDER_THREAD_NAME_PREFIX);
            ActionLogRecordSearchBean searchBean = this.helper.createSearchBean("admin", null, null, null, null, null);
            List<Integer> ids = this.actionLoggerManager.getActionRecords(searchBean);
            assertEquals(1, ids.size());

            this.getRequest().getSession().setAttribute(ContentActionConstants.SESSION_PARAM_NAME_CURRENT_CONTENT_PREXIX + contentOnSessionMarker, content);
            this.initContentAction("/do/jacms/Content", "save", contentOnSessionMarker);
            this.setUserOnSession("admin");
            result = this.executeAction();
            synchronized (this) {
                this.wait(1000);
            }
            assertEquals(Action.SUCCESS, result);
            contentId = content.getId();
            assertNotNull(this.contentManager.loadContent(contentId, false));
            super.waitThreads(IActionLogManager.LOG_APPENDER_THREAD_NAME_PREFIX);
            List<Integer> actionRecords = this.actionLoggerManager.getActionRecords(null);
            assertNotNull(actionRecords);
            assertEquals(2, actionRecords.size());
            ActionLogRecord actionRecord = this.actionLoggerManager.getActionRecord(actionRecords.get(1));
            UserDetails adminUser = this.getUser("admin", "admin");
            Date lastUpdateDate = this.actionLoggerManager.lastUpdateDate(adminUser);
            assertEquals(actionRecord.getUpdateDate(), lastUpdateDate);
        } catch (Throwable t) {
            throw t;
        } finally {
            this.contentManager.deleteContent(content);
            assertNull(this.contentManager.loadContent(contentId, false));
        }
    }

    protected void initContentAction(String namespace, String name, String contentOnSessionMarker) throws Exception {
        this.initAction(namespace, name);
        this.addParameter("contentOnSessionMarker", contentOnSessionMarker);
    }

    private void init() {
        this.actionLoggerManager = (IActionLogManager) this.getService(SystemConstants.ACTION_LOGGER_MANAGER);
        this.pageManager = (IPageManager) this.getService(SystemConstants.PAGE_MANAGER);
        this.langManager = (ILangManager) this.getService(SystemConstants.LANGUAGE_MANAGER);
        this.contentManager = (IContentManager) this.getService(JacmsSystemConstants.CONTENT_MANAGER);
        this.helper = new ActionLoggerTestHelper(this.getApplicationContext());
    }

    @Override
    protected void tearDown() throws Exception {
        this.helper.cleanRecords();
        super.tearDown();
    }

}
