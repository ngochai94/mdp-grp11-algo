import Socket from 'simple-websocket';

const WS_URL = 'ws://localhost:8080';
const socket = Socket(WS_URL);
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

function send(msg) {
  if (connected) {
    socket.send(msg);
  }
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