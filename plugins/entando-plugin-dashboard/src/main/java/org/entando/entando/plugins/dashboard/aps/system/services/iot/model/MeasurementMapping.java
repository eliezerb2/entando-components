package org.entando.entando.plugins.dashboard.aps.system.services.iot.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Objects;

public class MeasurementMapping {

  public MeasurementMapping() {
  }

  public MeasurementMapping(String sourceName, String sourceType, String destinationName,
      String transformerClass) {
    this.sourceName = sourceName;
    this.sourceType = sourceType;
    this.destinationName = destinationName;
    this.transformerClass = transformerClass;
  }

  public MeasurementMapping(String sourceName, String destinationName) {
    this.sourceName = sourceName;
    this.destinationName = destinationName;
  }

  String sourceName;
  @JsonIgnore
  String sourceType;
  String destinationName;
  @JsonIgnore
  String transformerClass;

  public String getSourceName() {
    return sourceName;
  }

  public void setSourceName(String sourceName) {
    this.sourceName = sourceName;
  }

  public String getSourceType() {
    return sourceType;
  }

  public void setSourceType(String sourceType) {
    this.sourceType = sourceType;
  }

  public String getDestinationName() {
    return destinationName;
  }

  public void setDestinationName(String destinationName) {
    this.destinationName = destinationName;
  }

  public String getTransformerClass() {
    return transformerClass;
  }

  public void setTransformerClass(String transformerClass) {
    this.transformerClass = transformerClass;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MeasurementMapping that = (MeasurementMapping) o;
    return Objects.equals(sourceName, that.sourceName) &&
        Objects.equals(sourceType, that.sourceType) &&
        Objects.equals(destinationName, that.destinationName) &&
        Objects.equals(transformerClass, that.transformerClass);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sourceName, sourceType, destinationName, transformerClass);
  }
}