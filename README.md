# Steal and Share Game

A LAN-based two-player game implemented in Java with a retro-style UI. Players make strategic decisions to either STEAL or SHARE, affecting their balance based on their opponent's choice.

## Game Rules

- **Both choose SHARE:** Both players gain 3 coins
- **Both choose STEAL:** Both players gain 1 coin
- **One STEAL, one SHARE:** The player who chose STEAL gains 5 coins, player who SHARED gains 0 coins
- **Player disconnection:** If a player disconnects or times out during a round, their move defaults to SHARE for that round and the game ends after that round
- **Round timeout:** If a player doesn't submit a move within 30 seconds, they default to SHARE

## Features

- Retro 1990s style UI using Java Swing
- LAN-based multiplayer (2 players)
- 30-second timeout per move (defaults to SHARE)
- Real-time coin updates
- Configurable initial money and number of rounds
- Sound effects for different game outcomes
- Retro-styled interface with game log
- Automatic play using predefined algorithms

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
- Updates player coin counts based on game rules
- Broadcasts game state to connected clients
- Detects player disconnections and ends the game appropriately
- Defaults to SHARE for players who disconnect or time out

### Client (GameClient.java)
- Provides retro-styled GUI interface
- Manages network connection to server
- Handles user input and move submission
- Displays game state and coin updates
- Plays sound effects based on game events
- Supports automatic play with three algorithm choices:
  - Tit for Tat: Starts with SHARE, then copies opponent's last move
  - Random: Randomly chooses between STEAL and SHARE
  - Systematic: Alternates between SHARE and STEAL (starting with SHARE)

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

### Method 3: Using Maven Commands
1. Start the server:
```bash
mvn exec:java -Dexec.mainClass="com.stealnshare.server.GameServer"
```

