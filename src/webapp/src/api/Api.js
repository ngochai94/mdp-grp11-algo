import Socket from 'simple-websocket';

const socket = Socket("ws://localhost:8080/");
let connected = false;

socket.on('connect', () => {
  console.log('connected');
  connected = true;
  sendHeartBeat();
});

socket.on('close', () => {
  console.log('closed');
  connected = false;
});

function wait(cb) {
  if (!connected) {
    console.log('connecting...');
    setTimeout(wait, 1000);
  } else {
    cb();
  }
}

function send(msg) {
  wait(() => socket.send(msg));
}

function sendHeartBeat() {
  send("");
  setTimeout(sendHeartBeat, 10000);
}

export default {
  send,

  register: (cb) => {
    socket.on('data', (data) => {
      const s = new TextDecoder("utf-8").decode(data);
      cb(s);
    });
  }
}