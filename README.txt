This code can be built from eclipse,

Alternatively, one only needs to install the 
SimpleAndroidClient.apk located in /bin of this directory.

It is suggested to run the server first, to determine
what socket and IP the client should search for the
server on. The client will only be able to connect
on the socket and ip address that the server is passed 
as arguments.

When connected to the server, the client may enter
one message that will be sent to the server.
The server will then use the cipher method outlined in 
earlier assignments and return the cipher text
of the sent message. This message will be displayed
to the client in the text view above the edit text
field of the main activity. 