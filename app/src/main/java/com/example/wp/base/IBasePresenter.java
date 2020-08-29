package com.example.wp.base;

public interface IBasePresenter<T>  {

    /**
     * 用于注册UI的回调
     * @param t
     */
    void registerViewCallback(T t);
    /**
     * 用于取消注册UI的回调
     * @param t
     */
    void unRegisterViewCallback(T t);
}
