# jkubernetes

1. Build jkubernetes
```
git clone https://github.com/gwisoft/jkubernetes.git
mvn clean package install
```

2. Build install tar 
```
mvn package assembly:assembly
```

3. install jkubernetes
```
tar -zxvf jkubernetes-all-*.*.*-SNAPSHOT-distribution.tar.gz

cd jkubernetes-all-*.*.*-SNAPSHOT/bin

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
