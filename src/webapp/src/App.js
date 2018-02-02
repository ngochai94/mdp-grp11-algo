import React, { Component } from 'react';
import Board from './components/Board'
import './App.css';

class App extends Component {
  render() {
    return (
      <div className="App">
        <header className="App-header">
          <div className="App-title">Board Visualization</div>
        </header>
        <Board/>
      </div>
    );
  }
}

export default App;
