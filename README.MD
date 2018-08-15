# Crossport

A software to map some localhost ports to server ports. If the server has a public IP, you can access your local applications by this.

## Usage
Crossport has both server program and client program in one jar package.

### Launch server

```sh
java -jar crossport-0.0.1.jar --serverMode [--listenPort=5566]
```

`--serverMode` crossport will run in server mode without GUI.

`--listenPort=5566` (optional) crossport will listen to port `5566`, default value is `5566`

### Launch client

crossport will run in client mode without argument `--serverMode`.
```sh
java -jar crossport-0.0.1.jar --serverAddress=127.0.0.1:5566
```

`--serverAddress=127.0.0.1:5566` (optional) to set server address, then crossport will connect to `127.0.0.1:5566`

### Build project

the project build by gradle
```
./gradlew build
```
the jar will be generated in `/build/libs`
