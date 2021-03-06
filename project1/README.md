# sdis1819-t1g10

>In this project you will develop a distributed backup service for a local area network (LAN). The idea is to use the free disk space of the computers in a LAN for backing up files in other computers in the same LAN. The service is provided by servers in an environment that is assumed cooperative (rather than hostile). Nevertheless, each server retains control over its own disks and, if needed, may reclaim the space it made available for backing up other computers' files.

Description offered on class specifications

## To run on UBUNTU

### 1. Create __compile.sh__ on project1
```
#!/bin/bash
find -name "*.java" > sources.txt
javac -d ./out @sources.txt
rm sources.txt
```
### 2. Run `rmiregistry &` on project1/out

### 3. Run from project1/out  

#### 3.1 Peer  
*Usage: protocol.Peer <protocol_version> <server_id> <MC_ip:MC_port> <MDB_ip:MDB_port> <MDR_ip:MDR_port>*  
`java protocol.Peer 1.0 1 229.0.0.1:1111 229.0.0.1:1112 229.0.0.1:1113`  
    
#### 3.2 TestApp  
##### 3.2.1 Backup  
*Usage: app.TestApp <peer_ap> BACKUP <filepath> <replication_degree>*  
`java app.TestApp localhost:peer1 BACKUP ../FILES/img2.png 1`  
##### 3.2.2 Restore  
*Usage: app.TestApp <peer_ap> RESTORE <filepath>*  
`java app.TestApp peer1 RESTORE ../FILES/img2.png`  
##### 3.2.3 Delete  
*Usage: app.TestApp <peer_ap> DELETE <filepath>*  
`java app.TestApp peer1 DELETE ../FILES/img2.png`  
##### 3.2.4 Reclaim  
*Usage: app.TestApp <peer_ap> RECLAIM <max_size>*  
`java app.TestApp peer1 RECLAIM 10000`  
##### 3.2.5 State  
*Usage: app.TestApp <peer_ap> STATE*  
`java app.TestApp peer1 STATE`  

### 4. Saved Files
All data is saved under TMP folder created on the same level of **src/**
