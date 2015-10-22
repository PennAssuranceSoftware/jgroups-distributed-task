package com.pennassurancesoftware.jgroups.distributed_task.meta;

import java.io.Serializable;

public class MemberMeta implements Serializable {
   private static final long serialVersionUID = 4457878300433939263L;

   private double cpuFrequency;
   private int executionThreads;
   private String instanceAddress;
   private int numberOfProcessors;

   public double getCpuFrequency() {
      return cpuFrequency;
   }

   public int getExecutionThreads() {
      return executionThreads;
   }

   public String getInstanceAddress() {
      return instanceAddress;
   }

   public int getNumberOfProcessors() {
      return numberOfProcessors;
   }

   public MemberMeta setCpuFrequency( double cpuFrequency ) {
      this.cpuFrequency = cpuFrequency;
      return this;
   }

   public MemberMeta setExecutionThreads( int executionThreads ) {
      this.executionThreads = executionThreads;
      return this;
   }

   public MemberMeta setInstanceAddress( String instanceAddress ) {
      this.instanceAddress = instanceAddress;
      return this;
   }

   public MemberMeta setNumberOfProcessors( int numberOfProcessors ) {
      this.numberOfProcessors = numberOfProcessors;
      return this;
   }
}
