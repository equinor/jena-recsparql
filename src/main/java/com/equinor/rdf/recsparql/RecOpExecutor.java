package com.equinor.rdf.recsparql;

import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.main.OpExecutor;

public class RecOpExecutor extends OpExecutor {


    private final OpExecutor wrapped;
    protected RecOpExecutor(ExecutionContext execCxt, OpExecutor wrapped) {
        super(execCxt);
        this.wrapped = wrapped;
    }

    // OpQuadPattern


}
