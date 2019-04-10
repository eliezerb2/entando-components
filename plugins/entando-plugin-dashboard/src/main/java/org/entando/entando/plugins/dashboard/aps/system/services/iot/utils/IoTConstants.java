package org.entando.entando.plugins.dashboard.aps.system.services.iot.utils;

public class IoTConstants {
  
  public static final String NO_SERVER_INSTANCE = "No server Instance";
  
  public static final int CONNECTION_TIMEOUT = 500;

  public static final String JSON_FIELD_SEPARATOR = "->";
  
  public static final String MEASUREMENT_TYPE_EXCEPTION = "Invalid MeasurementPayload Type";

  public static final int MINIMUM_TIMEOUT_CONNECTION = 1000;


  //EXCEPTIONS AND LOGGER
  public static final String EX_CANT_COMMUNICATE_API = "%s error dashboard id %s, datasource id %s, can't communicate to Api Service";
  public static final String LOGGER_ERROR_CALLING_API = "{} obtained {} calling dashboard id {}, datasource code {}";
  public static final String UNABLE_TO_PARSE_DASHBOARD = "unable to parse dashboard";
  public static final String UNABLE_TO_PARSE_DATASOURCE = "unable to parse datasource";
}