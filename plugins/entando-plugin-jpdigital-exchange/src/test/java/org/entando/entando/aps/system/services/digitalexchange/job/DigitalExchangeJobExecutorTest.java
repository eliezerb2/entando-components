/*
 * Copyright 2019-Present Entando Inc. (http://www.entando.com) All rights reserved.
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
package org.entando.entando.aps.system.services.digitalexchange.job;

import com.agiletec.aps.system.exception.ApsSystemException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.KeyPair;
import java.util.Collections;
import java.util.function.Consumer;
import org.entando.entando.aps.system.init.DatabaseManager;
import org.entando.entando.aps.system.init.InitializerManager;
import org.entando.entando.aps.system.jpa.servdb.DigitalExchangeJob;
import org.entando.entando.aps.system.services.digitalexchange.DigitalExchangesService;
import org.entando.entando.aps.system.services.digitalexchange.client.DigitalExchangesClient;
import org.entando.entando.aps.system.services.digitalexchange.model.DigitalExchange;
import org.entando.entando.aps.system.services.digitalexchange.signature.SignatureUtil;
import org.entando.entando.web.common.model.PagedMetadata;
import org.entando.entando.web.common.model.PagedRestResponse;
import org.entando.entando.web.digitalexchange.component.DigitalExchangeComponent;
import org.entando.entando.web.label.LabelController;
import org.entando.entando.web.pagemodel.PageModelController;
import org.springframework.context.ApplicationContext;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import org.entando.entando.aps.system.init.model.ComponentInstallationReport;
import org.entando.entando.aps.system.init.model.SystemInstallationReport;
import static org.entando.entando.aps.system.services.digitalexchange.DigitalExchangeTestUtils.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class DigitalExchangeJobExecutorTest {

    private static File tempZipFile;

    @Mock
    private DigitalExchangesClient client;

    @Mock
    private DigitalExchangesService digitalExchangesService;

    @Mock
    private ComponentStorageManager storageManager;

    @Mock
    private PageModelController pageModelController;
    
    @Spy
    @InjectMocks
    private ModelParser modelParser;

    @Mock
    private DatabaseManager databaseManager;

    @Mock
    private InitializerManager initializerManager;

    @Mock
    private Consumer<DigitalExchangeJob> jobConsumer;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private LabelController labelController;

    @Spy
    private CommandExecutor commandExecutor;

    @Mock
    private SystemInstallationReport installationReport;

    @Mock
    private ComponentInstallationReport componentInstallationReport;

    @InjectMocks
    @Spy
    private DigitalExchangeInstallExecutor installer;

    private DigitalExchangeJob job;

    @BeforeClass
    public static void setupZip() {
        tempZipFile = ComponentZipUtil.getTestPageModelZip();
    }

    @AfterClass
    public static void deleteZip() {
        if (tempZipFile != null) {
            tempZipFile.delete();
        }
    }

    @Before
    public void setUp() throws Exception {


        commandExecutor.setApplicationContext(applicationContext);

        job = new DigitalExchangeJob();
        job.setDigitalExchangeId("DE");
        job.setComponentId("de_test_page_model");

        when(client.getStreamResponse(any(), any())).thenReturn(new FileInputStream(tempZipFile));

        when(client.getSingleResponse(any(String.class), any())).thenReturn(getComponentInfoResponse());

        when(digitalExchangesService.findById(anyString())).thenReturn(getDE1());

        when(storageManager.getProtectedStream(endsWith("component.xml")))
                .thenReturn(getClass().getClassLoader().getResourceAsStream("components/de_test_page_model/component.xml"));

        when(storageManager.getProtectedStream(endsWith("port_data_test.sql")))
                .thenReturn(getClass().getClassLoader().getResourceAsStream("components/de_test_page_model/data/port_data_test.sql"));

        when(storageManager.getProtectedStream(endsWith("de_test_page_model.json")))
                .thenReturn(getClass().getClassLoader().getResourceAsStream("components/de_test_page_model/data/de_test_page_model.json"));

        when(storageManager.getProtectedStream(endsWith("de_test_label.json")))
                .thenReturn(getClass().getClassLoader().getResourceAsStream("components/de_test_page_model/data/de_test_label.json"));

        when(storageManager.existsProtected(any())).thenReturn(true);

        when(applicationContext.getBean("labelController")).thenReturn(labelController);
        when(applicationContext.getBean("componentStorageManager")).thenReturn(storageManager);
        when(applicationContext.getBean("modelParser")).thenReturn(modelParser);
        when(applicationContext.getBean("pageModelController")).thenReturn(pageModelController);

        doNothing().when(installer).reloadSystem();

        when(initializerManager.getCurrentReport()).thenReturn(installationReport);
        when(installationReport.getComponentReport(any(), eq(false))).thenReturn(componentInstallationReport);
        when(componentInstallationReport.getStatus()).thenReturn(SystemInstallationReport.Status.OK);
    }

    private PagedRestResponse<DigitalExchangeComponent> getComponentInfoResponse() {
        PagedMetadata<DigitalExchangeComponent> pagedMetadata = new PagedMetadata<>();
        try(InputStream in = Files.newInputStream(tempZipFile.toPath(), StandardOpenOption.READ)) {
            DigitalExchangeComponent component = new DigitalExchangeComponent();
            component.setSignature(SignatureUtil.signPackage(in, SignatureUtil.privateKeyFromPEM(getTestPrivateKey())));
            pagedMetadata.setBody(Collections.singletonList(component));
            return new PagedRestResponse<>(pagedMetadata);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }


    @Test
    public void shouldInstallComponent() throws ApsSystemException, IOException {

        installer.execute(job, jobConsumer);

        ArgumentCaptor<DigitalExchangeJob> jobCaptor = ArgumentCaptor.forClass(DigitalExchangeJob.class);
        verify(jobConsumer, times(6)).accept(jobCaptor.capture());
        assertThat(jobCaptor.getValue().getProgress()).isEqualTo(1);
        assertThat(jobCaptor.getValue().getStatus()).isEqualTo(JobStatus.COMPLETED);

        ArgumentCaptor<String> protectedFileCaptor = ArgumentCaptor.forClass(String.class);
        verify(storageManager, times(4)).saveProtectedFile(protectedFileCaptor.capture(), any());
        assertThat(protectedFileCaptor.getAllValues()).allMatch(v -> v.startsWith(job.getComponentId()));

        ArgumentCaptor<String> resourceFileCaptor = ArgumentCaptor.forClass(String.class);
        verify(storageManager, times(1)).saveResourceFile(resourceFileCaptor.capture(), any());
        assertThat(resourceFileCaptor.getAllValues()).allMatch(v -> v.startsWith(job.getComponentId()));

        verify(databaseManager, times(1)).initComponentDatabases(any(), any(), anyBoolean());
        verify(databaseManager, times(1)).initComponentDefaultResources(any(), any(), anyBoolean());
        verify(initializerManager, times(2)).saveReport(any());

        verify(labelController, times(1)).addLabelGroup(any());
    }

    @Test(expected = JobExecutionException.class)
    public void shouldFailOnDownload() {

        when(client.getStreamResponse(any(), any())).thenThrow(UncheckedIOException.class);

        installer.execute(job, jobConsumer);
    }

    @Test(expected = JobExecutionException.class)
    public void shouldFailOnUnzip() throws IOException, ApsSystemException {

        doThrow(ApsSystemException.class).when(storageManager)
                .saveProtectedFile(any(), any());

        installer.execute(job, jobConsumer);
    }

    @Test(expected = JobExecutionException.class)
    public void shouldFailOnMissingComponentDefinition() throws ApsSystemException {

        when(storageManager.existsProtected(endsWith("component.xml"))).thenReturn(false);

        installer.execute(job, jobConsumer);
    }

    @Test(expected = JobExecutionException.class)
    public void shouldFailOnParsingXML() throws ApsSystemException {

        when(storageManager.getProtectedStream(endsWith("component.xml"))).thenReturn(null);

        installer.execute(job, jobConsumer);
    }

    @Test(expected = JobExecutionException.class)
    public void shouldFailOnInstallation() throws ApsSystemException {

        doThrow(ApsSystemException.class)
                .when(databaseManager).initComponentDatabases(any(), any(), anyBoolean());

        installer.execute(job, jobConsumer);
    }

    @Test(expected = JobExecutionException.class)
    public void shouldFailOnNotVerifiedSignature() throws ApsSystemException {

        DigitalExchange testInstance = getDE1();
        testInstance.setPublicKey(SignatureUtil.publicKeyToPEM(SignatureUtil.createKeyPair().getPublic()));

        when(digitalExchangesService.findById(anyString())).thenReturn(testInstance);

        installer.execute(job, jobConsumer);


    }
}
