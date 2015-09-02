package com.github.xsavikx.websitemonitor.jndi;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

import com.github.xsavikx.websitemonitor.db.DatabaseRoutine;

public class DatabaseRoutineFactory implements ObjectFactory {
  private static final String SOURCE_ATTRIBUTE_NAME = "source";
  private static final String VALUE_ATTRIBUTE_NAME = "value";
  private static final String OVERRIDE_ATTRIBUTE_NAME = "override";

  private static final String DATABASE_ROUTINE_IS_ON_VALUE = "on";

  @Override
  public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception {
    Reference ref = (Reference) obj;
    Enumeration<RefAddr> addrs = ref.getAll();
    DatabaseRoutine routine = null;
    String source = null;
    boolean isOn = false;
    boolean override = false;
    while (addrs.hasMoreElements()) {
      RefAddr addr = addrs.nextElement();
      String attributeName = addr.getType();
      if (SOURCE_ATTRIBUTE_NAME.equalsIgnoreCase(attributeName)) {
        source = (String) addr.getContent();
      } else if (VALUE_ATTRIBUTE_NAME.equalsIgnoreCase(attributeName)) {
        isOn = DATABASE_ROUTINE_IS_ON_VALUE.equalsIgnoreCase((String) addr.getContent()) == true ? true : false;
      } else if (OVERRIDE_ATTRIBUTE_NAME.equalsIgnoreCase(attributeName)) {
        override = Boolean.parseBoolean((String) addr.getContent());
      }
    }
    routine = DatabaseRoutine.getBySource(source);
    routine.setOn(isOn);
    routine.setOverride(override);
    return routine;
  }
}
