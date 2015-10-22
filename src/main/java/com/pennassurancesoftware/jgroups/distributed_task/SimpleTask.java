package com.pennassurancesoftware.jgroups.distributed_task;

import java.io.DataInput;
import java.io.DataOutput;

import org.jgroups.util.Streamable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pennassurancesoftware.jgroups.distributed_task.type.DistributedTaskType;

/**
 * Defines the interface for tasks that are responsible for a single relatively 
 * short lived action (like calling a web service)
 */
public abstract class SimpleTask extends AbstractDistributedTask implements Runnable, Streamable {
   private static final Logger LOG = LoggerFactory.getLogger( SimpleTask.class );

   public SimpleTask() {
      super( DistributedTaskType.Simple );
   }

   @Override
   public void readFrom( DataInput input ) throws Exception {
      doReadFrom( input );
   }

   @Override
   public void run() {
      try {
         doRun();
      }
      catch( Exception exception ) {
         LOG.error( String.format( "Error processing Worker Task: %s", getId() ), exception );
      }
   }

   @Override
   public void writeTo( DataOutput output ) throws Exception {
      doWriteTo( output );
   }

   protected void doReadFrom( DataInput input ) throws Exception {}

   /**
    * Allows the implementing class to implement the worker logic
    * @throws Exception {@link Exception}
    */
   protected abstract void doRun() throws Exception;

   protected void doWriteTo( DataOutput output ) throws Exception {}
}
