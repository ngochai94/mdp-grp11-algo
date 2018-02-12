import React, { Component } from 'react';
import { Input, Button } from 'antd';
import socket from '../api/Api';

export default class RobotConfigForm extends Component {
  constructor(props) {
    super(props);
    this._onClickShortestPath = this._onClickShortestPath.bind(this);
    this._onClickExplore = this._onClickExplore.bind(this);
  }

  render() {
    return (
      <div className="setting-group">
        <Input
          className="setting"
          defaultValue="300"
          onBlur={(e) => socket.updateMoveTime(e.target.value)}
          addonBefore="Move time"
          addonAfter="ms"/>
        <Input
          className="setting"
          defaultValue="300"
          onBlur={(e) => socket.updateTurnTime(e.target.value)}
          addonBefore="Turn time"
          addonAfter="ms"/>
        <Button className="setting" onClick={this._onClickExplore} type="primary">Explore</Button>
        <Button className="setting" onClick={this._onClickShortestPath} type="primary">Shortest Path</Button>
      </div>
    )
  }

  _onClickExplore = () => {
    console.log('Explore start');
    this.props.onSendMap();
    socket.startExplore();
  };

  _onClickShortestPath = () => {
    console.log('Shortest path start');
    this.props.onSendMap();
    socket.startShortestPath()
  };
}

