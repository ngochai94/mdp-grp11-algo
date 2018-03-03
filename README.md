# MDP Algorithm Group 11

## Usage
**Run**
`./run.sh`

**Server Development**
`sbt run`

**UI Development**
`cd src/webapp && npm start`

## Details
* Server uses Scala to implement 2 different algorithms for exploration
  * Wall hugging: always turn robot with this priorities: Left > Forward > Right
  * Nearest helpful cell: always move to the nearest cell that is expected to be helpful
* UI is written in React/JS and make use of Ant design
  * Allow manual map drawing or map uploading
  * Exploration with time/coverage limit
  * Shortest path with way point

## Screenshot
![UI Screenshot](screenshot.png)
