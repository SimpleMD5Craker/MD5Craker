#!/usr/bin/python
# -*- coding: UTF-8 -*-
import time
import hashlib
from flask import Flask, jsonify, request
from flask_cors import CORS
import socket  

app = Flask(__name__)
CORS(app)
PORT = 12345
HOST = socket.gethostname() 


def hashcode(code):
    hl = hashlib.md5()
    hl.update(code.encode(encoding='utf-8'))
    return hl.hexdigest()


def permutations(iterable, r=None):
    # permutations('ABCD', 2) --> AB AC AD BA BC BD CA CB CD DA DB DC
    # permutations(range(3)) --> 012 021 102 120 201 210
    pool = tuple(iterable)
    n = len(pool)
    r = n if r is None else r
    if r > n:
        return
    indices = list(range(n))
    cycles = list(range(n, n-r, -1))
    yield tuple(pool[i] for i in indices[:r])
    while n:
        for i in reversed(range(r)):
            cycles[i] -= 1
            if cycles[i] == 0:
                indices[i:] = indices[i+1:] + indices[i:i+1]
                cycles[i] = n - i
            else:
                j = cycles[i]
                indices[i], indices[-j] = indices[-j], indices[i]
                yield tuple(pool[i] for i in indices[:r])
                break
        else:
            return
        

def get_char_list():
    res = []
    for c in range(ord("a"), ord("z")+1):
        res.append(chr(c))
    for c in range(ord("A"), ord("Z")+1):
        res.append(chr(c))
    return res


def password_cracker(password, length=5):
    char_list = get_char_list()
    code_generator = permutations(char_list,5)
    for code in code_generator:
        code = "".join(code)
        print(code)
        if hashcode(code) == password:
            return code
    return "not found!"
        

@app.route('/decode', methods=['GET', 'POST'])
def decode():
    if request.method == 'GET':
        try:
            uid = request.args['uid']
        except:
            uid = "syp"
        password = request.args['password']
    elif request.method == 'POST':
        # print("in request.method == 'POST'")
        # print("ffffff")
        # print(request.get_json())
        print(request.json)
        # print("hhhhhhh")
        uid = request.json['uid']
        password = request.json['password']
    # result = password_cracker(password)
    s = socket.socket()
    s.connect((HOST, PORT))
    # s.send((str(uid) + "|" + str(password)).encode("utf-8"))
    s.send("hello, this info is from a.py".encode("utf-8"))
    recv_msg, _ = s.recvfrom(10000)
    print(recv_msg)
    recv_msg = recv_msg.decode()
    print("after decode")
    print(recv_msg)
    return {"uid":uid, "result":recv_msg}
    


if __name__=='__main__':
    
    app.run(host='0.0.0.0', port=31000)