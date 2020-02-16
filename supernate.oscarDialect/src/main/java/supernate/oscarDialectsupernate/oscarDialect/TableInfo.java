package supernate.oscarDialectsupernate.oscarDialect;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import supernate.core.Entity;
import supernate.core.tags.Column;


public class TableInfo extends supernate.core.TableInfo{
	protected TableInfo initWithClass(Class z) {
		return null;
	}
	
	protected TableInfo initWithDatabase(Entity et,int id) {
		return null;
		
	}
	
	protected List<String> getInitSql(boolean createhis){
		return null;
	}
	
	protected ColumnInfo getColumnInfo(Field field) {
		ColumnInfo ret = null;
		if(field.isAnnotationPresent(Column.class)) {
			Column columntemp = (Column) field.getAnnotation(Column.class);
			ret = new ColumnInfo(columntemp);
			ret.setFieldName(field.getName());
			if(columntemp.name().equals("")){
				ret.setName(field.getName());
			}
			if(columntemp.type()==-1) {
				Type t = field.getGenericType();				
				switch(t.getTypeName().toLowerCase()) {
				    case "int":
				    case "java.lang.integer":
				    	ret.setType(0);
					  if(ret.getLength()==null||ret.getLength().equals("255"))
						  ret.setLength("10");
					  break;
				    case "byte":
				    	ret.setType(7);
				    	break;
				    case "float":
				    case "java.lang.number":
				    case "double":
				    	ret.setType(2);
						  if(ret.getLength()==null||ret.getLength().equals("255"))
							  ret.setLength("8,2");
				      break;
				    case "java.math.bigdecimal":
				    	ret.setType(1);
						  if(ret.getLength()==null||ret.getLength().equals("255"))
							  ret.setLength("8,2");
				    	break;
				    case "java.sql.timestamp":
				    	ret.setType(5);
						  if(ret.getLength()==null||ret.getLength().equals("255"))
							  ret.setLength("");
				    	break;
				    case "long":
				    case "java.lang.long":
				    	ret.setType(6);
						  if(ret.getLength()==null||ret.getLength().equals("255"))
							  ret.setLength("8,2");
				    	break;
				    case "java.lang.string":
				    default:
				    	ret.setType(3);
				    	break;
				}
			}
		}
		return ret;
	}
	
	protected Object getKeyValue(Entity en) {
		List<Map<String,Object>> temp = en.QueryToMap("select "+this.getTableName()+"_SEQUENCE.nextval as key from dual");
		return temp.get(0).get("key");
	}
}
