package com.pennassurancesoftware.jgroups.distributed_task;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.pennassurancesoftware.jgroups.distributed_task.meta.ClusterMeta;
import com.pennassurancesoftware.jgroups.distributed_task.mock1.TakeTimeSimpleTask;

public class TestRequests {

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
   public void runningTasks() throws Exception {
      // Fixture

      // Call
      final TakeTimeSimpleTask task = new TakeTimeSimpleTask( 1000 );
      system.submitTask( task );
      Thread.sleep( 1000 );
      final int count = system.getRunningTasks().size();

      // Assert
      Assert.assertEquals( count, 1 );
   }

   @Test(groups = { "unit" })
   public void clusterMeta() throws Exception {
      final ClusterMeta meta = system.getClusterMeta();
      System.out.println( meta );

      Assert.assertEquals( meta.getMembers().size(), 1 );
   }

}
