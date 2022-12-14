package com.equinor.rdf.recsparql;

import org.apache.jena.sparql.algebra.OpExtBuilder;
import org.apache.jena.sparql.algebra.op.OpExt;
import org.apache.jena.sparql.sse.ItemList;

public class WithRecursiveOpExtBuilder implements OpExtBuilder {

    public static final String WITH_RECURSIVE = "WITH";
    @Override
    public String getTagName() {
        return WITH_RECURSIVE;
    }

    @Override
    public OpExt make(ItemList argList) {
        // TODO Auto-generated method stub
        return null;
    }

}