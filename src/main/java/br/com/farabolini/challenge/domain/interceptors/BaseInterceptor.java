package br.com.farabolini.challenge.domain.interceptors;

public interface BaseInterceptor<T> {

    void intercept(T message);

}
