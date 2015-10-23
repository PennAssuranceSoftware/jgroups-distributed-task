package com.pennassurancesoftware.jgroups.distributed_task;

public class RunningTaskRef {
   private String id;
   private String instanceAddress;
   private Class<?> taskClass;
   private String threadName;

   public String getId() {
      return id;
   }

   public String getInstanceAddress() {
      return instanceAddress;
   }

   public Class<?> getTaskClass() {
      return taskClass;
   }

   public String getThreadName() {
      return threadName;
   }

   public RunningTaskRef setId( String id ) {
      this.id = id;
      return this;
   }

   public RunningTaskRef setInstanceAddress( String instanceAddress ) {
      this.instanceAddress = instanceAddress;
      return this;
   }

   public RunningTaskRef setTaskClass( Class<?> taskClass ) {
      this.taskClass = taskClass;
      return this;
   }

   public RunningTaskRef setThreadName( String threadName ) {
      this.threadName = threadName;
      return this;
   }
}