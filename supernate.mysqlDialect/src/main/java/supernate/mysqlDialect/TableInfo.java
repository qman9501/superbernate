package supernate.mysqlDialect;

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
	
	protected List<String> getInitSql(boolean createhis){
		List<String> ret = new ArrayList<String>();
		List<String> indexs = new ArrayList<String>();
		List<String> hisindes = new ArrayList<String>();
		List<String> autosf = new ArrayList<String>();
		List<String> columnStr = new ArrayList<String>();
		String temp1="CREATE TABLE "+this.tableName.toUpperCase()+"(",temp2="CREATE TABLE "+this.tableName.toUpperCase()+"_HIS(";
		for(int i = 0;i<columns.size();i++) {
			supernate.core.ColumnInfo co = columns.get(i);
			String is = co.getIndexSql(false),his=co.getIndexSql(true);
			List<String> auf = co.getAutoSqls();
			if(auf!=null)
			autosf.addAll(auf);
			if(!is.equals(""))
			indexs.add(is);
			if(!his.equals(""))
			hisindes.add(his);
			columnStr.add(co.toString());
			if(i>0) {
				temp1+=",\n";
				temp2+=",\n";
			}
			temp1+=co.getSqlString(false);
			temp2+=co.getSqlString(true);
		}
		if(this.keyColumn!=null) {
			temp1+=",PRIMARY KEY ("+this.keyColumn.getName()+")";
		}
		ret.add(temp1+");");
		if(indexs.size()>0)
		ret.addAll(indexs);
		if(createhis) {
			temp2+=",LOGDATE datetime,\nLOGTYPE int(1)";
			ret.add(temp2+");");
			if(hisindes.size()>0)
				ret.addAll(hisindes);
			ret.addAll(getHisFunction());
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
			ret.setNullable(0);
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
	protected List<String> getHisFunction() {
		List<String> ret = new ArrayList<String>();
		// TODO Auto-generated method stubString trigerupdate = "CREATE OR REPLACE TRIGGER TRI_"+this.getTableName()+"_UP\"\r\n" + 
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
			hiscolumnnames+="NEW."+co.getName().toUpperCase();
			hisdcolumnnames+="OLD."+co.getName().toUpperCase();
		}
		trigerupdate = "CREATE OR REPLACE TRIGGER TRI_"+this.getTableName()+"_UPDATE\r\n" ;
		trigerupdate += "BEFORE UPDATE\r\n"+ 
		"ON "+this.getTableName()+" FOR EACH ROW\r\n" + 
		"BEGIN\r\n" + 
		"	INSERT into "+this.getTableName()+"_HIS ("+columnnames+",LOGDATE,LOGTYPE) values("+hiscolumnnames+",now(),1);\r\n" + 
		"END;\r\n";
		ret.add(trigerupdate);
        String trigerinsert = "CREATE OR REPLACE TRIGGER TRI_"+this.getTableName()+"_INS\r\n" + 
		"BEFORE INSERT\r\n" + 
		"ON "+this.getTableName()+" FOR EACH ROW\r\n" + 
		"BEGIN\r\n" + 
		"	INSERT into "+this.getTableName()+"_HIS ("+columnnames+",LOGDATE,LOGTYPE) values("+hiscolumnnames+",now(),0);\r\n" + 
		"END;\r\n";
		ret.add(trigerinsert);
        String trigerindel = "CREATE OR REPLACE TRIGGER TRI_"+this.getTableName()+"_DEL\r\n" + 
		"BEFORE DELETE\r\n" + 
		"ON "+this.getTableName().toUpperCase()+" FOR EACH ROW\r\n" + 
		"BEGIN\r\n" + 
		"	INSERT into "+this.getTableName().toUpperCase()+"_HIS ("+columnnames+",LOGDATE,LOGTYPE) values("+hisdcolumnnames+",now(),2);\r\n" + 
		"END;\r\n";
		ret.add(trigerindel);
		return ret;
	}

	@Override
	protected String getPageSql() {
		// TODO Auto-generated method stub
		return "select * from ({sql}) limit ?, ?";
	}
	
	@Override
	protected String getClassDriver() {
		// TODO Auto-generated method stub
		return "com.mysql.jdbc.Driver";
	}
}
