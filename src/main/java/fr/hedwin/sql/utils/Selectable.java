package fr.hedwin.sql.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Selectable {

    private Map<String, Object> params = new HashMap<>();
    private boolean equal;

    public Selectable(boolean equal, Object[]... strings){
        this.equal = equal;
        for(Object[] param : strings){
            params.put(param[0].toString(), param[1]);
        }
    }
    public Selectable(Object[]... strings){
        this.equal = true;
        for(Object[] param : strings){
            params.put(param[0].toString(), param[1]);
        }
    }

    public String toString(){
        if(params.isEmpty()) return "";
        else if(equal) return "WHERE "+params.entrySet().stream().map(e -> e.getKey()+" = '"+e.getValue()+"'").collect(Collectors.joining("&&"));
        else return "WHERE "+params.entrySet().stream().map(e -> e.getKey()+" LIKE '%"+e.getValue()+"%'").collect(Collectors.joining("&&"));
    }

}