2. Start two client instances (in separate terminals):
```bash
mvn exec:java -Dexec.mainClass="com.stealnshare.client.GameClient"
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
   - Choose STEAL or SHARE using the buttons, or let an algorithm play for you
   - Algorithms take 3 seconds to "think" before making a move
   - If no choice is made, defaults to SHARE
   - If a player disconnects during a round, their move defaults to SHARE and the game ends after that round
   - View results and coin updates in real-time
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

## Disconnection Handling

The game has been designed to gracefully handle player disconnections:

1. Detection:
   - The server detects disconnections via socket read timeouts
   - If a player doesn't respond within the time limit, they're considered disconnected

2. Response:
   - For the current round: The disconnected player's move defaults to SHARE
   - The round is completed and coins are awarded accordingly
   - Game state is sent to the remaining connected player
   - The game ends after the current round instead of continuing

3. Reconnection:
   - Players must restart the client application to reconnect
   - A new game session will begin when two players connect

## Algorithm Play

The game supports automatic play using predefined algorithms:

1. **Algorithm Types:**
   - **Tit for Tat:** Always starts with SHARE, then copies what the opponent did in the previous round
   - **Random:** Randomly chooses between STEAL and SHARE with equal probability
   - **Systematic:** Alternates between SHARE and STEAL (always starts with SHARE)

2. **Usage:**
   - Click the "PLAY WITH ALGORITHM" button in the first round before making any manual move
   - Select your desired algorithm from the dialog
   - Once an algorithm is selected, manual play is disabled for the entire game
   - The algorithm will wait 3 seconds before making each move

3. **Limitations:**
   - Algorithms can only be selected in the first round before making any manual move
   - Algorithm selection cannot be changed mid-game
   - Each new game requires a new algorithm selection


## Computer Networking Details

### Network Architecture

The Steal and Share game implements a classic client-server architecture using Java's socket programming capabilities:

1. **Transport Layer**
   - **Protocol**: TCP (Transmission Control Protocol)
   - **Port**: 8080 (configurable in GameConfig.java)
   - **Benefits**: Reliable, ordered, and error-checked delivery of game messages
   - **Connection Management**: Full-duplex communication with persistent connections

2. **Application Layer Protocol**
   - **Format**: Text-based with colon-separated fields
   - **Encoding**: UTF-8 text
   - **Message Types**:
     ```
     GAME_START                      # Server -> Client: Game initialization
     ROUND:<number>                  # Server -> Client: Round announcement
     STEAL or SHARE                  # Client -> Server: Player move
     RESULT:<move1>:<move2>:<coins1>:<coins2>[:<TIMEOUT>]  # Server -> Client: Round results
     GAME_OVER                       # Server -> Client: Game termination
     ```

3. **Protocol Flow**:
   ```
   1. Server starts and binds to port 8080
   2. Two clients connect to server
   3. Server sends GAME_START to both clients
   4. For each round:
      a. Server sends ROUND:<number> to both clients
      b. Clients send STEAL or SHARE to server (or timeout)
      c. Server processes moves and sends RESULT:... to both clients
   5. After all rounds or on disconnect, server sends GAME_OVER
   6. Server closes connections
   ```

### Socket Programming Implementation

1. **Server-Side Socket Management**:
   ```java
   // Server socket initialization
   serverSocket = new ServerSocket(GameConfig.PORT);
   
   // Accepting client connections (blocking)
   Socket clientSocket = serverSocket.accept();
   
   // Creating I/O streams for client communication
   BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
   PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
   ```

2. **Client-Side Socket Management**:
   ```java
   // Connecting to server
   socket = new Socket("localhost", GameConfig.PORT);
   
   // Creating I/O streams for server communication
   in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
   out = new PrintWriter(socket.getOutputStream(), true);
   
   // Asynchronous message listening
   new Thread(this::listenForMessages).start();
   ```

3. **Concurrency Model**:
   - **Server**: Multi-threaded with one thread per client plus main game thread
   - **Client**: Main UI thread + dedicated network listener thread
   - **Synchronization**: Server coordinates player moves and ensures game consistency

### Network Flow Control and Timing

1. **Timeout Implementation**:
   ```java
   // Server-side timeout handling using ExecutorService and Futures
   ExecutorService executor = Executors.newFixedThreadPool(2);
   Future<String> moveFuture = executor.submit(() -> {
       return in.readLine(); // Read player move
   });
   
   // Try to get move with timeout
   try {
       String move = moveFuture.get(GameConfig.MOVE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
       // Process valid move
   } catch (TimeoutException e) {
       // Handle timeout - default to SHARE
   }
   ```

2. **Buffering and Flushing**:
   - Server and client both use `PrintWriter` with auto-flush enabled
   - Critical messages use explicit `flush()` to ensure immediate transmission
   - Buffered readers improve network efficiency

### Network Reliability and Error Handling

1. **Connection Loss Detection**:
   - IOException during socket read/write indicates connection loss
   - Server detects client disconnection through socket read timeouts
   - Socket exceptions caught and handled to prevent application crashes

2. **Error Recovery**:
   - If a move is invalid or connection drops, server defaults to SHARE
   - Game continues with defaulted move for current round
   - Game ends after the round if player disconnected

### Network Performance Considerations

1. **Bandwidth Usage**:
   - Extremely lightweight protocol (few bytes per message)
   - Text-based messages typically under 100 bytes each
   - Total bandwidth per game session typically under 10KB

2. **Latency Handling**:
   - 30-second move timeout accommodates even high-latency connections
   - Blocking reads on client ensure proper synchronization
   - No real-time action requirements makes the game tolerant of network delays

### Network Security Considerations

1. **Current Implementation**:
   - No encryption of network traffic
   - No authentication mechanism
   - Designed for trusted LAN environments only

2. **Security Recommendations for Production**:
   - Implement SSL/TLS for encrypted communication
   - Add user authentication system
   - Validate all incoming messages to prevent injection attacks
   - Implement rate limiting to prevent denial of service

### Network Debugging and Testing

1. **Debugging Techniques**:
   - Extensive logging of network messages with timestamps
   - Console output of all sent/received messages
   - Error capture and display for network operations

2. **Network Testing Tools**:
   - Wireshark for packet capture and protocol analysis
   - Netcat for manual connection testing
   - Network emulators for simulating poor network conditions

### Common Network Issues and Solutions

1. **Port Already in Use**
   - **Issue**: Cannot start server because port 8080 is already in use
   - **Solution**: Change the port in GameConfig.java or close the application using port 8080

2. **Firewall Blocking**
   - **Issue**: Clients cannot connect to server
   - **Solution**: Configure firewall to allow traffic on port 8080 (TCP)

3. **Connection Timeouts**
   - **Issue**: Clients unable to reach server
   - **Solution**: Verify server IP address, ensure server is running, check network connectivity

4. **NAT Traversal**
   - **Issue**: Cannot connect from external networks
   - **Solution**: Configure port forwarding on router, or use server with public IP address