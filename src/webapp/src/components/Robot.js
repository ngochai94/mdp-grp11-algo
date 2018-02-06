import React, { Component } from 'react';
import RobotImg from '../robot.gif';

export default class Robot extends Component {
  render() {
    const left = 100 + (this.props.x - 2) * 24;
    const top = 50 + (19 - this.props.y) * 24;
    const styles = {
      height: '72px',
      width: '72px',
      position: 'absolute',
      left,
      top,
      transform: 'rotate(' + this.props.rotate + 'deg)',
    };
    return (
      <img
        style={styles}
        src={RobotImg}
        alt="Robot"
      />
    );
  }
}

Robot.defaultProps = {
  x: 1,
  y: 1,
  rotate: 0,
};
