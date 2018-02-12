import React, { Component } from 'react';
import Board from './components/Board'
import RobotConfigForm from "./components/RobotConfigForm";
import { Layout, Col } from 'antd';
import socket from './api/Api';

import './App.css';
import 'antd/dist/antd.css';

const { Header } = Layout;

class App extends Component {
  constructor(props) {
    super(props);
    this.state = {
      robot: {x: 2, y: 2},
      rotate: 0,
      cells: this.getCellsFromTextMap(),
      drawCells: this.getCellsFromTextMap(),
    };

    this._onClickShortestPath = this._onClickShortestPath.bind(this);
    this._onClickExplore = this._onClickExplore.bind(this);
    this._onUpdateDrawBoard = this._onUpdateDrawBoard.bind(this);
    this._onSendMap = this._onSendMap.bind(this);

    socket.register((data) => {
      const { maze, robot } = JSON.parse(data);
      const { x: ox, y: oy } = robot.orientation;
      const cells = this.getCellsFromTextMap(maze.split('\n'));

      let rotate = 0;
      if (ox === 1 && oy === 0) rotate = 90;
      else if (ox === 0 && oy === -1) rotate = 180;
      else if (ox === -1 && oy === 0) rotate = 270;

      this.setState({
        robot: robot.center,
        rotate,
        cells,
      })
    });
  }

  getCellsFromTextMap = (map) => {
    let cells = {};
    for (let row = 1; row <= 20; row++) {
      for (let col = 1; col <= 15; col++) {
        cells[[row, col]] = map ? parseInt(map[20 - row][col - 1], 10) : 1;
      }
    }
    return cells;
  };

  render() {
    const boards = (
      <div>
        <Col span={2}/>
        <Col span={6}>
          <Board
            cells = {this.state.drawCells}
            onUpdate = {this._onUpdateDrawBoard}
          />
        </Col>
        <Col span={1}/>
        <Col span={6} className="setting-group">
          <RobotConfigForm
            onSendMap = {this._onSendMap}
          />
        </Col>
        <Col span={1}/>
        <Col span={6}>
          <Board
            robotX = {this.state.robot.x}
            robotY = {this.state.robot.y}
            rotate = {this.state.rotate}
            enforced = {true}
            cells = {this.state.cells}
          />
        </Col>
        <Col span={2}/>
      </div>
    );
    return (
      <Layout>
        <Header>
          <div className="header-text">Board Visualization</div>
        </Header>
        {boards}
      </Layout>
    );
  }

  _onClickShortestPath = () => {
    console.log('Shortest path start');
    this._onSendMap();
    socket.startShortestPath();
  };

  _onClickExplore = () => {
    console.log('Explore start');
    this._onSendMap();
    socket.startExplore();
  };

  _onSendMap = () => {
    let map = ""
    for (let row = 1; row <= 20; row++) {
      if (map) {
        map += "\n";
      }
      for (let col = 1; col <= 15; col++) {
        map += this.state.drawCells[[row, col]].toString();
      }
    }
    socket.updateMap(map);
  };

  _onUpdateDrawBoard = (cells) => {
    this.setState({
      drawCells: cells,
    })
  }
}

export default App;
