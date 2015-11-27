package com.pennassurancesoftware.jgroups.distributed_task.mock1;

import com.pennassurancesoftware.jgroups.distributed_task.SimpleTask;

public class TakeTimeSimpleTask extends SimpleTask {
   private final int seconds;

   public TakeTimeSimpleTask() {
      this( 10 );
   }

   public TakeTimeSimpleTask( int seconds ) {
      this.seconds = seconds;
   }

   @Override
   protected void doRun() throws Exception {
      for( int index = 1; index <= seconds; index++ ) {
         System.out.println( String.format( "Take Time Task: %s of %s seconds", index, seconds ) );
         Thread.sleep( 1000 );
      }
   }

   @Override
   public String getDescription() {
      return String.format( "Take Time Task: %s seconds", seconds );
   }
}
