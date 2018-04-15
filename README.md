#Onenet ： A pure java,excelling and beyond  implementation of Ngrok or FRP 

Make your PCs access by Internet.
Release your Pc's cloud capability.
Client/Server mode.
High performance on High concurrent.
Industry level software.
##
* Latest version: 1.2-RELEASE
* Sources branch: 1.2-RELEASE

##Features
1. TCP and HTTP one direction tunnel
2. Traffic control for each context/tunnel.
2. Gzip Support
3. AES Support
4. One Server Support Mutipl Domains
5. Mutil Channl Server/Client Support
6. Cross Platform(Linux, Windows and the other can run JRE8)

##User Guide
1. Install Java 8 on your Server(Cloud VPS or any PC can access by Internet ) and Pc.
   >  The AES feature need JCE lib for JRE.
2. Download(or self build) both server and client of OneNet.
    - Self build:
    ```java
    #for server
    [project root dir] gradlew one-net-server:clearBoot
    ...
    #for client
    [project root dir] gradlew one-net-client:clearBoot
    ```
    the jar output are in [project root dir][sub project dir]/build/libs
    - Direct download
     [one-net-server-clear-1.2-RELEASE](http://www.weyong.net/one-net-server-clear-1.2-RELEASE)
     [one-net-client-clear-1.2-RELEASE](http://www.weyong.net/one-net-client-clear-1.2-RELEASE)
     -- The files hosted in my home and tunnel by OneNet via Cloud VPS :)
3. Config your tunnels
    - create application.yml files for both server and client, the following template is for Server.
    - The following template demonstrate blow four contexts:
    
Context | Tunnel In | Tunnel Out|
--- | --- | ---
weyong | [server]:83|  [127.0.0.1]:88
mstsc | [server]:56789 | [127.0.0.1]:3389
test1 | [server]:80 | [127.0.0.1]:88
test2 | [server]:80 | [127.0.0.1]:82||

[application.yml for server](http://www.weyong.net/one-net-server-clear-1.2-RELEASE)

    ```yml
    #The OneNetServer config template
    #The file name should be : application.yml
    server:
      port: 8080
      tomcat:
        uri-encoding: UTF-8
    logging:
      level:
        root: error
        org.springframework.boot: error
        com.weyong: info
        io.netty.handler.logging: debug
    oneNetServer:
      name: firstServer
      oneNetPort: 9527
      tcpContexts:
        -
          contextName: weyong
          internetPort: 83
          zip: true
          aes: false
          kBps: 200
        -
          contextName: mstsc
          internetPort: 56789
          kBps: 200
          zip: true
          aes: false
      httpContexts:
        -
          contextName: test1
          zip: true
          aes: false
          kBps: 200
          domainRegExs:
            -
              \w+.test1.com
        -
          contextName: test2
          zip: true
          aes: false
          kBps: 200
          domainRegExs:
            -
              \w+.test2.com
    ```
    
[application.yml for client](http://www.weyong.net/one-net-client-clear-1.2-RELEASE)

    ```yml
    #The OneNetClient config template
    #The file name should be : application.yml
    server:
      port: 8081
      tomcat:
        uri-encoding: UTF-8
    logging:
      level:
        root: error
        org.springframework.boot: error
        com.weyong: debug
        io.netty.handler.logging: debug
    oneNetClient:
      serverName: localhost
      reconnectAfterNSeconds: 7
      serverConfigs:
        -
          hostName: localhost
          oneNetPort: 9527
          contexts:
            -
              contextName: weyong
              localhost: 127.0.0.1
              port: 88
              localPool: true
            -
              contextName: mstsc
              localhost: 127.0.0.1
              port: 3389
              localPool: false
            -
              contextName: test1
              localhost: 127.0.0.1
              port: 88
              localPool: false
            -
              contextName: test2
              localhost: 127.0.0.1
              port: 82
              localPool: false
    ```
4. Start Server and Client.
    - put application.yml in the same folder with jar file
    ```shell
    #for server
    /usr/bin/java -Xmx256m -jar one-net-server-clear-1.2-RELEASE.jar > one-net-server.log
    #for client
    /usr/bin/java -Xmx256m -jar one-net-client-clear-1.2-RELEASE.jar > one-net-client.log
    ```
5. Check Internet access of your services.
   - check your local service access by Internet. 


##Road Map
1. Web UI Console
