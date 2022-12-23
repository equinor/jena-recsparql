package com.equinor.rdf.recsparql;

import org.apache.jena.query.Syntax;

public class RecSyntax extends Syntax {
    protected RecSyntax(String s) {
        super(s);
    }

    public static RecSyntax create(String localname) {
        return new RecSyntax(recSyntaxNS + localname);
    }

    public static final String recSyntaxNS = "http://rdf.equinor.com/recsparql/";
    public static final Syntax syntaxRecSPARQL11 = create("syntax/RecSPARQL_11");
    public static final Syntax syntaxRecARQ = create("syntax/RecARQ");
}
