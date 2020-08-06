package com.mashreq.transfercoreservice.middleware;

import java.util.Comparator;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SortByValueFirst<T> implements Comparator<T> {

    private final T value;

    @Override
    public int compare(T t1, T t2) {
        if(value != null){
            if(value.equals(t1)){
                return -1;
            }else if(value.equals(t2)){
                return 1;
            }
        }
        return 0;
    }
}
