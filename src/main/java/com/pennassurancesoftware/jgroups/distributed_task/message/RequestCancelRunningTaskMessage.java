package com.pennassurancesoftware.jgroups.distributed_task.message;

import java.io.Serializable;

public class RequestCancelRunningTaskMessage implements Serializable {
   private static final long serialVersionUID = 1597611642222841201L;
   
   private String id;

   public RequestCancelRunningTaskMessage( String id ) {
      this.id = id;
   }

   public String getId() {
      return id;
   }
}
