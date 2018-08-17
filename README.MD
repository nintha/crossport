# Crossport

A software solution to map some localhost ports to server ports. If the server has a public IP, you can access your local applications through this.
```
+-----------+      +-----------+       +-----------+    +----------+
|           |      |           |       |           |    |          |
|Web Browser|      |PUBLIC HOST|       | LOCAL HOST|    |Local Web |
|           |      |           |       |           |    |  Serser  |
|           +------>40000  5566<-------+           +---->   8080   |
|           |      |           |       |           |    |          |
|           |      | crossport |       | crossport |    |          |
|           |      |  server   |       |  client   |    |          |
+-----------+      +-----------+       +-----------+    +----------+
```

## Usage
Crossport has both a server program and a client program in a single jar package.

### Launch server

```sh
java -jar crossport-0.0.1.jar --serverMode --listenPort=5566
```

With `--serverMode`, crossport will run in server mode without GUI.

With `--listenPort=5566` (optional), crossport will listen to port `5566`; The default value is `5566`.

### Launch client

Crossport will run in client mode without the argument `--serverMode`:
```sh
java -jar crossport-0.0.1.jar --serverAddress=127.0.0.1:5566
```

`--serverAddress=127.0.0.1:5566` (optional) sets server address; Crossport will connect to `127.0.0.1:5566`.

### Build project

The project is built with gradle:
```
./gradlew build
```
The jar package will be generated in `/build/libs`.
