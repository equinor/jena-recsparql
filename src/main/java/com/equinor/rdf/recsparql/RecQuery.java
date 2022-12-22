package com.equinor.rdf.recsparql;

import java.util.ArrayList;
import java.util.Collection;
import org.apache.jena.query.Query;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.expr.ExprTransform;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransform;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformCopyBase;
import org.apache.jena.sparql.syntax.syntaxtransform.ExprTransformApplyElementTransform;

public class RecQuery extends Query {
    // Recursive attributes
    protected boolean isRecursive = false;
    private ArrayList<RecClause> recClauses_applicationOrder;
    public RecQuery(){
    }
    public void setRecursive(Syntax syntax){
        if(isRecursive) return;
        isRecursive = true;
        setSyntax(syntax);
        recClauses_applicationOrder = new ArrayList<>();
    }
    public boolean isRecursive() {return isRecursive;}

    public void addRecursiveQuery(String iri, Query query){
        recClauses_applicationOrder.add(0,new RecClause(iri, query));
    }
    /** Returns WITH RECURSIVE clauses in application-order (innermost first) */
    public Collection<RecClause> getRecursiveQueries(){
        if(recClauses_applicationOrder == null) java.util.Collections.emptyList();
        return java.util.Collections.unmodifiableCollection(recClauses_applicationOrder);
    }
    @Override
    public Query cloneQuery() {
        ElementTransform eltTransform = new ElementTransformCopyBase(true);
        ExprTransform exprTransform = new ExprTransformApplyElementTransform(eltTransform, true);

        Query result = RecQueryTransformOps.transform(this, eltTransform, exprTransform);
        return result;
    }
 
    public record RecClause(String iri, Query query){}
    }
