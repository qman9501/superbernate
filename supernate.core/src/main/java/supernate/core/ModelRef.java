package supernate.core;
/**
 * 
 */


import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 *
 */
public class ModelRef {
	
	public static boolean StringCheck(Object t) {
		if(t==null) {
			return false;
		}
		String a = t.toString();
		if(a.equals("")) {
			return false;
		}
		return true;
	}
	
	public static void CopyProperties(Object source,Object desi,String... notcopy) {
		Class a = source.getClass();
		Class b = desi.getClass();
		List<Field> fields = getDeclaredFields(source);
		for(Field fi :fields) {
			if(notcopy!=null) {
				for(String e:notcopy) {
					if(notcopy.equals(fi.getName()))
						continue;
				}
			}
			try {
				setFieldValue(desi,fi.getName(),getFieldValue(source,fi.getName()));
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	    /** 
	     * 循环向上转型, 获     * @param object : 子类对象 
	     * @param methodName : 父类中的方法名 
	     * @param parameterTypes : 父类中的方法参数类型 
	     * @return 父类中的方法对象 
	     */  
	      
	    public static Method getDeclaredMethod(Object object, String methodName, Class<?> ... parameterTypes){  
	        Method method = null ;  
	          
	        for(Class<?> clazz = object.getClass() ; clazz != Object.class ; clazz = clazz.getSuperclass()) {  
	            try {  
	                method = clazz.getDeclaredMethod(methodName, parameterTypes) ;  
	                return method ;  
	            } catch (Exception e) {  
	                //这里甚么都不能抛出去。  
	                //如果这里的异常打印或者往外抛，则就不会进入              
	            }  
	        }  
	          
	        return null;  
	    }  
	      
	    /** 
	     * 直接调用对象方法, 而忽略修饰符(private, protected, default) 
	     * @param object : 子类对象 
	     * @param methodName : 父类中的方法名 
	     * @param parameterTypes : 父类中的方法参数类型 
	     * @param parameters : 父类中的方法参数 
	     * @return 父类中方法的执行结果 
	     */  
	      
	    public static Object invokeMethod(Object object, String methodName, Class<?> [] parameterTypes,  
	            Object [] parameters) {  
	        //根据 对象、方法名和对应的方法参数 通过取 Method 对象  
	        Method method = getDeclaredMethod(object, methodName, parameterTypes) ;  
	          
	        //抑制Java对方法进行检查,主要是针对私有方法而言  
	        method.setAccessible(true) ;  
	          
	            try {  
	                if(null != method) {  
	                      
	                    //调用object 的 method 所代表的方法，其方法的参数是 parameters  
	                    return method.invoke(object, parameters) ;  
	                }  
	            } catch (IllegalArgumentException e) {  
	                e.printStackTrace();  
	            } catch (IllegalAccessException e) {  
	                e.printStackTrace();  
	            } catch (InvocationTargetException e) {  
	                e.printStackTrace();  
	            }  
	          
	        return null;  
	    }  
	  
	    /** 
	     * 循环向上转型, 获     * @param object : 子类对象 
	     * @param fieldName : 父类中     * @return 父类中     */  
	      
	    public static Field getDeclaredField(Object object, String fieldName){  
	        Field field = null ;  
	          
	        Class<?> clazz = object.getClass() ;  
	          
	        for(; clazz != Object.class ; clazz = clazz.getSuperclass()) {  
	            try {  
	                field = clazz.getDeclaredField(fieldName) ;  
	                return field ;  
	            } catch (Exception e) {  
	                //这里甚么都不能抛出去。  
	                //如果这里的异常打印或者往外抛，则就不会进入                  
	            }   
	        }  
	      
	        return null;  
	    }
	    
	    public static List<Field> getDeclaredFields(Object object){  
	        List<Field> field = new ArrayList<Field>() ;  
	          
	        Class<?> clazz = object.getClass() ;  
	          
	        for(; clazz != Object.class ; clazz = clazz.getSuperclass()) {  
	            try {  
	            	for(Field f : clazz.getDeclaredFields())
	            	if(!field.contains(f)) {
	            		field.add(f);
	            	}
	                //return field ;  
	            } catch (Exception e) {  
	                //这里甚么都不能抛出去。  
	                //如果这里的异常打印或者往外抛，则就不会进入                  
	            }   
	        }  
	      
	        return field;  
	    }
	      
	    /** 
	     * 直接设置对象属性值, 忽略 private/protected 修饰符, 也     * @param object : 子类对象 
	     * @param fieldName : 父类中     * @param value : 将要设置的值 
	     */  
	      
	    public static void setFieldValue(Object object, String fieldName, Object value){  
	      
	          
	        try {  
		        //根据 对象和属性名通过取 Field对象  
		        Field field = getDeclaredField(object, fieldName) ;  
		        if((field.getModifiers()&9)>0)
		        	return;
		        //抑制Java对其的检查  
		        field.setAccessible(true) ; 
		        String typename = field.getType().getName().toLowerCase();
		        Object temp = paseType(typename,value);		        
	            //将 object 中 field 所代表的值 设置为 value  
	            field.set(object, temp) ;  
	        } catch (IllegalArgumentException e) {  
	        	e.printStackTrace();
	            //e.printStackTrace();  
	        } catch (IllegalAccessException e) {
	        	e.printStackTrace();  
	            //e.printStackTrace();  
	        }  
	          
	    }  
	    
	    public static Object paseType(String type,Object value) {
	    	if(value==null)
	    		return null;
	    	Object ret = null;
	    	if(value.getClass().getSimpleName().toLowerCase().equals(type.toLowerCase())) {
	    		return value;
	    	}
	    	if(type.toLowerCase().contains("int")) {
	    		if(value.toString().equals(""))
	    			return null;
	    		return Integer.parseInt(value.toString());
	    	}
	    	if(type.toLowerCase().contains("long")) {
	    		if(value.toString().equals(""))
	    			return null;
	    		return Long.parseLong(value.toString());
	    	}
	    	if(type.toLowerCase().equals("byte")) {
	    		if(value.toString().equals(""))
	    			return null;
	    		return Byte.parseByte(value.toString());
	    	}
	    	if(type.toLowerCase().equals("date")) {
	    		if(value.toString().equals(""))
	    			return null;
	    		return new Date(value.toString());
	    	}
	    	return value;
	    }
	      
	    /** 
	     * 直接读的属性值, 忽略 private/protected 修饰符, 也     * @param object : 子类对象 
	     * @param fieldName : 父类中     * @return : 父类中     */  
	      
	    public static Object getFieldValue(Object object, String fieldName){  
	          
	        //根据 对象和属性名通过取 Field对象  
	        Field field = getDeclaredField(object, fieldName) ;  
	          
	        //抑制Java对其的检查  
	        field.setAccessible(true) ;  
	          
	        try {  
	            //获的属性值  
	            return field.get(object) ;  
	              
	        } catch(Exception e) {  
	            e.printStackTrace() ;  
	        }  
	          
	        return null;  
	    }  
	    
	    public static Timestamp getTimestamp(Object t) {
	    	if(t==null)
	    		return null;
	    	if(t.getClass().getSimpleName().toLowerCase().contains("string")) {
	    		String v = t.toString();
	    		if(v.equals("")) {
	    			return null;
	    		}
	    		if(v.indexOf(" ")>0) {
					Timestamp ts = Timestamp.valueOf(v);
					return ts;
				}else {
					Timestamp ts = Timestamp.valueOf(v+" 00:00:00");
					return ts;
				}
	    	}else {
				if(t.getClass().getSimpleName().toLowerCase().contains("date")) {
					Date t1 = (Date)t;
					Timestamp ts = new Timestamp(t1.getTime());
					return ts;
				}else {
					return (Timestamp)t;
				}
	    	}
	    }
}
