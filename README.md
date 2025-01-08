# Graphical IRC client 

## Features
- Connect to a running chat server and listen for messages.
- Allow the user to specify the address and port number of the server.
- Register on the server with a nickname chosen by the user.
- Display incoming chat messages from other server users.
- Allow the user to send messages to other server users.
- Allow the user to join and leave channels, and send and receive messages in them.
- Graphical user interface (GUI) for all implemented functionality, using Swing.
- Display a list of all channels on the server.
- Display the users that are in each channel the user has joined.
- Allow the user to connect to multiple servers simultaneously.

## How to run
- Clone the repository
- Run the .jar file in the build directory

## Server protocol
This client is designed to connect to a server that uses a simplified version of the IRC protocol. Any errors should be reported to the client in this format:
ERROR \<error message>.

The server should support the following commands: 

NICK \<nickname>
attempts to register with the server as a chat user, with the given nickname

QUIT
terminates the connection to the server

JOIN \<channel>
joins the channel with the given name, which must start with a hash symbol

PART \<channel>
leaves the channel with the given name

NAMES \<channel>
requests a list of all users in the given channel

LIST
requests a list of all channels that currently exist on the server

PRIVMSG \<target> \<message>
sends a chat message to the given channel or directly to a user
- \<target> should be the name of a channel or user
- \<message> is the text of the message to be displayed

TIME
requests the current time on the server

PING \<message>
requests a response from the server, which should include the given message

INFO
requests information about the server
