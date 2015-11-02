package com.pennassurancesoftware.jgroups.distributed_task;

import java.util.Date;
import java.util.concurrent.Future;

import org.junit.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.pennassurancesoftware.jgroups.distributed_task.mock1.MockSimpleTask;
import com.pennassurancesoftware.jgroups.distributed_task.mock1.MockSimpleTask2;

public class TestSimpleTask {

   private DistributedTaskSystem system;

   @BeforeClass(groups = { "unit" })
   public void setup() throws Exception {
      system = new DistributedTaskSystem.Std();
   }

   @AfterClass(groups = { "unit" })
   public void tearDown() throws Exception {
      system.getChannel().close();
   }

   @Test(groups = { "unit" })
   public void testTask1() throws Exception {
      // Fixture

      // Call
      final MockSimpleTask task = new MockSimpleTask();
      final Future<?> future = system.submitTask( task );
      future.get(); // Wait

      // Assert

   }

   @Test(groups = { "unit" })
   public void testTask2() throws Exception {
      // Fixture

      // Call
      final MockSimpleTask2 task = new MockSimpleTask2();
      final Future<Date> future = system.submitTask( task );
      final Date result = future.get(); // Wait

      // Assert
      Assert.assertNotNull( result );
   }

}
