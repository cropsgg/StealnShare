# Steal and Share Game

A LAN-based two-player game implemented in Java with a retro-style UI. Players make strategic decisions to either STEAL or SHARE, affecting their balance based on their opponent's choice.

## Game Rules

- **Both choose STEAL:** Both players lose the bet amount
- **Both choose SHARE:** Both players keep their money (no change)
- **One STEAL, one SHARE:** The player who chose STEAL wins the bet amount from the opponent

## Features

- Retro 1990s style UI using Java Swing
- LAN-based multiplayer (2 players)
- 30-second timeout per move (defaults to SHARE)
- Real-time balance updates
- Configurable initial money and number of rounds
- Sound effects for different game outcomes
- Retro-styled interface with game log

## Requirements

- Java 11 or higher
- Maven 3.x
- Network connectivity for LAN play

## Project Structure

```
stealnshare/
├── src/                              # Source code directory
│   ├── main/
│   │   ├── java/                     # Java source files
│   │   │   └── com/stealnshare/
│   │   │       ├── client/           # Client-side code
│   │   │       │   └── GameClient.java   # Client UI and network logic
│   │   │       ├── server/           # Server-side code
│   │   │       │   └── GameServer.java   # Game server and logic
│   │   │       └── common/           # Shared code
│   │   │           └── GameConfig.java   # Common constants and configs
│   │   └── resources/                # Resource files
│   │       └── sounds/               # Game sound effects
│   │           ├── steal.wav         # Sound for steal action
│   │           ├── share.wav         # Sound for share action
│   │           ├── lose.wav          # Sound for losing
│   │           ├── both_steal.wav    # Sound for both stealing
│   │           └── generate_sounds.py # Script to generate sound files
│   └── test/                         # Test directory
│       └── java/                     # Test source files
├── run-server.sh                     # Script to start the server
├── run-client.sh                     # Script to start a client
└── pom.xml                          # Maven project configuration
```

## Component Details

### Server (GameServer.java)
- Manages game state and player connections
- Handles game logic and move validation
- Processes player moves with timeout mechanism
- Updates player balances based on game rules
- Broadcasts game state to connected clients

### Client (GameClient.java)
- Provides retro-styled GUI interface
- Manages network connection to server
- Handles user input and move submission
- Displays game state and balance updates
- Plays sound effects based on game events

### Common (GameConfig.java)
- Defines shared constants
- Network protocol messages
- Game configuration parameters
- Port and connection settings

## Building the Project

1. Clone the repository:
```bash
git clone https://github.com/cropsgg/StealnShare.git
cd StealnShare
```

2. Build with Maven:
```bash
mvn clean package
```

## Running the Game

### Method 1: Using Shell Scripts (Recommended)
1. Start the server:
```bash
./run-server.sh
```

2. Start two client instances (in separate terminals):
```bash
./run-client.sh
```

### Method 2: Using Java Commands
1. Start the server:
```bash
java -cp target/steal-and-share-1.0-SNAPSHOT.jar com.stealnshare.server.GameServer
```

2. Start two client instances:
```bash
java -cp target/steal-and-share-1.0-SNAPSHOT.jar com.stealnshare.client.GameClient
```

## Game Setup and Play

1. Server Setup:
   - Start the server first
   - Server will wait for two players to connect
   - Manages game state and player interactions

2. Client Setup (for each player):
   - Launch the client application
   - Enter initial money amount when prompted
   - Enter desired number of rounds
   - Wait for opponent to connect

3. Gameplay:
   - Each round lasts 30 seconds
   - Choose STEAL or SHARE using the buttons
   - If no choice is made, defaults to SHARE
   - View results and balance updates in real-time
   - Sound effects play based on game outcomes

## Network Configuration

- Default port: 8080
- Default host: localhost
- For LAN play:
  1. Server: Ensure port 8080 is accessible
  2. Clients: Modify server address in GameClient.java to server's IP
  3. Configure firewalls to allow connections

## Sound Effects

The game includes custom-generated sound effects:
- steal.wav: Plays when successfully stealing
- share.wav: Plays when both players share
- lose.wav: Plays when opponent steals
- both_steal.wav: Plays when both players steal

Sound files can be regenerated using the provided Python script:
```bash
cd src/main/resources/sounds
python generate_sounds.py
```

## Troubleshooting

1. Connection Issues:
   - Verify server is running
   - Check port availability
   - Confirm firewall settings
   - Verify network connectivity

2. Build Issues:
   - Ensure Java 11+ is installed
   - Verify Maven installation
   - Check for missing dependencies

3. Runtime Issues:
   - Verify sound files exist
   - Check file permissions
   - Monitor server logs

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit changes
4. Push to the branch
5. Create a Pull Request

## License

This project is open source and available under the MIT License.


