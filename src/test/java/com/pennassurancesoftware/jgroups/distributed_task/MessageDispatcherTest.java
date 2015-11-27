package com.pennassurancesoftware.jgroups.distributed_task;

import org.jgroups.Channel;
import org.jgroups.Message;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestHandler;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.util.RspList;
import org.jgroups.util.UUID;
import org.jgroups.util.Util;
import org.testng.Assert;

import com.pennassurancesoftware.jgroups.distributed_task.mock1.MockSimpleTask;

public class MessageDispatcherTest implements RequestHandler {
   Channel channel;
   MessageDispatcher disp;
   @SuppressWarnings("rawtypes")
   RspList rsp_list;
   String props; // to be set by application programmer

   public void start() throws Exception {
      channel = new DistributedTaskSystem.DefaultChannel().create();
      disp = new MessageDispatcher( channel, null, null, this );
      // channel.connect( "MessageDispatcherTestGroup" );

      for( int i = 0; i < 10; i++ ) {
         Util.sleep( 100 );
         System.out.println( "Casting message #" + i );
         final RequestOptions opts = new RequestOptions( ResponseMode.GET_ALL, 100000 );
         rsp_list = disp.castMessage( null,
               new Message( null, null, new String( "Number #" + i ) ), opts );
         Assert.assertTrue( !rsp_list.isEmpty(), "No Response found!!" );
         System.out.println( "Responses:\n" + rsp_list );
      }
      channel.close();
      disp.stop();
   }

   public Object handle( Message msg ) {
      System.out.println( "handle(): " + msg );
      // return new String( "Success !" );
      return new RunningTaskRef()
            .setId( UUID.randomUUID().toString() )
            .setInstanceAddress( "blah" )
            .setTaskClass( MockSimpleTask.class )
            .setThreadName( "what what" );
   }

   public static void main( String[] args ) {
      try {
         new MessageDispatcherTest().start();
      }
      catch( Exception e ) {
         System.err.println( e );
      }
   }
}