ALTER TABLE dashboard_config_datasource ADD COLUMN datasourcecode character varying(255);

ALTER TABLE dashboard_config_datasource ADD COLUMN metadata TEXT;

ALTER TABLE dashboard_config_datasource ADD COLUMN name character varying(255);
 
ALTER TABLE dashboard_config ADD COLUMN "type" character varying(100);

ALTER TABLE dashboard_config_datasource ALTER COLUMN datasourceuri DROP NOT NULL;
  
    