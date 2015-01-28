/** @jsx React.DOM */

var React = require("react/addons");
var NavTabsComponent = require("../components/NavTabsComponent");

module.exports = React.createClass({
    name: "TogglableTabsComponent",

    propTypes: {
      activeTabId: React.PropTypes.string.isRequired,
      className: React.PropTypes.string,
      onTabClick: React.PropTypes.func,
      tabs: React.PropTypes.array
    },

    render: function () {
      var childNodes = React.Children.map(this.props.children,
        function (child) {
          return React.addons.cloneWithProps(child, {
            isActive: (child.props.id === this.props.activeTabId)
          });
        }, this);

      /* jshint trailing:false, quotmark:false, newcap:false */
      /* jscs:disable disallowTrailingWhitespace, validateQuoteMarks, maximumLineLength */
      var nav;
      if (this.props.onTabClick != null && this.props.tabs != null) {
        nav = (
          <NavTabsComponent
            activeTabId={this.props.activeTabId}
            onTabClick={this.props.onTabClick}
            tabs={this.props.tabs} />
        );
      }

      return (
        <div className={this.props.className}>
          {nav}
          <div className="tab-content">
            {childNodes}
          </div>
        </div>
      );
    }
  });
