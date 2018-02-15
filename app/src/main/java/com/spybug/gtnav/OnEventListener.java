package com.spybug.gtnav;

public interface OnEventListener<T,Q> {
    public void onSuccess(T object);
    public void onFailure(Q object);
}
