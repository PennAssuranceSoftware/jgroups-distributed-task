package com.pennassurancesoftware.jgroups.distributed_task.mock1;

import java.io.DataInput;
import java.io.DataOutput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pennassurancesoftware.jgroups.distributed_task.ResumableTask;
import com.pennassurancesoftware.jgroups.distributed_task.type.ResumableTaskStatus;

public class MockResumableTask extends ResumableTask {
   private static final Logger LOG = LoggerFactory.getLogger( MockResumableTask.class );

   private int counter = 0;

   @Override
   public void readFrom( DataInput input ) throws Exception {
      counter = input.readInt();
   }

   @Override
   public void writeTo( DataOutput output ) throws Exception {
      output.writeInt( counter );
   }

   @Override
   public String getDescription() {
      return "Counting: " + counter;
   }

   @Override
   protected ResumableTaskStatus doRun() throws Exception {
      counter++;
      LOG.info( "Polling {}", counter );
      return ResumableTaskStatus.Wait;
   }

   @Override
   protected ResumableTaskStatus doResume() throws Exception {
      counter++;
      LOG.info( "Polling (Resumed) {}", counter );
      return counter > 3 ? ResumableTaskStatus.Complete : ResumableTaskStatus.Wait;
   }
}
