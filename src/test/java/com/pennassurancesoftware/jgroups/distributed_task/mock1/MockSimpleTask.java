package com.pennassurancesoftware.jgroups.distributed_task.mock1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pennassurancesoftware.jgroups.distributed_task.SimpleTask;

public class MockSimpleTask extends SimpleTask {
   private static Logger LOG = LoggerFactory.getLogger( MockResumableTask.class );

   @Override
   protected void doRun() throws Exception {
      LOG.info( "Simple Task running" );
   }

   @Override
   public String getDescription() {
      return "Simple Test";
   }

}
