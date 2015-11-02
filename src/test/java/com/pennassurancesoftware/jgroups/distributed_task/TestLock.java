package com.pennassurancesoftware.jgroups.distributed_task;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import org.junit.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestLock {
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
   public void testLock() throws Exception {
      // Fixture

      // Call
      final Lock lock = system.getLock( "TST-LOCK" );
      if( lock.tryLock( 2000, TimeUnit.MILLISECONDS ) ) {
         try {
            // access the resource protected by "mylock"
         }
         finally {
            lock.unlock();
         }
      }
      else {
         Assert.fail( "Failed to obtain lock" );
      }

      // Assert
   }
}
