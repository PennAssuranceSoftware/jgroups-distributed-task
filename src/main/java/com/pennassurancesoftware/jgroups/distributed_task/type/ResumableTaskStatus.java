package com.pennassurancesoftware.jgroups.distributed_task.type;

/** Defines the types of statuses that can be returned from a long running task */
public enum ResumableTaskStatus {
   Wait, Complete, Null, ;
}
