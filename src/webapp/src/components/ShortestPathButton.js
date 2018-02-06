import React, { Component } from 'react';
import Api from "../api/Api";

export default class ShortestPathButton extends Component {
  render() {
    return (
      <button
        onClick={this._onClick}
      >Shortest Path</button>
    )
  }

  _onClick() {
    Api.send("asdf");
  }
}
