package com.zolstein.compacthashmap;

import java.util.Iterator;
import java.util.function.Function;

class TransformedIterator<T, U> implements Iterator<U> {
    private Iterator<? extends T> source;
    private Function<? super T, ? extends U> transformer;
    TransformedIterator(Iterator<T> source, Function<T, U> transformer) {
        this.source = source;
        this.transformer = transformer;
    }

    @Override
    public boolean hasNext() {
        return source.hasNext();
    }

    @Override
    public U next() {
        return transformer.apply(source.next());
    }

    @Override
    public void remove() {
        source.remove();
    }
}
