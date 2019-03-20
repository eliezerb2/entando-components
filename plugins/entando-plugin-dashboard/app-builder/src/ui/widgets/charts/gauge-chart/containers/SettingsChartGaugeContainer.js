import {connect} from "react-redux";
import {formValueSelector} from "redux-form";

import SettingsChartGauge from "../components/SettingsChartGauge";

import {getDatasourceColumns} from "state/main/selectors";

const mapStateToProps = (state, ownProps) => {
  const selector = formValueSelector(ownProps.formName);
  return {
    datasourceSelected: selector(state, "datasource"),
    optionColumns: getDatasourceColumns(state),
    optionColumnXSelected: selector(state, "columns.x") || [],
    optionColumnYSelected: selector(state, "columns.y") || []
  };
};

const SettingsChartGaugeContainer = connect(
  mapStateToProps,
  null
)(SettingsChartGauge);

export default SettingsChartGaugeContainer;