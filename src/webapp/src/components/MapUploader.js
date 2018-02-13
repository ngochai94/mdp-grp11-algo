import React, { Component } from 'react';
import { Upload, Icon, Button } from 'antd';

export default class MapUploader extends Component {
  constructor(props) {
    super(props);
    this.handleFile = this.handleFile.bind(this);
  }

  render() {
    return (
      <div>
        <Upload
          beforeUpload={this.handleFile}
        >
          <Button type="dashed">
            <Icon type="upload"/>Upload map
          </Button>
        </Upload>
      </div>
    );
  }

  handleFile(file) {
    const reader = new FileReader();
    const self = this;
    reader.onload = function() {
      const map = reader.result;
      self.props.onLoad(map)
    };
    reader.readAsText(file);
    return false;
  }
}
