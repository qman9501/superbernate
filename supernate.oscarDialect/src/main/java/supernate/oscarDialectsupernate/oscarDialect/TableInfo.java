package supernate.oscarDialectsupernate.oscarDialect;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import supernate.core.Entity;
import supernate.core.tags.AutoGen;
import supernate.core.tags.Column;
import supernate.core.tags.ConnectionInfo;
import supernate.core.tags.Key;
import supernate.core.tags.Table;


public class TableInfo extends supernate.core.TableInfo{
	@SuppressWarnings("unchecked")
	protected TableInfo initWithClass(Class z) {
		TableInfo ret = null;
		//this.setTableName(z.getSimpleName());
		if(z.isAnnotationPresent(ConnectionInfo.class)) {
			ConnectionInfo connectionInfo = (ConnectionInfo)z.getAnnotation(ConnectionInfo.class);
			supernate.core.ConnectionInfo info = new supernate.core.ConnectionInfo();
			info.setDriver(connectionInfo.driver());
			info.setPassword(connectionInfo.password());
			info.setUrl(connectionInfo.url());
			info.setUsername(connectionInfo.username());
			ret.setConnectionInfo(info);
		}
		Class<?> clazz = z ; 
		for(; clazz != Object.class ; clazz = clazz.getSuperclass()) {  
            try {  
        		if(clazz.isAnnotationPresent(Table.class)) {
        			Table tableinfo = (Table) clazz.getAnnotation(Table.class);
        			if(!tableinfo.name().equals("")) {
        				ret.setTableName(tableinfo.name());
        			}else {
        				ret.setTableName(clazz.getSimpleName());
        			}
        			ret.setComment(tableinfo.comment());
        		}
            } catch (Exception e) {  
                //这里甚么都不能抛出去。  
                //如果这里的异常打印或者往外抛，则就不会进入                  
            }   
        }  
		if(ret.getTableName().equals("")) {
			logger.error("there's no table tag,please add it to class tag");
		}
		ret.initcolumns(z);
		return ret;
	}
	
	protected TableInfo initWithDatabase(Entity et,int id) {
		TableInfo ret = null;
		try {
			Entity<TableInfo> ti = new Entity<TableInfo>(et.getConn());
			ret = ti.getByClassKey(TableInfo.class,id);
			Entity<ColumnInfo> cos = new Entity<ColumnInfo>(et.getConn());
			List<ColumnInfo> colist = cos.getObjects(ColumnInfo.class,"tablename=?", ret.getTableName());
			for(int i = 0;i<colist.size();i++) {
				ColumnInfo co = colist.get(i);
				columns.add(co);
				if(co.isAi()) {
					autos.add(co);
				}
				if(co.isKey()) {
					keyColumn = co;
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return ret;
	}
	
	protected List<String> getInitSql(boolean createhis){
		List<String> ret = new ArrayList<String>();
		List<String> indexs = new ArrayList<String>();
		List<String> hisindes = new ArrayList<String>();
		List<String> autosf = new ArrayList<String>();
		String temp1="CREATE TABLE "+this.tableName.toUpperCase()+"(",temp2="CREATE TABLE "+this.tableName.toUpperCase()+"_HIS(";
		for(int i = 0;i<columns.size();i++) {
			supernate.core.ColumnInfo co = columns.get(i);
			temp1+=co.getSqlString(false)+",\r\n";
			autosf.add(co.getAutoFunctioin());
			temp2+=co.getSqlString(true)+",\r\n";
			indexs.add(co.getIndexSql(false));
			hisindes.add(co.getIndexSql(true));
		}
		ret.add(temp1+")");
		ret.addAll(indexs);
		if(createhis) {
			ret.add(temp2+")");
			ret.addAll(hisindes);
			ret.add(getHisFunction());
		}
		ret.addAll(autosf);
		return ret;
	}
	
	protected void initColumnInfo(Field field) {
		ColumnInfo ret = null;
		if(field.isAnnotationPresent(Column.class)) {
			Column columntemp = (Column) field.getAnnotation(Column.class);
			ret = new ColumnInfo(columntemp);
			ret.setFieldName(field.getName());
			if(columntemp.name().equals("")){
				ret.setName(field.getName());
				if(this.columns.indexOf(ret)<0) {
					this.columns.add(ret);
				}else {
					return;
				}
			}
			ret.setTableName(tableName);
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
		if(field.isAnnotationPresent(Key.class)){
			if(this.keyColumn==null)
				this.keyColumn = ret;
		}
		if(field.isAnnotationPresent(AutoGen.class)){
			ret.setAi(1);
			if(this.autos.indexOf(ret)<0)
			this.autos.add(ret);
		}
	}
	
	protected Object getKeyValue(Entity en) {
		return this.keyColumn.getAutoValue(en);
	}

	@Override
	protected String getHisFunction() {
		String ret = "";
		// TODO Auto-generated method stubString trigerupdate = "CREATE OR REPLACE TRIGGER \"TRI_"+this.getTableName()+"_UP\"\r\n" + 
		String trigerupdate = "";
		String columnnames = "";
		String hiscolumnnames = "";
		String hisdcolumnnames = "";
		for(int i = 0;i<columns.size();i++) {
			supernate.core.ColumnInfo co = columns.get(i);
			if(!columnnames.equals("")) {
				columnnames+=",";
				hiscolumnnames+=",";
				hisdcolumnnames+=",";
			}
			columnnames+=co.getName().toUpperCase();
			hiscolumnnames+=":NEW.\""+co.getName().toUpperCase()+"\"";
			hisdcolumnnames+=":OLD.\""+co.getName().toUpperCase()+"\"";
		}
		trigerupdate += "BEFORE UPDATE\r\n"  +
		"ON "+this.getTableName()+" FOR EACH ROW\r\n" + 
		"BEGIN\r\n" + 
		"	INSERT into "+this.getTableName()+"_HIS ("+columnnames+",LOGDATE,LOGTYPE) values("+hiscolumnnames+",sysdate,1);\r\n" + 
		"END;;\r\n";
		ret+=trigerupdate;
        String trigerinsert = "CREATE OR REPLACE TRIGGER \"TRI_"+this.getTableName()+"_INS\"\r\n" + 
		"BEFORE INSERT\r\n" + 
		"ON "+this.getTableName()+" FOR EACH ROW\r\n" + 
		"BEGIN\r\n" + 
		"	INSERT into "+this.getTableName()+"_HIS ("+columnnames+",LOGDATE,LOGTYPE) values("+hiscolumnnames+",sysdate,0);\r\n" + 
		"END;;\r\n";
		ret+=trigerinsert;
        String trigerindel = "CREATE OR REPLACE TRIGGER \"TRI_"+this.getTableName()+"_DEL\"\r\n" + 
		"BEFORE DELETE\r\n" + 
		"ON "+this.getTableName().toUpperCase()+" FOR EACH ROW\r\n" + 
		"BEGIN\r\n" + 
		"	INSERT into "+this.getTableName().toUpperCase()+"_HIS ("+columnnames+",LOGDATE,LOGTYPE) values("+hisdcolumnnames+",sysdate,2);\r\n" + 
		"END;;\r\n";
		ret+=trigerindel;
		return ret;
	}
}
