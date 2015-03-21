package com.multipay.android.helpers;

public interface Caller<T> {

    void afterCall(T result);

}