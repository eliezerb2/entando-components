import React from "react";
import PropTypes from "prop-types";
import {Row, Col} from "patternfly-react";
import {uniqueId} from "lodash";
import SettingsChartContainer from "ui/widgets/charts/common/containers/SettingsChartContainer";
import SettingsChartDonutContainer from "ui/widgets/charts/donut-chart/containers/SettingsChartDonutContainer";
import PreviewChartSelected from "ui/widgets/charts/common/components/PreviewChartSelected";

const DONUT_CHART = "DONUT_CHART";

const options = {
  render: (type, formName) => {
    switch (type) {
      case DONUT_CHART:
        return <SettingsChartDonutContainer formName={formName} />;
      default:
        return <SettingsChartContainer formName={formName} />;
    }
  },
  widthChart: type => {
    switch (type) {
      case DONUT_CHART:
        return 400;
      default:
        return null;
    }
  },
  columnSize: type => {
    switch (type) {
      case DONUT_CHART:
        return 4;
      default:
        return 12;
    }
  }
};
const ChartSecondStepContent = ({
  formName,
  typeChart,
  data,
  labelChartPreview,
  rotated
}) => {
  const render = options.render(typeChart, formName);
  const widthChart = options.widthChart(typeChart);
  const columnSize = options.columnSize(typeChart);
  return (
    <div className="ChartSecondStep">
      <PreviewChartSelected
        idChart={`chart-second-step-${uniqueId()}`}
        type={typeChart}
        data={data}
        labelChartPreview={labelChartPreview}
        axisRotated={rotated}
        heightChart={250}
        widthChart={widthChart}
        columnSize={columnSize}
      />
      <Row>
        <Col xs={12} className="ChartSecondStep__settings-container">
          {render}
        </Col>
      </Row>
    </div>
  );
};

ChartSecondStepContent.propTypes = {
  typeChart: PropTypes.string.isRequired,
  labelChartPreview: PropTypes.string.isRequired,
  data: PropTypes.arrayOf(
    PropTypes.arrayOf(PropTypes.oneOfType([PropTypes.string, PropTypes.number]))
  ).isRequired,
  axis: PropTypes.shape({
    rotated: PropTypes.bool
  })
};

export default ChartSecondStepContent;
