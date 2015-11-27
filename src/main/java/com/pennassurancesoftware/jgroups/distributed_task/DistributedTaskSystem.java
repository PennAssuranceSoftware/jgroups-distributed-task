package com.pennassurancesoftware.jgroups.distributed_task;

import java.lang.reflect.Constructor;
import java.net.DatagramSocket;
import java.net.DatagramSocketImpl;
import java.net.DatagramSocketImplFactory;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestHandler;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.blocks.executor.ExecutionRunner;
import org.jgroups.blocks.executor.ExecutionService;
import org.jgroups.blocks.executor.ExecutionService.DistributedFuture;
import org.jgroups.blocks.locking.LockNotification;
import org.jgroups.blocks.locking.LockService;
import org.jgroups.util.Filter;
import org.jgroups.util.Owner;
import org.jgroups.util.Rsp;
import org.jgroups.util.RspList;
import org.jgroups.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pennassurancesoftware.jgroups.distributed_task.message.RequestCancelRunningTaskMessage;
import com.pennassurancesoftware.jgroups.distributed_task.message.RequestClusterMemberMetaMessage;
import com.pennassurancesoftware.jgroups.distributed_task.message.RequestRunningTasksMessage;
import com.pennassurancesoftware.jgroups.distributed_task.meta.ClusterMeta;
import com.pennassurancesoftware.jgroups.distributed_task.meta.MemberMeta;
import com.pennassurancesoftware.jgroups.distributed_task.meta.SystemMeta;
import com.pennassurancesoftware.jgroups.distributed_task.util.Field;
import com.sun.jna.Platform;

/** Central class to interact and register all distributed tasks */
public interface DistributedTaskSystem {

   public static class DefaultChannel {
      private static final Logger LOG = LoggerFactory.getLogger( DefaultChannel.class );

      public JChannel create() {
         try {
            workaround_jgrp1970();

            final JChannel channel = new JChannel( getClass().getResourceAsStream( "/udp-distributed-task.xml" ) );
            channel.connect( String.format( "CLUSTER_%s", UUID.randomUUID().toString().toUpperCase() ) );
            return channel;
         }
         catch( Exception exception ) {
            throw new RuntimeException( "Failed to create default JGroups channel", exception );
         }
      }

      private void workaround_jgrp1970() {
         // Workaround: https://issues.jboss.org/browse/JGRP-1970
         // https://github.com/belaban/JGroups/wiki/FAQ#windows-issue-with-setting-udpip_ttl-in-ipv6-read-the-update-at-the-end-
         // NOTE: Only needed for Windows
         try {
            if( Platform.isWindows() ) {
               DatagramSocket.setDatagramSocketImplFactory( new DatagramSocketImplFactory() {
                  @Override
                  public DatagramSocketImpl createDatagramSocketImpl() {
                     try {
                        final Class<?> clazz = Class.forName( "java.net.TwoStacksPlainDatagramSocketImpl" );
                        final Constructor<?> contr = clazz.getDeclaredConstructors()[0];
                        contr.setAccessible( true );
                        return ( DatagramSocketImpl )contr.newInstance( true );
                     }
                     catch( Exception ex ) {
                        throw new RuntimeException( ex );
                     }
                  }
               } );
            }
         }
         catch( SocketException exception ) {
            LOG.warn( "Workaround for JGRP-1970 failed: Most likely the factory has already been set. Error message: {}", exception.getMessage() );
         }
         catch( Exception exception ) {
            throw new RuntimeException( String.format( "Workaround for JGRP-1970 failed" ), exception );
         }
      }
   }

   public static class RequestHandlerByRequestClass {
      private final RequestHandler handler;
      private final Class<?> requestClass;

      public RequestHandlerByRequestClass( Class<?> requestClass, RequestHandler handler ) {
         this.requestClass = requestClass;
         this.handler = handler;
      }

      public RequestHandler getHandler() {
         return handler;
      }

      public Class<?> getRequestClass() {
         return requestClass;
      }
   }

   public static class Std implements DistributedTaskSystem {

