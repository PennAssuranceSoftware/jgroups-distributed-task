package com.pennassurancesoftware.jgroups.distributed_task.util;

public class Field<T> {
   private final Object obj;
   private final String fieldName;

   public Object getObj() {
      return obj;
   }

   public String getFieldName() {
      return fieldName;
   }

   public Field( Object obj, String fieldName ) {
      this.obj = obj;
      this.fieldName = fieldName;
      if( obj == null ) {
         throw new RuntimeException( "Must specify object to get field from" );
      }
      if( fieldName == null || "".equals( fieldName ) ) {
         throw new RuntimeException( "Must specify a field name" );
      }
   }

   public boolean exists() {
      return findField() != null;
   }

   @SuppressWarnings("rawtypes")
   private java.lang.reflect.Field findField() {
      Class currentClazz = obj.getClass();
      java.lang.reflect.Field result = null;
      while( result == null ) {
         try {
            result = currentClazz.getDeclaredField( fieldName );
            result.setAccessible( true );
         }
         catch( NoSuchFieldException exception ) {
            if( currentClazz == Object.class ) {
               throw new RuntimeException( exception );
            }
            currentClazz = currentClazz.getSuperclass();
         }
      }
      return result;
   }

   @SuppressWarnings("unchecked")
   public T get() {
      try {
         T result = null;
         if( exists() ) {
            result = ( T )findField().get( obj );
         }
         return result;
      }
      catch( Exception exception ) {
         throw new RuntimeException( exception );
      }
   }
}
