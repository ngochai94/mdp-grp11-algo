import Socket from 'simple-websocket';

const socket = Socket("ws://localhost:8080/");
let connected = false;

socket.on('connect', () => {
  console.log('connected');
  connected = true;
});

socket.on('data', (data) => {
  const s = new TextDecoder("utf-8").decode(data);
  console.log(s);
});

socket.on('close', () => {
  console.log('closed');
  connected = false;
})

function wait(cb) {
  if (!connected) {
    console.log('connecting...');
    setTimeout(wait, 1000);
  } else {
    cb();
  }
}

export default {
  send: (msg) => {
    wait(() => socket.send(msg));
  }
}