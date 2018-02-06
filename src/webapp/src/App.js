import React, { Component } from 'react';
import Board from './components/Board'
import './App.css';
import ShortestPathButton from "./components/ShortestPathButton";

class App extends Component {
  render() {
    return (
      <div className="App">
        <header className="App-header">
          <div className="App-title">Board Visualization</div>
        </header>
        <div>
          <div className="left-half">
            <Board selectable = {true}/>
          </div>
          <div className="right-half">
            <Board
              robotX = {2}
              robotY = {2}
              rotate = {0}
            />
            <div className="buttons">
              <button>Explore</button>
              <ShortestPathButton/>
            </div>
          </div>
        </div>
      </div>
    );
  }
}

export default App;
