package org.gwisoft.jkubernetes.schedule;

import java.util.Set;

import org.gwisoft.jkubernetes.daemon.pod.ResourcePodSlot;


public interface IToplogyScheduler {

	Set<ResourcePodSlot> assignTasks(TopologyAssignContext context);
}
