import React, { Component } from 'react';
import './Cell.css'

export default class Cell extends Component {
  constructor(props) {
    super(props);
    this.state = {
      status: this._statusFromProps()
    }
  }

  render() {
    return (
      <div
        onMouseDown={(e) => this.props.onClick(e, this.props.row, this.props.col)}
        onMouseUp={(e) => this.props.onRelease(e)}
        onMouseEnter={(e) => this.props.onMouseEnter(e, this.props.row, this.props.col)}
        className={`cell ${this._statusFromProps()}`}
      />
    );
  }

  _statusFromProps() {
    let status = 'empty';
    if (this.props.status === 0) {
      status = 'unknown';
    } else if (this.props.status === 2) {
      status = 'blocked';
    }
    return status;
  }
}

Cell.defaultProps = {
  status: 'empty',
};