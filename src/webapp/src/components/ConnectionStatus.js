import socket from '../api/Api';
import { Badge } from 'antd';
import React, { Component } from 'react';

import './ConnectionStatus.css';

export default class ConnectionStatus extends Component {
  constructor(props) {
    super(props);
    this.state = {
      connected: false
    };
    this.check = this.check.bind(this);
  }
  check() {
    this.setState({
      connected: socket.isConnected()
    });
  }
  componentDidMount() {
    this.interval = setInterval(this.check, 1000);
  }

  componentWillUnmount() {
    clearInterval(this.interval);
  }

  render() {
    const badge = (this.state.connected) ? <Badge status="success" text="Connected"/> :
      <Badge status="error" text="Disconnected"/>;
    return (
      <span className='connection-status'>{badge}</span>
    );
  }
}