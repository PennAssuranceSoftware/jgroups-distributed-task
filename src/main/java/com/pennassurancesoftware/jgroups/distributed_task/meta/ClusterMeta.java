package com.pennassurancesoftware.jgroups.distributed_task.meta;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ClusterMeta implements Serializable {
   private static final long serialVersionUID = 4296391434201089225L;

   private Integer executionThreads;
   private String instanceAddress;
   private String instanceName;
   private List<MemberMeta> members = new ArrayList<MemberMeta>();
   private String name;

   @Override
   public String toString() {
      return String.format( "%s(Instance[%s:%s], Members: %s)",
            name,
            instanceName,
            instanceAddress,
            members.size() );
   }

   public Integer getExecutionThreads() {
      return executionThreads;
   }

   public String getInstanceAddress() {
      return instanceAddress;
   }

   public String getInstanceName() {
      return instanceName;
   }

   public List<MemberMeta> getMembers() {
      return members;
   }

   public String getName() {
      return name;
   }

   public Integer getTotalExecutionThreads() {
      Integer result = 0;
      for( MemberMeta member : members ) {
         result += member.getExecutionThreads();
      }
      return result;
   }

   public ClusterMeta setExecutionThreads( Integer executionThreads ) {
      this.executionThreads = executionThreads;
      return this;
   }

   public ClusterMeta setInstanceAddress( String instanceAddress ) {
      this.instanceAddress = instanceAddress;
      return this;
   }

   public ClusterMeta setInstanceName( String instanceName ) {
      this.instanceName = instanceName;
      return this;
   }

   public ClusterMeta setMembers( List<MemberMeta> members ) {
      this.members = members;
      return this;
   }

   public ClusterMeta setName( String name ) {
      this.name = name;
      return this;
   }
}
