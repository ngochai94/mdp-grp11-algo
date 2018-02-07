import React, { Component } from 'react';
import RobotImg from '../robot.gif';

export default class Robot extends Component {
  render() {
    const height = 65;
    const width = 65;
    const left = 100 + (this.props.x - 0.5) * 24 - height / 2;
    const top = 50 + (20.5 - this.props.y) * 24 - width / 2;
    const styles = {
      height: height + 'px',
      width: width + 'px',
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
