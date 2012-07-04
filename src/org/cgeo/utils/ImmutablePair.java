package org.cgeo.utils;

public class ImmutablePair<T1, T2> {
    public final T1 left;
    public final T2 right;
    
    public ImmutablePair(final T1 left, final T2 right) {
        this.left = left;
        this.right = right;
    }
}
