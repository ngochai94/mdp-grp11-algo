import React, { Component } from 'react';
import Cell from './Cell';
import Robot from './Robot';
import './Board.css';

export default class Board extends Component {
  constructor(props) {
    super(props);
    let cells = {};
    let tmpCells = {};
    for (let row = 1; row <= this.props.height; row++) {
      for (let col = 1; col <= this.props.width; col++) {
        cells[[row, col]] = 1;
        tmpCells[[row, col]] = 1;
      }
    }
    this.state = {
      cells,
      tmpCells,
      startRow: -1,
      startCol: -1,
    };

    this._onBoardLeave = this._onBoardLeave.bind(this);
    this._onCellClick = this._onCellClick.bind(this);
    this._onCellEnter = this._onCellEnter.bind(this);
    this._onCellRelease = this._onCellRelease.bind(this);
  }

  render() {
    let cells = [];
    const {height, width} = this.props;
    for (let row = height; row > 0; row--) {
      for (let col = 1; col <= width; col++) {
        cells.push(
          <Cell
            key = {`${row} ${col}`}
            onClick = {this._onCellClick}
            onRelease = {this._onCellRelease}
            onMouseEnter = {this._onCellEnter}
            row = {row}
            col = {col}
            status = {this.state.tmpCells[[row, col]]}
          />
        )
      }
    }

    const robot = this.props.robotX ? (
      <Robot
        x = {this.props.robotX}
        y = {this.props.robotY}
        rotate = {this.props.rotate}
    />) : null;

    return (
      <div
        className='Board'
        onMouseLeave = {this._onBoardLeave}
      >
        {cells}
        {robot}
      </div>
    );
  }

  _onCellClick(e, row, col) {
    e.preventDefault();
    if (this.props.selectable) {
      this.setState((prevState) => {
        let cells = JSON.parse(JSON.stringify(prevState.cells))
        cells[[row, col]] = -cells[[row, col]];
        return {
          tmpCells: cells,
          startRow: row,
          startCol: col,
        };
      });
    }
  }

  _onCellRelease(e) {
    e.preventDefault();
    if (this.props.selectable) {
      if (this.state.startCol !== -1) {
        this.setState((prevState) => ({
          cells: JSON.parse(JSON.stringify(prevState.tmpCells)),
        }));
      }
      this.setState({
        startRow: -1,
        startCol: -1,
      });
    }
  }

  _onCellEnter(e, row, col) {
    e.preventDefault();
    if (this.props.selectable) {
      if (this.state.startCol !== -1) {
        let newCells = JSON.parse(JSON.stringify(this.state.cells));
        for (let r = Math.min(row, this.state.startRow); r <= Math.max(row, this.state.startRow); r++) {
          for (let c = Math.min(col, this.state.startCol); c <= Math.max(col, this.state.startCol); c++) {
            newCells[[r, c]] = -newCells[[r, c]];
          }
        }
        this.setState({
          tmpCells: newCells,
        });
      }
    }
  }

  _onBoardLeave() {
    if (this.props.selectable) {
      this.setState((prevState) => ({
        tmpCells: JSON.parse(JSON.stringify(prevState.cells)),
        startRow: -1,
        startCol: -1,
      }));
    }
  }
}

Board.defaultProps = {
  height: 20,
  width: 15,
  selectable: false,
};
