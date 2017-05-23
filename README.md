# jkubernetes

1. Build with Maven
```
Prerequisites:
* Java min 1.8
* Maven 3
```
```
Build and run integration tests as follows:
mvn clean package install
```

2. Build install tar 
```
mvn package assembly:assembly
```

3. install jkubernetes
```
Install the zookeeper:
* Install...Refer to the official
```
```
tar -zxvf jkubernetes-all-*.*.*-SNAPSHOT-distribution.tar.gz
cd jkubernetes-all-*.*.*-SNAPSHOT
vi conf/jkubernetes.yaml ....edit zookeeper server ip and port

cd bin
kube run: nohup kubectl kube &
kubelet run: nohup kubectl kubelet &
```

4. deployment app
```
kubelet create -f ***.yaml
```
```
kubectl delete -f ***.yaml
```
```
kubectl rolling-update [old topology name] -f ***.yaml
```
```
kubelet get po [topology name]
```
```
kubelet replace -f ***.yaml
```


5、yaml file template
```
* command.yaml
apiVersion: v1
kind: ReplicationController
metadata:
 name: command_test_app
spec:
 replicas: 2
 template:
  spec:
   command: /usr/downloads/command_test_app/run.sh
```

```
* docker.yaml
apiVersion: v1
kind: ReplicationController
metadata:
 name: docker_test_app
spec:
 replicas: 2
 template:
  spec:
   containers:
    - name: docker_test_app
      image: gwisoft/centos:jdk
``` 
