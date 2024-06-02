package br.com.farabolini.challenge.domain.interceptors;

public interface InterceptorExecutor<T> {

    void execute(T message);

}
