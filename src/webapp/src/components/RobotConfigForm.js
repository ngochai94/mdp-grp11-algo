import React, { Component } from 'react';
import { Input, Button, Col, Radio } from 'antd';
import socket from '../api/Api';

import './RobotConfigForm.css';

export default class RobotConfigForm extends Component {
  constructor(props) {
    super(props);
    this.state = {
      explorer: 'wall'
    };
    this._onClickShortestPath = this._onClickShortestPath.bind(this);
    this._onClickExplore = this._onClickExplore.bind(this);
    this._onSelectExplorer = this._onSelectExplorer.bind(this);
  }

  render() {
    return (
      <div className="setting-group">
        <div className="explore">
          <Col span={10}>
            <Input
              className="setting"
              defaultValue="100"
              onBlur={(e) => socket.updateMoveTime(e.target.value)}
              addonBefore="Move time"
              // addonAfter="ms"
            />
          </Col>
          <Col span={4}/>
          <Col span={10}>
            <Input
              className="setting"
              defaultValue="120"
              onBlur={(e) => socket.updateTurnTime(e.target.value)}
              addonBefore="Turn time"
              // addonAfter="ms"
            />
          </Col>
          <Input
            className="setting"
            defaultValue="100"
            onBlur={(e) => socket.updateCoverageLimit(e.target.value)}
            addonBefore="Coverage limit"
            addonAfter="%"/>
          <Input
            className="setting"
            defaultValue="360"
            onBlur={(e) => socket.updateTimeLimit(e.target.value)}
            addonBefore="Time limit"
            addonAfter="s"/>
          <Radio.Group value={this.state.explorer} onChange={this._onSelectExplorer} className="setting">
            <Radio.Button value="wall">Wall Hugging</Radio.Button>
            <Radio.Button value="near">Nearest Cell</Radio.Button>
          </Radio.Group>
          <Button className="setting" onClick={this._onClickExplore} type="primary">Explore</Button>
        </div>
        <div className="shortestpath">
          <Col span={10}>
            <Input
              className="setting"
              defaultValue=""
              onBlur={(e) => socket.updateWayPointX(e.target.value)}
              addonBefore="Waypoint X"/>
          </Col>
          <Col span={4}/>
          <Col span={10}>
            <Input
              className="setting"
              defaultValue=""
              onBlur={(e) => socket.updateWayPointY(e.target.value)}
              addonBefore="Waypoint Y"/>
          </Col>
          <Button className="setting" onClick={this._onClickShortestPath} type="primary">Shortest Path</Button>
        </div>
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

  _onSelectExplorer = (e) => {
    e.preventDefault();
    this.setState({
      explorer: e.target.value
    });
    socket.updateExplorer(e.target.value);
  }
}

