package com.equinor.rdf.recsparql;

import java.util.ArrayList;

import org.apache.jena.query.Query;

public class RecQuery extends Query {
    // Recursive attributes
    public boolean isRecursive;
    public ArrayList<Query> ConstructRecursiveQueries;
    public RecursiveNode auxNode;
    public String recursiveURI;

    public RecQuery(){
        this.setSyntax(RecSyntax.syntaxRecSPARQL);
    }
        
    public void initRecursiveFields(RecursiveNode rn)
    {
    	isRecursive = true;
    	ConstructRecursiveQueries = new ArrayList<Query>();
    	auxNode = rn;
    }
    
}
