import socket from '../api/Api';
import { notification as noti } from 'antd';

noti.config({
  placement: 'topLeft',
});

socket.register((data) => {
  const parsedData = JSON.parse(data);
  if (parsedData['mType'] === 'notification') {
    const { notification } = parsedData;
    noti['info']({
      message: 'Notification',
      description: notification,
      duration: 10,
    });
  }
});