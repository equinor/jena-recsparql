package com.equinor.rdf.recsparql;

import org.apache.jena.query.Syntax;

public class RecSyntax extends Syntax {
    protected RecSyntax(String s) {
        super(s);
    }

    public static final Syntax syntaxRecSPARQL
    = Syntax.make("http://rdf.equinor.com/query/RecSPARQL_11");
}
