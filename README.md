# MD5 Cracker

## How to user it

### Prerequisite
#### Linux Version
`Ubuntu 18.04.1 LTS` 

#### Java8
you can use the following instructions to install Java8 environment:
`apt-get install openjdk-8-jre-headless`

#### Python 2.7 with flask
you can use the following instructions to install flask:  
`pip install flask`  
`pip install flask_cors`

#### GENI Resources  
you will need to download the `rspec.xml` and upload it to geni protal to reproduce our GENI topology.  

### Clone the project
`git clone https://github.com/SimpleMD5Craker/MD5Craker.git`

### Deployment of The Web Server
- SSH to your master node. Note done the ip address of your master node. The use following instructions to install Apache2:  
```
sudo apt update
sudo apt install apache2
sudo ufw status // make sure the fire wall is inactive
```  
- Create your homepage dir and allow you to modify the files on it:  
```
sudo mkdir -p /var/www/<your_hostname>.com/html
sudo chown -R <user_name> /var/www/<your_hostname>.com/html
sudo chmod -R 755 /var/www/<your_hostname>.com
```  
- Download the index.html and replace the url at line 84 with the ip address of your master node  
-Configure the apache virtual machine:  
```
sudo nano /etc/apache2/sites-available/<your_hostname>.com.conf

// then paste the following code into the <your_hostname>.com.conf
<VirtualHost *:80>
ServerAdmin admin@<your_hostname>.com
ServerName <your_hostname>.com
ServerAlias www.pc.com
DocumentRoot /var/www/<your_hostname>.com/html
ErrorLog ${APACHE_LOG_DIR}/error.log
CustomLog ${APACHE_LOG_DIR}/access.log combined
</VirtualHost>
```
- Enable the new configure file and disable the old configure file:
```
sudo a2ensite <your_hostname>.com.conf
sudo a2dissite 000-default.conf
sudo systemctl restart apache2
```
- Now you should be able to visity the website through http://ip_address_of_your_master_node/
- We also provide a video instruction https://youtu.be/ieVABTqqMKU.


### Download java and python runnable files

- Download the `Cracker.jar` file we provide here to all you machines, or you can download the code , make some modification, and compile it to make an executable `.jar` file by yourself
- Download the `app.py` in the `frontend` directory to your master node, and download the `crack.py` to your worker nodes.
- Download the `command.txt` to your worker nodes, place it in the directory that you put the `Cracker.jar`. Edit the file,  change `/home/qynglan/crack.py` to the path where you put the file `crack.py`.



### Start the program

- For worker node, the command is:

  `java -jar Cracker.jar worker [ip of the worker node] [ip:port of the master node]`

  Here the port number of master node is fixed to 12345, and if the master has multiple NICs, choose the one that is in the same subnet of the worker node.

- For master node, the command is:

  `java -jar Cracker.jar master [ip of the master node]`.

  If the master node has multiple NICs, you can use `127.0.0.1`

- Link to the page you deployed and configured, now you can try it. Please note that we only support strings of 5 characters, each in lowercase.



