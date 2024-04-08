package com.los.payment.cache;



import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author paul 2024/4/1
 */

public interface Cache {

    /*
        strings
     */

    // get k
    String getString(String k);
    // set k v
    void setString(String k,String v);
    //set k v exp
    void setString(String k, String v, Long exp);
    // set k v exp timeUnit
    void setString(String k, String v, Long exp, TimeUnit timeUnit);
    // mset k v [k v ...]
    void setString(Map<String,String> map);
    // mget k [k ...]
    List<String> getString(Collection<String> keys);


    /*
        hash
     */

    //hset k f v
    void setHash(String k,String f,String v);
    //mhset f v [f v ...]
    void setHash(String k,Map<String,Object> map);
    //hget k f
    String getHash(String k,String f);
    //mhget k f [ f ...]
    Map<Object,Object> getHash(String k);
    //heys k  #return all field names in the hash stored at key
    Collection<String> getHashFieldNames(String k);
    //hvals k  #return all the values in the hash stored at key
    Collection<String> getHashFieldValues(String k);
    //hdel k f [f...]
    void delHashFields(String k,Collection<String> fields);


}