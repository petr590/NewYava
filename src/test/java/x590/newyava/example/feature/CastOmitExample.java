package x590.newyava.example.feature;

import org.junit.Test;
import x590.newyava.example.Main;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("all")
public class CastOmitExample {
	@Test
	public void run() {
		Main.run(this);
	}

	public String get(List<String> list) {
		return list.get(0);
	}

	public String get(Optional<String> optional) {
		return optional.get();
	}
}
