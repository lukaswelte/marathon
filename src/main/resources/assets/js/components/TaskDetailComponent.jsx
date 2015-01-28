/** @jsx React.DOM */

var React = require("react/addons");
var BackboneMixin = require("../mixins/BackboneMixin");
var States = require("../constants/States");
var Task = require("../models/Task");
var TimeFieldComponent = require("../components/TimeFieldComponent");
var TaskHealthComponent = require("../components/TaskHealthComponent");

var TaskDetailComponent = React.createClass({
  displayName: "TaskDetailComponent",

  mixins: [BackboneMixin],

  propTypes: {
    fetchState: React.PropTypes.number.isRequired,
    hasHealth: React.PropTypes.bool,
    onShowTaskList: React.PropTypes.func.isRequired,
    task: React.PropTypes.object.isRequired
  },

  getResource: function () {
    return this.props.task;
  },

  handleShowTaskList: function (event) {
    event.preventDefault();
    this.props.onShowTaskList();
  },

  render: function () {
    var task = this.props.task;
    var hasHealth = !!this.props.hasHealth;
    var hasError =
      this.props.fetchState === States.STATE_ERROR ||
      task.collection == null;
    var taskHealth = task.getHealth();
    var healthClassSet;
    var timeNodes;
    var timeFields;

    /* jshint trailing:false, quotmark:false, newcap:false */
    /* jscs:disable disallowTrailingWhitespace, validateQuoteMarks, maximumLineLength */
    if (!hasError) {
      healthClassSet = React.addons.classSet({
        "text-unhealthy": taskHealth === Task.HEALTH.UNHEALTHY,
        "text-muted": taskHealth === Task.HEALTH.UNKNOWN
      });

      timeNodes = [
        {
          label: "Staged at",
          time: task.get("stagedAt")
        }, {
          label: "Started at",
          time: task.get("startedAt")
        }
      ];
      timeFields = timeNodes.map(function (timeNode, index) {
        return (
          <TimeFieldComponent
            key={index}
            label={timeNode.label}
            time={timeNode.time} />
        );
      });
    }

    return (
      <div>
        <ol className="breadcrumb">
          <li>
            <a href="#" onClick={this.handleShowTaskList}>Task List</a>
          </li>
          <li className="active">{task.get("id")}</li>
        </ol>
        <h5>Task Details</h5>
        {hasError ?
          <p className="text-center text-danger">
            Error fetching task details.
            Go to <a href="#" onClick={this.handleShowTaskList}>Task List</a> to see the full list.
          </p> :
          null}
        {<div>
          <dl className="dl-horizontal">
            <dt>Host</dt>
            <dd>{task.get("host")}</dd>
            <dt>Ports</dt>
            <dd>[{task.get("ports").toString()}]</dd>
            <dt>Status</dt>
            <dd>{task.get("status")}</dd>
            {timeFields}
            <dt>Version</dt>
            <dd>
              <time dateTime={task.get("version")}>
                {task.get("version").toLocaleString()}
              </time>
            </dd>
            <dt>Health</dt>
            <dd className={healthClassSet}>{this.props.taskHealthMessage}</dd>
          </dl>
          {hasHealth ?
            <TaskHealthComponent task={task} /> :
            null}
        </div>}
      </div>
    );
  }
});

module.exports = TaskDetailComponent;
