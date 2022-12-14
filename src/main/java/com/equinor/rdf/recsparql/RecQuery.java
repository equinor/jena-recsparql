package com.equinor.rdf.recsparql;

import java.util.HashMap;

import org.apache.jena.query.Query;
import org.apache.jena.query.Syntax;

public class RecQuery extends Query {
    // Recursive attributes
    protected boolean isRecursive = false;
    public HashMap<String,Query> constructRecursiveQueries;
    public RecQuery(){
    }
    public void setRecursive(Syntax syntax){
        if(isRecursive) return;
        isRecursive = true;
        setSyntax(syntax);
        constructRecursiveQueries = new HashMap<String,Query>();
    }
    public boolean isRecursive() {return isRecursive;}
    public void addRecursiveQuery(String iri, Query query){
        constructRecursiveQueries.put(iri, query);
    }

    
}
