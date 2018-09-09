package com.Network;

/**
 * Created by Guillaume on 03/11/13.
 * interface implémentée par tous ceux qui appellent l'API
 */

public interface ApiCaller {
    void onApiResult(String result, int type) throws Exception;
}
