# MD5 Cracker

## How to user it

### Prerequisite

`Java8`

`Python 2.7` with flask

### Deployment Web service





### Download java and python runnable files

- Download the `Cracker.jar` file we provide here to all you machines, or you can download the code , make some modification, and compile it to make an executable `.jar` file by yourself
- Download the `app.py` in the `frontend` directory to your master node, and download the `crack.py` to your worker nodes.
- Download the `command.txt` to your worker nodes, place it in the directory that you put the `Cracker.jar`. Edit the file,  change `/home/qynglan/crack.py` to the path where you put the file `crack.py`.



### Start the program

- For worker node, the command is:

  `java -jar Cracker.jar worker [ip of the worker node] [ip:port of the master node]`

  Here the port number of master node is fixed to 12345.

- For master node, the command is:

  `java -jar Cracker.jar master [ip of the master node]`.

  If the master node has multiple NICs, you can use `127.0.0.1`

- Link to the page you deployed and configured, now you can try it. Please note that we only support strings of 5 characters, each in lowercase.



