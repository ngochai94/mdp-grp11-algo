import React, { Component } from 'react';

export default class ShortestPathButton extends Component {
  render() {
    return (
      <button
        onClick={this.props.onClick}
      >Shortest Path</button>
    )
  }
}
