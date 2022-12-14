package com.equinor.rdf.recsparql.lang;

import org.apache.jena.query.Query;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.lang.SPARQLParserBase;

import com.equinor.rdf.recsparql.RecQuery;

public abstract class RecSPARQLParserBase 
    extends SPARQLParserBase
{
    protected abstract Syntax getSyntax();
    public RecQuery getRecQuery() throws Exception{
        if(!(query instanceof RecQuery))
            throw new Exception("Parser initialized with un-extended Query type, cannot process recursive query.");
        return (RecQuery)query;
    }
    public void startRecursiveConstruct(String iri, int beginColumn, int beginLine){
        RecQuery recursiveQuery;
        try{
            recursiveQuery = getRecQuery();
        }
        catch(Exception e){
            pushQuery();
            query = new Query(); //we are in an error state now, this query will be discarded
            return;
        }
        Query recursiveConstruct = newSubQuery(getPrologue());
        recursiveQuery.setRecursive(getSyntax());
        recursiveQuery.addRecursiveQuery(iri, recursiveConstruct);
        pushQuery();
        query = recursiveConstruct;
    }
    public void endRecursiveConstruct(String iri, int beginColumn, int beginLine){
        popQuery();
    }
};