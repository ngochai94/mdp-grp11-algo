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

function updateMoveTime(moveTime) {
  send("movetime\n" + moveTime);
}

function updateTurnTime(turnTime) {
  send("turntime\n" + turnTime);
}

function updateMap(map) {
  send("map\n" + map);
}

function startExplore() {
  send("explore");
}

function startShortestPath() {
  send("shortestpath");
}

export default {
  updateMoveTime,
  updateTurnTime,
  updateMap,
  startExplore,
  startShortestPath,

  register: (cb) => {
    socket.on('data', (data) => {
      const s = new TextDecoder("utf-8").decode(data);
      cb(s);
    });
  }
}