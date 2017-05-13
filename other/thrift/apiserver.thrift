namespace java org.gwisoft.jkubernetes.apiserver.thrift

service ApiServer {
  void submitTopologyStr(1: string yaml);
  void deleteTopology(1: string yaml);
  void rollingUpdateTopology(1: string oldTopologyName, 2: string yaml);
  void replaceTopology(1: string yaml);
  string getTopologyInfo(1: string topologyName);
  string getTopologyInfoAll();
}