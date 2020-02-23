package supernate.oracleDialect;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import com.sxkj.jutils.TimestampHelper;

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
	
	public String getSqlString() {
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
		if(this.isKey()) {
			ret+=",\r\nCONSTRAINT "+this.getTableName()+"_PK PRIMARY KEY ("+this.getName()+")";
		}else
		if(this.isUnique()) {
			ret+=",\r\nCONSTRAINT "+this.getTableName()+this.getName()+"+UN UNIQUE ("+this.getName()+")";
		}
		return ret;
	}
	
	public String getIndexSql() {
		if(this.isIndex()) {
			return "CREATE INDEX "+this.getTableName()+"_IDX_"+this.getName()+" ON "+this.getTableName().toUpperCase()+" USING BTREE("+this.getName()+") INIT 64K NEXT 64K MAXSIZE UNLIMITED FILL 70 SPLIT 50;";
		}
		return "";
	}
	
}
