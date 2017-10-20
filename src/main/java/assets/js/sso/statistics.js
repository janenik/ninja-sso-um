/**<#-- This javascript source is a Freemarker template and is supposed to be included. -->*/
var sso = sso || {};
sso.statistics = sso.statistics || {};

sso.statistics.Storage = {};

sso.statistics.ControllerChart = function(serverUrl) {
  this.serverUrl = serverUrl;
  this.requestInterval = 1000; // In milliseconds
  this.numberOfSamples = 300; // Which is equivalent to 5 minutes.
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
    transition: {
       duration: 0
    },
    size: {
      height: 600
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
  $.getJSON(this.serverUrl, this.onResponse.bind(this))
    .fail(this.scheduleNextRequest.bind(this));
};

sso.statistics.ControllerChart.prototype.scheduleNextRequest = function() {
  setTimeout(this.sendDataRequest.bind(this), this.requestInterval);
};

sso.statistics.ControllerChart.prototype.onResponse = function(data) {
  var columns = [], previousCount, currentCount, lastValue, columnData, controllerData, controllerDataValue, rate, i;
  var lastValue;
  $.each(data.data, function(key, value) {
     if (!sso.statistics.Storage[value.name]) {
       sso.statistics.Storage[value.name] = [];//this.getNewEmptyControllerData(value.meanRate);
     }
     controllerData = sso.statistics.Storage[value.name];
     controllerData.push(value);
     if (controllerData.length >= this.numberOfSamples) {
        controllerData.shift();
     }
     previousCount = value.count;
     columnData = [];
     columnData.push(this.getColumnName(value.name));
     for (i = 0; i < controllerData.length; i++) {
       controllerDataValue = controllerData[i];
       if (controllerDataValue.prefillRate) {
         columnData.push(controllerDataValue.prefillRate);
         previousCount = controllerDataValue.prefillRate;
       } else {
         currentCount = controllerData[i].count;
         rate = currentCount - previousCount;
         rate = rate >= 0 ? rate : 0;
         columnData.push(rate);
         previousCount = currentCount;
       }
     }
     columns.push(columnData);
     lastValue = value;
  }.bind(this));
  this.updateChart(columns, (lastValue && lastValue.rateUnit) || '');
  this.scheduleNextRequest();
};

sso.statistics.ControllerChart.prototype.getNewEmptyControllerData = function(defaultValue) {
  var column = [];
  for (var i = 0; i < this.numberOfSamples; i++) {
    column.push({
      prefillRate: defaultValue
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
    new sso.statistics.ControllerChart('${contextPath}/auth/admin/statistics/json');
})();