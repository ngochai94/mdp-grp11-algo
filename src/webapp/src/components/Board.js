import React, { Component } from 'react';
import Cell from './Cell';
import Robot from './Robot';
import './Board.css';

export default class Board extends Component {
  constructor(props) {
    super(props);
    let tmpCells = {};
    for (let row = 1; row <= this.props.height; row++) {
      for (let col = 1; col <= this.props.width; col++) {
        tmpCells[[row, col]] = 1;
      }
    }
    this.state = {
      tmpCells,
      startRow: -1,
      startCol: -1,
    };

    this._renderCells = this._renderCells.bind(this);
    this._onBoardLeave = this._onBoardLeave.bind(this);
    this._onCellClick = this._onCellClick.bind(this);
    this._onCellEnter = this._onCellEnter.bind(this);
    this._onCellRelease = this._onCellRelease.bind(this);
  }

  render() {

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
        {this._renderCells()}
        {robot}
      </div>
    );
  }

  _renderCells = () => {
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
            status = {this.props.enforced ? this.props.cells[[row, col]] : this.state.tmpCells[[row, col]]}
          />
        )
      }
    }
    return cells;
  };

  _onCellClick = (e, row, col) => {
    e.preventDefault();
    if (!this.props.enforced) {
      this.setState((prevState, props) => {
        let cells = JSON.parse(JSON.stringify(props.cells));
        cells[[row, col]] = 3 - cells[[row, col]];
        return {
          tmpCells: cells,
          startRow: row,
          startCol: col,
        };
      });
    }
  };

  _onCellRelease(e) {
    e.preventDefault();
    if (!this.props.enforced) {
      if (this.state.startCol !== -1) {
        const cells = JSON.parse(JSON.stringify(this.state.tmpCells));
        this.props.onUpdate(cells);
      }
      this.setState({
        startRow: -1,
        startCol: -1,
      });
    }
  }

  _onCellEnter(e, row, col) {
    e.preventDefault();
    if (!this.props.enforced) {
      if (this.state.startCol !== -1) {
        let newCells = JSON.parse(JSON.stringify(this.props.cells));
        for (let r = Math.min(row, this.state.startRow); r <= Math.max(row, this.state.startRow); r++) {
          for (let c = Math.min(col, this.state.startCol); c <= Math.max(col, this.state.startCol); c++) {
            newCells[[r, c]] = 3 - newCells[[r, c]];
          }
        }
        this.setState({
          tmpCells: newCells,
        });
      }
    }
  }

  _onBoardLeave() {
    if (!this.props.enforced) {
      this.setState((prevState, props) => ({
        tmpCells: JSON.parse(JSON.stringify(props.cells)),
        startRow: -1,
        startCol: -1,
      }));
    }
  }
}

Board.defaultProps = {
  height: 20,
  width: 15,
  enforced: false,
};
