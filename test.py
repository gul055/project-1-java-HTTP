from socket import socket

# Create connection to the server
s = socket()
s.connect(("localhost", 8080))

# Compose the message/HTTP request we want to send to the server
msgPart1 = b"GET /../../request.input HTTP/1.1\r\nHost: Ha\r\n\r\nGET /index.html HTTP/1.1\r\nHost: Ha\r\nContent:  aaa\r\nConnection: close\r\n\r\nGET /index.html HTTP/1.1\r\nHost: 1\r\n\r\n"
# msgPart1 = b"GET /../../request.input HTTP/1.1\r\nHost: Ha\r\n\r\nGAT /index.html HTTP/1.1\r\nHost: Ha\r\nContent:  aaa\r\n\r\n"
# msgPart1 = b"GET /index.html HTTP/1.1\r\nHost: Ha\r\n\r\n"

# Send out the request
s.sendall(msgPart1)

# Listen for response and print it out
# while True:
#     print (s.recv(4096))
res = ""
temp = s.recv(20)
while temp != b'':
  res += temp.decode("utf-8")
  temp = s.recv(20)
print(res)
s.close()
