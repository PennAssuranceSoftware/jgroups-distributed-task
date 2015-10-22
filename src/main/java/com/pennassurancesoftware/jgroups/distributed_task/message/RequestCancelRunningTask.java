package com.pennassurancesoftware.jgroups.distributed_task.message;

public class RequestCancelRunningTask {
   private String id;

   public RequestCancelRunningTask( String id ) {
      this.id = id;
   }

   public String getId() {
      return id;
   }
}
