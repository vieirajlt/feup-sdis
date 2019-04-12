# feup-sdis

## To run on UBUNTU

1. Create __compile.sh__ on project1
    ```
    #!/bin/bash
    find -name "*.java" > sources.txt
    javac -d ./out @sources.txt
    rm sources.txt
    ```
2. Run `rmiregistry &` on project1/out

3. Run from project1/out  
    3.1 `java protol.Peer 1.0 1 229.0.0.1:1111 229.0.0.1:1112 229.0.0.1:1113`  
    3.2 `java app.TestApp peer1 BACKUP ../FILES/img2.png 1`  
