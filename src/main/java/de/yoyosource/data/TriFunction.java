package de.yoyosource.data2;

public interface TriFunction<A, B, C, D> {
    D accept(A a, B b, C c);
}
