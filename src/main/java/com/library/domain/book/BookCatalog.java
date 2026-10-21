package com.library.domain.book;

import java.util.*;
import java.util.function.Consumer;

public class BookCatalog implements Iterable<Book>{

    private final BookRepository books;
    private final int pageSize;

    public BookCatalog(BookRepository books, int pageSize) {
        if(pageSize <= 0)
        {
            throw new IllegalArgumentException("Velicina stranice mora biti pozitivna");
        }
        this.books = books;
        this.pageSize = pageSize;
    }

    @Override
    public Iterator<Book> iterator() {
        return new PageIterator();
    }

    private class PageIterator implements Iterator<Book>{
        private int nextPage = 0;
        private Iterator<Book> current = Collections.emptyIterator();
        private boolean lastPageLoaded = false;

        @Override
        public boolean hasNext() {
            while(!current.hasNext() && !lastPageLoaded){
                List<Book> page = books.findPage(nextPage++, pageSize);
                lastPageLoaded = page.size() < pageSize;
                current = page.iterator();
            }

            return current.hasNext();
        }

        @Override
        public Book next() {
            if(!hasNext()){
                throw new NoSuchElementException("Nema vise knjiga u katalogu");
            }

            return current.next();
        }
    }

    @Override
    public void forEach(Consumer<? super Book> action) {
        Iterable.super.forEach(action);
    }

    @Override
    public Spliterator<Book> spliterator() {
        return Iterable.super.spliterator();
    }
}
