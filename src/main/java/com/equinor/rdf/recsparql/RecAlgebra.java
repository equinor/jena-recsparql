package com.equinor.rdf.recsparql;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;

public class RecAlgebra extends Algebra {
    
    public static Op compile(Query query) {
        if ( query == null )
            return null;
        return RecAlgebraGenerator.create().compile(query);
    }
}
