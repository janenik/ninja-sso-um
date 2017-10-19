/**<#-- This javascript source is a Freemarker template and is supposed to be included. -->*/
var sso = sso || {};
sso.statistics = sso.statistics || {};

sso.statistics.Storage = {};

sso.statistics.ControllerChart = function(serverUrl, requestIntervalMs, numberOfSamples) {
  this.serverUrl = serverUrl;
  this.requestInterval = requestIntervalMs || 1000; // In milliseconds
  this.numberOfSamples = numberOfSamples || 300; // Which is equivalent to 5 minutes.
  this.scheduleNextRequest();
};

sso.statistics.ControllerChart.prototype.updateChart = function(columns, axisYTitle) {
  if (this.chart) {
    this.chart.load({
      columns: columns
    });
    return;
  }
  this.chart = c3.generate({
    bindto: '#chart',
    data: {
      columns: columns
    },
    size: {
      height: 600,
    },
    axis: {
      y: {
        label: {
          text: axisYTitle,
          position: 'outer-middle'
        }
      }
    }
  });
  this.chart.hide();
  this.chart.show('ApplicationController.index');
}

sso.statistics.ControllerChart.prototype.sendDataRequest = function() {
  $.getJSON(this.serverUrl, this.onResponse.bind(this));
};

sso.statistics.ControllerChart.prototype.scheduleNextRequest = function() {
  setTimeout(this.sendDataRequest.bind(this), this.requestInterval);
};

sso.statistics.ControllerChart.prototype.onResponse = function(data) {
  var columns = [];
  var lastValue;
  $.each(data.data, function(key, value) {
     if (!sso.statistics.Storage[value.name]) {
       sso.statistics.Storage[value.name] = this.getNewEmptyControllerData(value.name);
     }
     var controllerData = sso.statistics.Storage[value.name];
     controllerData.push(value);
     if (controllerData.length > this.numberOfSamples) {
        controllerData.shift();
     }
     var columnData = [];
     columnData.push(this.getColumnName(value.name));
     for (var i = 0; i < controllerData.length; i++) {
        columnData.push(controllerData[i].oneMinuteRate || 0);
     }
     columns.push(columnData);
     lastValue = value;
  }.bind(this));
  this.updateChart(columns, (lastValue && lastValue.rateUnit) || '');
  this.scheduleNextRequest();
};

sso.statistics.ControllerChart.prototype.getNewEmptyControllerData = function(controllerName) {
  var column = [];
  column.push(this.getColumnName(controllerName));
  for (var i = 0; i < this.numberOfSamples; i++) {
    column.push({
      oneMinuteRate: 0
    });
  }
  return column;
};

sso.statistics.ControllerChart.prototype.getColumnName = function(controllerName) {
  var components = controllerName.split('.');
  var len = components.length;
  if (len <= 1) {return controllerName};
  return components[len - 2] + '.' + components[len - 1];
};

(function() {
    new sso.statistics.ControllerChart('${contextPath}/auth/admin/statistics/json', 1000, 300);
})();