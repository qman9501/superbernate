package supernate.oscarDialectsupernate.oscarDialect;

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
				this.setSqlType("INT4");
				break;
			case 3:
				this.setSqlType("VARCHAR2");
				break;
			case 2:
				this.setSqlType("NUMBER");
				break;
			case 1:
				this.setSqlType("DECIMAL");
				break;
			case 4:
				this.setSqlType("TEXT");
				break;
			case 5:
				this.setSqlType("TIMESTAMP");
				break;
			case 6:
				this.setSqlType("INT8");
				break;
			case 7:
				this.setSqlType("INT1");
				break;
		}
	}

	@Override
	public String getSqlString(boolean ishistory) {
		String ret="";
		switch(this.getType()) {
		case 0:
		case 4:
		case 5:
		case 6:
		case 7:
			ret = this.getName()+" "+this.getSqlType();
			break;
		default:
			ret = this.getName()+" "+this.getSqlType()+"("+this.getLength()+")";
		}
		if(this.getNullable()!=1) {
			ret+= " NOT NULL";
		}
		if(this.isKey()&&(!ishistory)) {
			ret+=",\r\nCONSTRAINT "+this.getTableName()+"_PK PRIMARY KEY ("+this.getName()+")";
		}else
		if(this.isUnique()&&(!ishistory)) {
			ret+=",\r\nCONSTRAINT "+this.getTableName()+this.getName()+"+UN UNIQUE ("+this.getName()+")";
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
		    ResultSet rs=stkey.executeQuery("select "+this.getTableName()+"_SEQUENCE.nextval from dual");
		    if(rs.next()){
		    	ret=rs.getObject(1);
	        }
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
		ret.add("CREATE SEQUENCE "+this.tableName.toUpperCase()+"_"+this.getName()+"_SEQUENCE INCREMENT 1 MINVALUE 1 START 1 NO CACHE  NO CYCLE;");
		return ret;
	}
}