      private static final Logger LOG = LoggerFactory.getLogger( DistributedTaskSystem.class );
      private JChannel channel;
      private DistributedTaskSystemConfiguration configuration = new DistributedTaskSystemConfiguration();
      private MessageDispatcher dispatcher;
      private LockService lockService;
      private List<RequestHandlerByRequestClass> requestHandlers = new ArrayList<DistributedTaskSystem.RequestHandlerByRequestClass>();

      private ExecutionRunner runner;

      public Std() {
         this( new DefaultChannel().create() );
      }

      public Std( JChannel channel ) {
         this( channel, new DistributedTaskSystemConfiguration() );
      }

      private Std( JChannel channel, DistributedTaskSystemConfiguration configuration ) {
         this.configuration = configuration;
         this.channel = channel;
         init();
      }

      @Override
      public boolean cancelRunningTask( String instanceAddress, String id ) {
         final RspList<Boolean> responses = sendMessageToSingle( instanceAddress, new RequestCancelRunningTaskMessage( id ) );
         boolean result = false;
         for( Boolean response : responses.getResults() ) {
            if( response ) {
               result = true;
            }
         }
         return result;
      }

      @Override
      public JChannel getChannel() {
         return channel;
      }

      /** @return Current State of the cluster */
      @Override
      public ClusterMeta getClusterMeta() {
         final ClusterMeta result = new ClusterMeta()
               .setName( channel.getClusterName() )
               .setInstanceName( channel.getName() )
               .setInstanceAddress( channel.getAddressAsString() )
               .setExecutionThreads( configuration.getExecutionThreadCount() );

         // Members
         final RspList<MemberMeta> responses = sendMessageToAll( new RequestClusterMemberMetaMessage() );
         result.getMembers().addAll( responses.getResults() );
         return result;
      }

      @Override
      public DistributedTaskSystemConfiguration getConfiguration() {
         return configuration;
      }

      @Override
      public Lock getLock( String lockName ) {
         return lockService.getLock( lockName );
      }

      @Override
      public RunningTaskRef getRunningTask( String instanceAddress, String id ) {
         RunningTaskRef result = null;
         final RspList<List<RunningTaskRef>> responses = sendMessageToSingle( instanceAddress, new RequestRunningTasksMessage() );
         for( List<RunningTaskRef> response : responses.getResults() ) {
            for( RunningTaskRef def : response ) {
               if( def.getId().equals( id ) ) {
                  result = def;
                  break;
               }
            }
         }
         return result;
      }

      /** @return Currently Running Tasks on the cluster */
      @Override
      public List<RunningTaskRef> getRunningTasks() {
         final RspList<List<RunningTaskRef>> responses = sendMessageToAll( new RequestRunningTasksMessage() );
         final List<RunningTaskRef> result = new ArrayList<RunningTaskRef>();
         for( List<RunningTaskRef> response : responses.getResults() ) {
            result.addAll( response );
         }
         return result;
      }

      @Override
      public List<RunningTaskRef> getRunningTasks( final Class<?> taskClass ) {
         return getRunningTasks( new Filter<RunningTaskRef>() {
            @Override
            public boolean accept( RunningTaskRef ref ) {
               return ref.getTaskClass().equals( taskClass );
            }
         } );
      }

      @Override
      public List<RunningTaskRef> getRunningTasks( Filter<RunningTaskRef> filter ) {
         final List<RunningTaskRef> result = new ArrayList<RunningTaskRef>();
         for( RunningTaskRef ref : getRunningTasks() ) {
            if( filter.accept( ref ) ) {
               result.add( ref );
            }
         }
         return result;
      }

      @Override
      @SuppressWarnings("unchecked")
      public <T> Future<T> submitTask( ResumableTask task ) {
         final ExecutionService service = new ExecutionService( channel );
         return service.<T> submit( ( Callable<T> )task );
      }

      @Override
      @SuppressWarnings("unchecked")
      public <T> Future<T> submitTask( SimpleTask task ) {
         final ExecutionService service = new ExecutionService( channel );
         return service.submit( ( Callable<T> )task );
      }

