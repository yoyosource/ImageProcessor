package de.yoyosource.data;

public interface TriFunction<A, B, C, D> {
    D accept(A a, B b, C c);
}
