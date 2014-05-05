/**
 * 
 */
package com.oaktree.core.jmx;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oaktree.core.container.INameable;
import com.oaktree.core.logging.Log;


/**
 * Bean registration helpers for JMX so you dont have to implement any annoying interfaces or
 * name your bean in any particular way. Just throw your bean or nameable and it will register.
 * 
 * How to use:
 * 	MBeanManager.register("MyCategory","Subcategory",myobject);
 * 
 * @author Oaktree Designs Ltd
 * 
 */
public class MBeanManager {

	
	private static final Logger logger = LoggerFactory.getLogger(MBeanManager.class.getName());

	public static class JMXWrapperBean implements DynamicMBean {

		public JMXWrapperBean(Object o) {
			object = o;
		}
		private Object object;
		
		@Override
		public Object getAttribute(String attr)
				throws AttributeNotFoundException, MBeanException,
				ReflectionException {
			try {
				Class c = object.getClass();
				Field f = null;
				try {f = c.getDeclaredField(attr); } catch (Exception e) {logger.error(e.getMessage());}
				while (f == null && c.getSuperclass() != null && !c.getSuperclass().equals(Object.class)) {
					c = c.getSuperclass();
					try {f = c.getDeclaredField(attr); } catch (Exception e) {logger.error(e.getMessage());}
				}
				if (f == null) {
					return null;
				}
				f.setAccessible(true);
				return f.get(object);
			} catch (Exception e) {
				Log.exception(logger,e);
			}
			return null;
		}

		@Override
		public AttributeList getAttributes(String[] attrs) {
			AttributeList l = new AttributeList();
			for (String attr : attrs) {
				try {
					l.add( new Attribute(attr,this.getAttribute(attr)));
				} catch (Exception e) {
					Log.exception(logger,e);
				}
			}
			return l;
		}

		public List<MBeanAttributeInfo> getFieldInfo(Class c) {
			Field[] fields = c.getDeclaredFields();
			List<MBeanAttributeInfo> attrinfo = new ArrayList<MBeanAttributeInfo>();
			int i = 0;
			for (Field field:fields) {
				field.setAccessible(true);
				Method getter = null;
				Method setter = null;
				String n = field.getName();
				char u = Character.toUpperCase(n.charAt(0));
				n = u + n.substring(1);
				try {
					try {getter = c.getDeclaredMethod("get" + n);} catch (Exception e){logger.warn(e.getMessage());}
					if (getter == null) {
						try {
							getter = c.getDeclaredMethod("is" + n);
						} catch (Exception e) {logger.warn(e.getMessage());}
					}
					if (getter != null) {
						getter.setAccessible(true);
					}
				} catch (Exception e) {logger.warn(e.getMessage());}
				try {
					setter = c.getDeclaredMethod("set"+n,getter != null ? getter.getReturnType() : String.class);
					if (setter != null) {
						setter.setAccessible(true);
					}
					
				} catch (Exception e) {logger.warn(e.getMessage());}
				try {attrinfo.add(new MBeanAttributeInfo(field.getName(),field.getName(),getter,setter));
				} catch (Exception e) {logger.warn(e.getMessage());}
				i++;
			}
			return attrinfo;
		}
		
		@Override
		public MBeanInfo getMBeanInfo() {
			MBeanInfo info = null;
			try {
				List<MBeanAttributeInfo> attrinfo = new ArrayList<MBeanAttributeInfo>();
				Class c = object.getClass();
				attrinfo.addAll(getFieldInfo(c));
				while (c.getSuperclass() != null && !c.getSuperclass().equals(Object.class)) {
					c = c.getSuperclass();
					attrinfo.addAll(getFieldInfo(c));
				}
				c = object.getClass();
				
				MBeanConstructorInfo[] coninfo = new MBeanConstructorInfo[]{new MBeanConstructorInfo("default", object.getClass().getConstructor())};
				Method[] m = c.getDeclaredMethods();
				List<MBeanOperationInfo> opinfo = new ArrayList<MBeanOperationInfo>();
				for (Method method:m) {
					opinfo.add(new MBeanOperationInfo("",method));
				}
				while (c.getSuperclass() != null && !c.getSuperclass().equals(Object.class)) {
					c = c.getSuperclass();
					m = c.getDeclaredMethods();
					for (Method method:m) {
						opinfo.add(new MBeanOperationInfo("",method));
					}
				}
				c = object.getClass();
				
				
				MBeanNotificationInfo[] notinfo = new MBeanNotificationInfo[]{};
				info = new MBeanInfo(object.getClass().getSimpleName(),object.getClass().getName(),attrinfo.toArray(new MBeanAttributeInfo[attrinfo.size()]),coninfo,opinfo.toArray(new MBeanOperationInfo[opinfo.size()]),notinfo);
			} catch (Throwable e) {
				Log.exception(logger,e);
			}
			return info;
		}