      private RequestHandlerByRequestClass createCancelRunningTaskHandler() {
         return new RequestHandlerByRequestClass( RequestCancelRunningTaskMessage.class, handleCancelRunningTaskRequest() );
      }

      private RequestHandler handleCancelRunningTaskRequest() {
         return new RequestHandler() {
            @Override
            public Object handle( Message msg ) throws Exception {
               final RequestCancelRunningTaskMessage message = ( RequestCancelRunningTaskMessage )msg.getObject();
               Boolean result = Boolean.FALSE;
               for( Runnable runnable : runner.getCurrentRunningTasks().values() ) {
                  if( runnable != null && runnable instanceof DistributedFuture ) {
                     final AbstractDistributedTask task = getRunningTask( ( DistributedFuture<?> )runnable );
                     if( message.getId().equalsIgnoreCase( task.getId() ) ) {
                        final Future<?> future = ( Future<?> )runnable;
                        result = future.cancel( true );
                        break;
                     }
                  }
               }
               return result;
            }
         };
      }

      private RequestHandlerByRequestClass createClusterMemberMetaHandler() {
         return new RequestHandlerByRequestClass( RequestClusterMemberMetaMessage.class, handleClusterMemberMetaRequest() );
      }

      private RequestHandler handleClusterMemberMetaRequest() {
         return new RequestHandler() {
            @Override
            public Object handle( Message msg ) throws Exception {
               return new MemberMeta()
                     .setInstanceAddress( channel.getAddress().toString() )
                     .setCpuFrequency( new SystemMeta().getCpuFrequency() )
                     .setExecutionThreads( configuration.getExecutionThreadCount() )
                     .setNumberOfProcessors( new SystemMeta().getNumberOfProcessors() );
            }
         };
      }

      private LockNotification createLockListener() {
         return new LockNotification() {
            @Override
            public void awaited( String lock_name, Owner owner ) {
               LOG.info( "awaited \"{}\" by {}", lock_name, owner );
            }

            @Override
            public void awaiting( String lock_name, Owner owner ) {
               LOG.info( "awaiting \"{}\" by {}", lock_name, owner );
            }

            @Override
            public void lockCreated( String name ) {
               LOG.info( "\"{}\" lock created", name );
            }

            @Override
            public void lockDeleted( String name ) {
               LOG.info( "\"{}\" lock deleted", name );
            }

            @Override
            public void locked( String lock_name, Owner owner ) {
               LOG.info( "\"{}\" locked by {}", lock_name, owner );
            }

            @Override
            public void unlocked( String lock_name, Owner owner ) {
               LOG.info( "\"{}\" unlocked by {}", lock_name, owner );
            }
         };
      }

      private RequestHandler requestHandler() {
         return new RequestHandler() {
            @Override
            public Object handle( Message msg ) throws Exception {
               Object result = null;
               boolean handlerFound = false;
               for( RequestHandlerByRequestClass byClass : requestHandlers ) {
                  if( byClass.getRequestClass().equals( msg.getObject().getClass() ) ) {
                     result = byClass.getHandler().handle( msg );
                     handlerFound = true;
                     break;
                  }
               }
               if( !handlerFound ) {
                  throw new RuntimeException( String.format( "No handler method could be found for message: %s", msg ) );
               }
               return result;
            }
         };
      }

      private RequestHandlerByRequestClass createRunningTasksHandler() {
         return new RequestHandlerByRequestClass( RequestRunningTasksMessage.class, handleRunningTasksRequest() );
      }

      private RequestHandler handleRunningTasksRequest() {
         return new RequestHandler() {
            @Override
            public Object handle( Message msg ) throws Exception {
               final List<RunningTaskRef> result = new ArrayList<RunningTaskRef>();
               for( Thread thread : runner.getCurrentRunningTasks().keySet() ) {
                  final Runnable runnable = runner.getCurrentRunningTasks().get( thread );
                  if( runnable != null && runnable instanceof DistributedFuture ) {
                     final AbstractDistributedTask task = getRunningTask( ( DistributedFuture<?> )runnable );
                     result.add( new RunningTaskRef()
                           .setId( task.getId() )
                           .setTaskClass( task.getClass() )
                           .setInstanceAddress( channel.getAddressAsString() )
                           .setThreadName( thread.getName() )
                           );
                  }
               }
               return result;
            }
         };
      }

