package com.pennassurancesoftware.jgroups.distributed_task.message;

import java.io.Serializable;

/** Requests all running tasks with the specified definition ID */
public class RequestRunningTasksMessage implements Serializable {
   private static final long serialVersionUID = -7277289977471712107L;

   public RequestRunningTasksMessage() {}

}
