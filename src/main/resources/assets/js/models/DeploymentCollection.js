var Deployment = require("../models/Deployment");
var SortableCollection = require("../models/SortableCollection");

var DeploymentCollection = SortableCollection.extend({
  model: Deployment,

  initialize: function (models, options) {
    this.options = options;
    this.setComparator("-id");
    this.sort();
  },

  url: function () {
    return "/v2/deployments";
  }
});

module.exports = DeploymentCollection;
