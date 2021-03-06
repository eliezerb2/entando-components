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
package org.entando.entando.aps.system.services.pagemodel.model;

import java.util.Objects;
import org.apache.commons.lang.builder.ToStringBuilder;

public class DigitalExchangePageModelDto extends PageModelDto {

    private String digitalExchangeName;
    private boolean installed;

    public DigitalExchangePageModelDto() {
    }

    public DigitalExchangePageModelDto(PageModelDto other) {
        super(other);
    }

    public DigitalExchangePageModelDto(DigitalExchangePageModelDto other) {
        super(other);
        this.digitalExchangeName = other.digitalExchangeName;
    }

    public String getDigitalExchange() {
        return digitalExchangeName;
    }

    public void setDigitalExchangeName(String digitalExchangeName) {
        this.digitalExchangeName = digitalExchangeName;
    }

    public boolean isInstalled() {
        return installed;
    }

    public void setInstalled(boolean installed) {
        this.installed = installed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DigitalExchangePageModelDto that = (DigitalExchangePageModelDto) o;
        return super.equals(o)
                && Objects.equals(digitalExchangeName, that.digitalExchangeName)
                && Objects.equals(installed, that.installed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCode(), getDescr(), getConfiguration(), getMainFrame(), getPluginCode(), getTemplate(), digitalExchangeName, installed, getReferences());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("code", getCode())
                .append("descr", getDescr())
                .append("configuration", getConfiguration())
                .append("mainFrame", getMainFrame())
                .append("pluginCode", getPluginCode())
                .append("template", getTemplate())
                .append("digitalExchange", digitalExchangeName)
                .append("installed", installed)
                .append("references", getReferences())
                .toString();
    }
}
