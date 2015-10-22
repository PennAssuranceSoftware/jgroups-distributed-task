package com.pennassurancesoftware.jgroups.distributed_task;

public class DistributedTaskSystemConfiguration {
   private static final int DEFAULT_CLUSTER_THREADS = 10;

   private int executionThreadCount = DEFAULT_CLUSTER_THREADS;

   public int getExecutionThreadCount() {
      return executionThreadCount;
   }

   public void setExecutionThreadCount( int executionThreadCount ) {
      this.executionThreadCount = executionThreadCount;
   }
}
