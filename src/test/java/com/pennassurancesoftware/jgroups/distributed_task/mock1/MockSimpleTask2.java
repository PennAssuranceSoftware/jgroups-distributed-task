package com.pennassurancesoftware.jgroups.distributed_task.mock1;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pennassurancesoftware.jgroups.distributed_task.SimpleTask;
import com.pennassurancesoftware.jgroups.distributed_task.WithResult;

public class MockSimpleTask2 extends SimpleTask implements WithResult<Date> {
   private static Logger LOG = LoggerFactory.getLogger( MockResumableTask.class );

   private Date date;

   @Override
   protected void doRun() throws Exception {
      LOG.info( "Simple Task running" );
      date = new Date();
   }

   @Override
   public String getDescription() {
      return "Simple Test";
   }

   @Override
   public Date getResult() {
      return date;
   }

}
