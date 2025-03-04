# Steal and Share Game

A LAN-based two-player game implemented in Java with a retro-style UI. Players make strategic decisions to either STEAL or SHARE, affecting their balance based on their opponent's choice.

## Game Rules

- **Both choose STEAL:** Both players lose the bet amount
- **Both choose SHARE:** Both players keep their money (no change)
- **One STEAL, one SHARE:** The player who chose STEAL wins the bet amount from the opponent

## Features

- Retro 1990s style UI using Java Swing
- LAN-based multiplayer (2 players)
- 5-second timeout per move (defaults to SHARE)
- Real-time balance updates
- Configurable initial money and number of rounds

## Requirements

- Java 11 or higher
- Maven

## Building the Project

```bash
mvn clean package
```

## Running the Game

1. First, start the server:
```bash
java -cp target/steal-and-share-1.0-SNAPSHOT.jar com.stealnshare.server.GameServer
```

2. Then start two client instances (in separate terminals):
```bash
java -cp target/steal-and-share-1.0-SNAPSHOT.jar com.stealnshare.client.GameClient
```

3. For each client:
   - Enter the initial money amount when prompted
   - Enter the number of rounds when prompted
   - Use the STEAL or SHARE buttons to make your move each round
   - Watch your balance update based on the results

## Network Configuration

- The game runs on port 12345 by default
- Clients connect to localhost by default
- To play over LAN, modify the server address in the client code

## Project Structure

```
stealnshare/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── stealnshare/
│   │               ├── client/
│   │               │   └── GameClient.java
│   │               ├── server/
│   │               │   └── GameServer.java
│   │               └── common/
│   │                   └── GameConfig.java
│   └── test/
│       └── java/
└── pom.xml
```
