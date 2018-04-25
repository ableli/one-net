# Onenet ： A pure java,excelling and beyond  implementation of Ngrok or FRP 

Make your PCs'services access by Internet.
Release your Pc's cloud capability.
Client/Server mode.
High performance on High concurrent.
Industry level software.
## Release
* Latest version: 1.3-RELEASE
* Sources branch: 1.3-RELEASE
* Update Note 1.3-RELEASE.
    * Enable the client pool config.
    * Fix the major issue of too many invalid OneNet channels.
    * Add timeout in local connection creation process.

## Features
1. TCP and HTTP one direction tunnel
   > Http context need 80 port and it parse request host name to match config context) 
   >, and the tcp context doing no parse on data trans in.
2. Traffic control for each context/tunnel.
2. Gzip Support
3. AES Support
	>Need JCE to support
4. One Server Support Mutipl Domains
5. Mutil Channl Server/Client Support
6. Cross Platform(Linux, Windows and the other can run JRE8)

## User Guide
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
        - [one-net-server-clear-1.3-RELEASE](http://www.weyong.net/1.3/server/one-net-server-clear-1.3-RELEASE.jar)
        - [one-net-client-clear-1.3-RELEASE](http://www.weyong.net/1.3/client/one-net-client-clear-1.3-RELEASE.jar)
     > The files hosted in my home and tunnel by OneNet via Cloud VPS :)
3. Config your tunnels
    - create application.yml files for both server and client, the following template is for Server.
    - The following template demonstrate blow four contexts:
    
        Context | Tunnel In | Tunnel Out|
        --- | --- | ---
        weyong | [server]:83|  [127.0.0.1]:88
        mstsc | [server]:56789 | [127.0.0.1]:3389
        test1 | [server]:80 | [127.0.0.1]:88
        test2 | [server]:80 | [127.0.0.1]:82||

        [application.yml for server](http://www.weyong.net/1.3/server/application.yml)
        
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
            
        [application.yml for client](http://www.weyong.net/1.3/client/application.yml)
        
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
                      poolConfig:
                        maxIdle: 50
                        minIdle: 30
                        maxTotal: 1024
                        blockWhenExhausted: true
                        fireness: true
                        testWhileIdle: true
                        testOnBorrow: true
                        testOnReturn: false
                        maxWaitMillis: 100
                        timeBetweenEvictionRunsMillis: 1000
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
        > The application.yml need edit in professional editor VS Code and IDEA .etc, Any informat or speicial char will cause error.
        The pool config is critical to performance, change the value until you know what it is.
4. Start Server and Client.
    - put application.yml in the same folder with jar file
        ```shell
        #for server
        /usr/bin/java -Xmx256m -jar one-net-server-clear-1.3-RELEASE.jar > one-net-server.log
        #for client
        /usr/bin/java -Xmx256m -jar one-net-client-clear-1.3-RELEASE.jar > one-net-client.log
        ```
5. Check Internet access of your services.
   - check your local service access by Internet. 

## Performance
1. The IO core is base on Netty 4.1.9. It is total as-sync.
2. Byte level transfer protocol result in low cost.
3. Localhost connection pool supported.
3. 100 threads, 100 request each thread,  test result.
    ![alt text](https://raw.githubusercontent.com/ableli/one-net/master/doc/img/100X100-Request-Test.png "Logo Title Text 1")


## Road Map
1. Web UI Console
