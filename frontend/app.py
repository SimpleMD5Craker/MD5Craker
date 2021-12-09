#!/usr/bin/python
# -*- coding: UTF-8 -*-
import time
import hashlib
from flask import Flask, jsonify, request
from flask_cors import CORS
import socket  

app = Flask(__name__)
CORS(app)

@app.route('/decode', methods=['GET', 'POST'])
def decode():
    if request.method == 'GET':
        try:
            uid = request.args['uid']
        except:
            uid = "syp"
        password = request.args['password']
    elif request.method == 'POST':
        print(request.json)
        uid = request.json['uid']
        password = request.json['password']
    start = time.time()
    message = "{}:{}\n".format(uid, password)
    s.send(message.encode("utf-8"))
    print("send: {}".format(message))
    recv_msg, _ = s.recvfrom(2048)
    recv_msg = recv_msg.decode()
    code = recv_msg.strip().split(':')[-1]
    end = time.time()
    res = {"uid":uid, "result":code, "time": end - start}
    print(res)
    return res
    


if __name__=='__main__':
    s = socket.socket()
    s.connect(("localhost", 9000))
    app.run(host='0.0.0.0', port=31000)