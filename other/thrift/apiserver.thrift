namespace java org.jkubernetes.apiserver.thrift

service ApiServer {
  void submitTopology(1: binary yaml);
  void submitTopologyStr(1: string yaml);
}