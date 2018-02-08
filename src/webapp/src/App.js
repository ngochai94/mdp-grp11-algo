import React, { Component } from 'react';
import Board from './components/Board'
import './App.css';
import Button from "./components/Button";
import socket from './api/Api';

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
    return (
      <div className="App">
        <header className="App-header">
          <div className="App-title">Board Visualization</div>
        </header>
        <div>
          <div className="left-half">
            <Board
              cells = {this.state.drawCells}
              onUpdate = {this._onUpdateDrawBoard}
            />
          </div>
          <div className="right-half">
            <Board
              robotX = {this.state.robot.x}
              robotY = {this.state.robot.y}
              rotate = {this.state.rotate}
              enforced = {true}
              cells = {this.state.cells}
            />
            <div className="buttons">
              <Button
                onClick = {this._onClickExplore}
                name = "Explore"
              />
              <Button
                onClick = {this._onClickShortestPath}
                name = "Shortest Path"
              />
            </div>
          </div>
        </div>
      </div>
    );
  }

  _onClickShortestPath = () => {
    console.log('Shortest path start');
    let msg = "map";
    for (let row = 1; row <= 20; row++) {
      msg += "\n";
      for (let col = 1; col <= 15; col++) {
        msg += this.state.drawCells[[row, col]].toString();
      }
    }
    socket.send(msg);
    socket.send("shortestpath");
  };

  _onClickExplore = () => {
    console.log('Explore start');
    let msg = "map";
    for (let row = 1; row <= 20; row++) {
      msg += "\n";
      for (let col = 1; col <= 15; col++) {
        msg += this.state.drawCells[[row, col]].toString();
      }
    }
    socket.send(msg);
    socket.send("explore");
  };

  _onUpdateDrawBoard = (cells) => {
    this.setState({
      drawCells: cells,
    })
  }
}

export default App;
