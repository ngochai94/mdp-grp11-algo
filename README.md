# MDP Algorithm Group 11

## Usage
**Run**
`./run.sh`

**Server Development**
`ALGO=<0|1|2> FAKE_ANDROID=<0|1> FAKE_RPI=<0|1> sbt run`

**UI Development**
`cd src/webapp && npm start`

**Packaging**
`sbt assembly`

## Details
* Server uses Scala to implement 3 different algorithms for exploration
  * Wall hugging: always turn robot with this priorities: Left > Forward > Right
  * Nearest helpful cell: always move to the nearest cell that is expected to be helpful
  * Hybrid: combination of the above algorithms
* UI is written in React/JS and make use of Ant design
  * Allow manual map drawing or map uploading
  * Exploration with time/coverage limit
  * Shortest path with way point

## Screenshot
![UI Screenshot](screenshot.png)