		@Override
		public Object invoke(String actionName, Object params[], String signature[])
				throws MBeanException, ReflectionException {
			try {
				Class[] ps = new Class[params.length];
				int i =0;
				for (Object o:params) {
					try {
						ps[i] = Class.forName(signature[i]);
					} catch (Exception e) {
						if (signature[i].equals("boolean")) {
							ps[i] = Boolean.TYPE;
						}
						if (signature[i].equals("int")) {
							ps[i] = Integer.TYPE;
						}
						if (signature[i].equals("double")) {
							ps[i] = Double.TYPE;
						}
						if (signature[i].equals("char")) {
							ps[i] = Character.TYPE;
						}
						if (signature[i].equals("short")) {
							ps[i] = Short.TYPE;
						}
						if (signature[i].equals("byte")) {
							ps[i] = Byte.TYPE;
						}
					}
					i++;
				}
				Class c = this.object.getClass();
				Method m = null;
				try { m = c.getDeclaredMethod(actionName,ps);} catch (Exception e) {};
				while (m == null && c.getSuperclass() != null && !c.getSuperclass().equals(Object.class)) {
					c = c.getSuperclass();
					try { m = c.getDeclaredMethod(actionName,ps);} catch (Exception e) {};
				}
				if (m != null) {
					return m.invoke(object,params);
				}
			} catch (Exception e) {				
				Log.exception(logger,e);
			} //mm no param types
			return null;
		}

		@Override
		public void setAttribute(Attribute attr)
				throws AttributeNotFoundException,
				InvalidAttributeValueException, MBeanException,
				ReflectionException {
			Class c = this.object.getClass();
			Field m = null;
			try { m = c.getDeclaredField(attr.getName());} catch (Exception e) {logger.error(e.getMessage());};
			while (m == null && c.getSuperclass() != null && !c.getSuperclass().equals(Object.class)) {
				c = c.getSuperclass();
				try { m = c.getDeclaredField(attr.getName());} catch (Exception e) {logger.error(e.getMessage());};
			}

			try {
				if (m != null) {
					m.setAccessible(true);
					try {
						m.set(object, attr.getValue());
					} catch (Exception e) {
						Log.exception(logger,e);
						throw new MBeanException(e);
					}

				} else {
					logger.warn("No method to set attr " + attr.getName());
				}
			} catch (Exception e) {
				Log.exception(logger,e);
				throw new AttributeNotFoundException();
			}
			
		}

		@Override
		public AttributeList setAttributes(AttributeList l) {
			for (Object o:l) {
				Attribute a = (Attribute)(o);
				try {
					this.setAttribute(a);					
				} catch (Exception e) {
					Log.exception(logger,e);
				}
			}
			return l;
		}
		
	}
	

	
	public static void register(String type,INameable object) {
		register(object.getClass().getName(),type,object);
	}
	
	/**
	 * Register an object.
	 * 
	 * @param object
	 */
	public static void register(String category,String type,INameable object ) {
		register(category,type,object.getName(),object);
	}
	
	/**
	 * Register any old object.
	 * @param category
	 * @param type
	 * @param objectName
	 * @param object
	 */
	public static void register(String category, String type, String objectName, Object object ) {
		MBeanServer mbs =ManagementFactory.getPlatformMBeanServer();
		ObjectName name;
		try {
			name = new ObjectName(category + ":type="
					+ type + ",name=" + objectName);
			mbs.registerMBean(new JMXWrapperBean(object), name);
			logger.info("Registered bean as " + objectName);
		} catch (Exception e) {
			logger.warn(
					"Object " + objectName
							+ " cannot be registered as MBean: " + e.toString());
		}
	}
	
	/**
	 * Unregister a bean identifed with its class and its name.
	 * @param object
	 */
	public static void unregister(String className,String objectName) {
		MBeanServer mbs =ManagementFactory.getPlatformMBeanServer();


		ObjectName name;
		try {
			name = new ObjectName(className + ":type="
					+ objectName);
			mbs.unregisterMBean(name);
			logger.info("Registered bean as " + objectName);
		} catch (Exception e) {
			logger.warn(
					"Object " + objectName
							+ " cannot be registered as MBean: " + e.toString());
		}
	}
		
}
