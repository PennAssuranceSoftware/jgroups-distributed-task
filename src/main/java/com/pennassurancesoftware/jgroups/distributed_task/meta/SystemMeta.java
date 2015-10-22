package com.pennassurancesoftware.jgroups.distributed_task.meta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.Processor;

public class SystemMeta {
   private static final Logger LOG = LoggerFactory.getLogger( SystemMeta.class );

   /** @return Frequency of CPU processor in GHz */
   public double getCpuFrequency() {
      double result = 2;
      try {
         SystemInfo si = new SystemInfo();
         HardwareAbstractionLayer hal = si.getHardware();
         final Processor cpu = hal.getProcessors()[0];
         final double frequency = Double.parseDouble( cpu.toString().split( "@" )[1].replace( "GHz", "" ) );
         result = frequency;
      }
      catch( Exception exception ) {
         LOG.warn( "Error trying to figure out the frequency of processors", exception );
      }
      return result;
   }

   public int getNumberOfProcessors() {
      return Runtime.getRuntime().availableProcessors();
   }
}
