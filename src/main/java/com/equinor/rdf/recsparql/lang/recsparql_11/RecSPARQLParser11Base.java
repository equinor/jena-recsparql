package com.equinor.rdf.recsparql.lang.recsparql_11;

import org.apache.jena.query.Syntax;

import com.equinor.rdf.recsparql.RecSyntax;
import com.equinor.rdf.recsparql.lang.RecSPARQLParserBase;

public class RecSPARQLParser11Base 
    extends RecSPARQLParserBase
    implements RecSPARQLParser11Constants
{

    @Override
    protected Syntax getSyntax() {
        return RecSyntax.syntaxRecSPARQL11;
    }

};