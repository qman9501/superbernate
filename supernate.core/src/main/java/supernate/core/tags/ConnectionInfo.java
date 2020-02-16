/**
 * 
 */
package supernate.core.tags;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author Administrator
 *
 */

@Target({TYPE}) 
@Retention(RUNTIME)
public @interface ConnectionInfo {
	String username() default "";
	String password() default "";
	String url() default "";
	String driver() default "";
}
