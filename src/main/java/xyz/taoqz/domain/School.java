package xyz.taoqz.domain;
import java.io.Serializable;

/**
* Created by T on  2020-08-16
 */

public class School  implements Serializable {

	private static final long serialVersionUID =  37101540617130857L;

	private Integer id;

	private String name;

	@Override
	public String toString() {
		return "School{" +
				"id=" + id +
				", name='" + name + '\'' +
				'}';
	}
}
