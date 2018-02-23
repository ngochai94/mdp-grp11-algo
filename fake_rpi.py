import socket
import threading

host = socket.gethostbyname('0.0.0.0')
PORT = 8080

connection = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
connection.setsockopt( socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
connection.bind((host, PORT))
connection.listen(1)

client, address = connection.accept()
print ('connected')


def send(msg):
    client.sendto(msg, address)


def receive():
    return client.recv(1024)

def sender():
    while True:
        msg = raw_input()
        send(msg)

def receiver():
    while True:
        received = receive()
        if not received:
            break
        print received

sender_thread = threading.Thread(target=sender)
sender_thread.start()

receiver_thread = threading.Thread(target=receiver)
receiver_thread.start()

sender_thread.join()
receiver_thread.join()
