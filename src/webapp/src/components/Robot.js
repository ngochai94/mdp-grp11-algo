import React, { Component } from 'react';
import RobotImg from '../robot.gif';

export default class Robot extends Component {
  render() {
    const height = 5;
    const width = 5;
    const left = (this.props.x - 2) * 1.66666;
    const top = (21 - this.props.y) * 1.66666;
    const styles = {
      height: height + 'vw',
      width: width + 'vw',
      position: 'absolute',
      left: left + 'vw',
      top: top + 'vw',
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
