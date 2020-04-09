package supernate.mariadbDialect;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.sxkj.jutils.TimestampHelper;

import supernate.core.Entity;
import supernate.core.tags.Column;
import org.springframework.util.ReflectionUtils;

public class ColumnInfo extends supernate.core.ColumnInfo{

	public ColumnInfo() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public ColumnInfo(Column co){
		super(co);
	}
	
	@Override
	public void setType(int type) {
		switch(type) {
			case 0:
				this.setSqlType("DECIMAL");
				if(this.getLength()==null||this.getLength().equals(""))
				this.setLength("4,0");
				break;
			case 3:
				this.setSqlType("VARCHAR");
				if(this.getLength()==null||this.getLength().equals(""))
					this.setLength("255");
				break;
			case 2:
			case 1:
				this.setSqlType("DECIMAL");
				if(this.getLength()==null||this.getLength().equals(""))
					this.setLength("11");
				break;
			case 4:
				this.setSqlType("TEXT");
				if(this.getLength()==null||this.getLength().equals(""))
					this.setLength("");
				break;
			case 5:
				this.setSqlType("DATETIME");
				if(this.getLength()==null||this.getLength().equals(""))
					this.setLength("");
				break;
			case 6:
				this.setSqlType("DECIMAL");
				this.setLength("8,0");
				break;
			case 7:
				this.setSqlType("DECIMAL");
				this.setLength("1,0");
				break;
		}
		this.type=type;
	}

	@Override
	public String getSqlString(boolean ishistory) {
		String ret="";
		ret = this.getName()+" " +this.getSqlType()+(this.getLength()!=null&&!this.getLength().equals("")?"("+this.getLength()+")":"");
		if(this.getNullable()!=1) {
			ret+= " NOT NULL";
		}
		if(this.isKey()&&(!ishistory)) {
			ret+=",\r\nCONSTRAINT "+this.getTableName()+"_PK PRIMARY KEY ("+this.getName()+")";
		}else
		if(this.isUnique()&&(!ishistory)) {
			ret+=",\r\nCONSTRAINT "+this.getTableName()+this.getName()+"_UN UNIQUE ("+this.getName()+")";
		}
		return ret;
	}

	@Override
	public String getIndexSql(boolean ishistory) {
		if(this.isIndex()) {
			return "CREATE INDEX "+this.getTableName()+(ishistory?"_HIS":"")+"_IDX_"+this.getName()+" ON "+this.getTableName().toUpperCase()+(ishistory?"_HIS":"")+" USING BTREE("+this.getName()+") INIT 64K NEXT 64K MAXSIZE UNLIMITED FILL 70 SPLIT 50;";
		}
		return "";
	}
	
	@Override
	public Object getAutoValue(Entity et) {
		Object ret = null;
		Statement stkey;
		try {
			stkey = et.getConn().createStatement();
		    ResultSet rs=stkey.executeQuery("select "+this.getTableName()+"_"+this.getName()+"_nextval() from dual");
		    if(rs.next()){
		    	ret=rs.getObject(1);
	        }
		    this.setValue(ret);
		    rs.close();
		    stkey.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
		return ret;
	}

	@Override
	public List<String> getAutoFunctioin() {
		// TODO Auto-generated method stub
		List<String> ret = new ArrayList<String>();
		String squence=this.tableName.toUpperCase()+"_"+this.getName();
		String rettemp = "CREATE TABLE\n" + 
				squence+"_sequence\n" + 
				"    (\n" + 
				"        minval bigint NOT NULL,\n" + 
				"        maxval bigint NOT NULL,\n" + 
				"        current_val bigint NOT NULL,\n" + 
				"        increment_val bigint DEFAULT '1' NOT NULL\n" + 
				"    );\n";
		ret.add(rettemp);
		rettemp = "CREATE FUNCTION "+squence+"_nextval() RETURNS bigint\n" + 
				"begin  \n" + 
				"declare _cur bigint;\n" + 
				"declare _maxvalue bigint;  -- 接收最大值\n" + 
				"declare _increment bigint; -- 接收增长步数\n" + 
				"declare _minvalue bigint;\n" + 
				"select increment_val,maxval,current_val,minval into _increment, _maxvalue,_cur, _minvalue from "+squence+"_sequence LIMIT 1;\n" + 
				"update "+squence+"_sequence                      -- 更新当前值\n" + 
				" set current_val = _cur + increment_val  ;\n" + 
				"if(_cur + _increment >= _maxvalue) then  -- 判断是都达到最大值\n" + 
				"      update "+squence+"_sequence  \n" + 
				"        set current_val = _minvalue  ;\n" +
				"end if;\n" + 
				"return _cur;  \n" + 
				"end;\n";
		ret.add(rettemp);
		rettemp = "insert into "+squence+"_sequence values(0,9999999999999,1,1);";
		ret.add(rettemp);
		return ret;
	}
}
