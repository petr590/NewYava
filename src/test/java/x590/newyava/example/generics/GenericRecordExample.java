package x590.newyava.example.generics;

import x590.newyava.example.Main;

import java.util.List;

public record GenericRecordExample<T>(float x, T t, List<String> list) {
    public static void main(String[] args) {
        Main.run(GenericRecordExample.class);
    }

    public GenericRecordExample {
        System.out.println("GG");
    }

    @Override
    public float x() {
        return x * x;
    }
}
