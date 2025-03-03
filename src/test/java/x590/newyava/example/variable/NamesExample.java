package x590.newyava.example.variable;

import org.junit.Test;
import x590.newyava.Config;
import x590.newyava.example.Main;

import javax.swing.text.html.HTMLDocument;
import java.net.URL;

@SuppressWarnings("all")
public class NamesExample {
	@Test
	public void run() {
		Main.run(this, Config.builder().ignoreVariableTable(true).build());
	}

	public static final native synchronized void main(String[] args);

	private native void setF(int f);

	private native void gg(int a, int b, int c);


	private String getString() {
		return "";
	}

	private void useString(String s) {}

	public void foo(boolean x) {
		String s = null;

		if (x)
			s = getString();

		useString(s);
	}

	interface User {
		String getName();

		void setName(String name);

		Vec3 speed();
	}

	interface Vec3 {}

	public void getAndSet(User user) {
		String name1 = user.getName();
		String name2 = "gg";
		user.setName(name2);
		setUserName(name2);
		Vec3 vec3_1 = user.speed();
		Vec3 vec3_2 = user.speed();
	}

	public void setUserName(String name) {}

	private void longVarNames(long x, long y, long z) {
		long min = Math.min(x, y);
	}

	private native void longVarNamesWithoutMethodBody(long x, long y, long z);

	public void test(URL url, HTMLDocument htmlDocument, Character character) {}

	// OMG
	public void test(Abstract abs, Assert assrt, Boolean bool, Break brk, Byte b, Case cs, Catch ctch,
	                 Char ch, Class clazz, Continue cont, Default def, Do d1, Double d2, Else els,
	                 Enum en, Extends ext, False fls, Final fin, Finally finl, Float f1, For fr,
	                 Goto gt, If f2, Implements impl, Import imp, Instanceof inst, Int n,
	                 Interface interf, Long l, Native nat, New mew, Null nll, Package pack,
	                 Private priv, Protected prot, Public pub, Return ret, Short s, Static stat,
	                 Strictfp strict, Super sup, Switch swt, This ths, Throw thr, Throws thrs,
	                 Transient trans, True tr1, Try tr2, Void v, Volatile vol, While whl,
	                 Var var, Record record, Sealed sealed, Yield yield) {}

	private interface Abstract {}
	private interface Assert {}
	private interface Boolean {}
	private interface Break {}
	private interface Byte {}
	private interface Case {}
	private interface Catch {}
	private interface Char {}
	private interface Class {}
	private interface Continue {}
	private interface Default {}
	private interface Do {}
	private interface Double {}
	private interface Else {}
	private interface Enum {}
	private interface Extends {}
	private interface False {}
	private interface Final {}
	private interface Finally {}
	private interface Float {}
	private interface For {}
	private interface Goto {}
	private interface If {}
	private interface Implements {}
	private interface Import {}
	private interface Instanceof {}
	private interface Int {}
	private interface Interface {}
	private interface Long {}
	private interface Native {}
	private interface New {}
	private interface Null {}
	private interface Package {}
	private interface Private {}
	private interface Protected {}
	private interface Public {}
	private interface Return {}
	private interface Short {}
	private interface Static {}
	private interface Strictfp {}
	private interface Super {}
	private interface Switch {}
	private interface This {}
	private interface Throw {}
	private interface Throws {}
	private interface Transient {}
	private interface True {}
	private interface Try {}
	private interface Void {}
	private interface Volatile {}
	private interface While {}

	private interface Var {}
	private interface Record {}
	private interface Sealed {}
	private interface Yield {}
}
