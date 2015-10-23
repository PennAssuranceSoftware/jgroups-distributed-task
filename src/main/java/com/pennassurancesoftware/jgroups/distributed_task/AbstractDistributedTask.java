package com.pennassurancesoftware.jgroups.distributed_task;

import java.util.concurrent.Callable;

import org.jgroups.util.Streamable;
import org.jgroups.util.UUID;

import com.pennassurancesoftware.jgroups.distributed_task.type.DistributedTaskType;

/** Abstract definition of distributed tasks */
@SuppressWarnings("javadoc")
public abstract class AbstractDistributedTask implements Callable<Object>, Streamable {
   private final DistributedTaskType type;
   private final String id;

   /** @return Description of the currently running instance of the task */
   public abstract String getDescription();

   public AbstractDistributedTask( DistributedTaskType type ) {
      this( type, UUID.randomUUID().toString().toUpperCase() );
   }

   public AbstractDistributedTask( DistributedTaskType type, String id ) {
      this.type = type;
      this.id = id;
   }

   public DistributedTaskType getType() {
      return type;
   }

   public String getId() {
      return id;
   }

   @Override
   public String toString() {
      return String.format( "%s(%s):%s", getType(), getId(), getDescription() );
   }
}