      private Address getAddress( String str ) {
         Address result = null;
         for( Address member : channel.getView().getMembers() ) {
            if( str.equals( member.toString() ) ) {
               result = member;
               break;
            }
         }
         return result;
      }

      private AbstractDistributedTask getRunningTask( DistributedFuture<?> future ) {
         AbstractDistributedTask result = null;
         final Callable<?> callable = future.getCallable();
         if( callable instanceof AbstractDistributedTask ) {
            result = ( AbstractDistributedTask )callable;
         }
         else if( new Field<Object>( callable, "task" ).exists() ) {
            final Object task = new Field<Object>( callable, "task" ).get();
            if( task instanceof AbstractDistributedTask ) {
               result = ( AbstractDistributedTask )task;
            }
         }
         return result;
      }

      private void init() {
         if( channel == null ) {
            throw new RuntimeException( String.format( "Cannot create Distributed Task System without JGroups channel being set" ) );
         }
         runner = new ExecutionRunner( channel );
         dispatcher = new MessageDispatcher( channel, null, null, requestHandler() );
         lockService = new LockService( channel );
         lockService.addLockListener( createLockListener() );
         final ExecutorService service = Executors.newFixedThreadPool( configuration.getExecutionThreadCount() );
         for( int i = 0; i < configuration.getExecutionThreadCount(); ++i ) {
            service.submit( runner );
         }
         requestHandlers.add( createClusterMemberMetaHandler() );
         requestHandlers.add( createRunningTasksHandler() );
         requestHandlers.add( createCancelRunningTaskHandler() );
      }

      private <T> RspList<T> sendMessageToAll( Object message ) {
         try {
            final RequestOptions opts = new RequestOptions( ResponseMode.GET_ALL, 0 );
            final RspList<T> result = dispatcher.castMessage( null,
                  new Message( null, null, message ),
                  opts );
            validateSendToAllResponse( result );
            return result;
         }
         catch( Exception exception ) {
            throw new RuntimeException( "Error sending message: " + message + " to cluster", exception );
         }
      }

      private <T> RspList<T> sendMessageToSingle( String address, Object message ) {
         final Address address2 = getAddress( address );
         if( address2 == null ) {
            throw new RuntimeException( String.format( "No member could be found for address: %s", address ) );
         }
         try {
            final RequestOptions opts = new RequestOptions( ResponseMode.GET_ALL, 10000 );
            return dispatcher.castMessage( null, new Message( address2, null, message ), opts );
         }
         catch( Exception exception ) {
            throw new RuntimeException( "Error sending message: " + message + " to cluster", exception );
         }
      }

      private void validateSendToAllResponse( RspList<?> responses ) {
         LOG.info( "Responses: {}", responses );
         boolean receivedFlag = true;
         for( Address member : channel.getView().getMembers() ) {
            receivedFlag = receivedFlag && responses.isReceived( member );
         }
         if( !receivedFlag ) {
            throw new RuntimeException( "Not all members on the cluster were able to receive the request." );
         }
         for( Address member : channel.getView().getMembers() ) {
            final Rsp<?> response = responses.get( member );
            if( response != null ) {
               if( response.hasException() ) {
                  throw new RuntimeException( String.format( "Member: %s failed to handle message.", response.getSender() ), response.getException() );
               }
            }
         }
      }
   }

   boolean cancelRunningTask( String instanceAddress, String id );

   JChannel getChannel();

   ClusterMeta getClusterMeta();

   DistributedTaskSystemConfiguration getConfiguration();

   Lock getLock( String lockName );

   RunningTaskRef getRunningTask( String instanceAddress, String id );

   List<RunningTaskRef> getRunningTasks();

   List<RunningTaskRef> getRunningTasks( Class<?> taskClass );

   List<RunningTaskRef> getRunningTasks( Filter<RunningTaskRef> filter );

   <T> Future<T> submitTask( ResumableTask task );

   <T> Future<T> submitTask( SimpleTask task );
}
