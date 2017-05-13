# jkubernetes

Build jkubernetes
```
git clone https://github.com/gwisoft/jkubernetes.git
mvn clean package install
```

Build install tar 
```
mvn package assembly:assembly
```

install jkubernetes
```
tar -zxvf jkubernetes-all-*.*.*-SNAPSHOT-distribution.tar.gz

cd jkubernetes-all-*.*.*-SNAPSHOT/bin

kube run: nohup kubectl kube &
kubelet run: nohup kubectl kubelet &
```

deployment app
```
kubelet create ***.yaml
```
