package com.equinor.rdf.recsparql;

import org.apache.jena.graph.Node_URI;
import org.apache.jena.sparql.algebra.op.OpGraph;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.main.OpExecutor;
import org.apache.jena.sparql.engine.main.QC;

public class RecOpExecutor extends OpExecutor {

    protected RecOpExecutor(ExecutionContext execCxt) {
        super(execCxt);
    }

    @Override
    protected QueryIterator execute(OpGraph opGraph, QueryIterator input) {
        if (opGraph.getNode() instanceof Node_URI node &&
                RecQueryEngine.getRecursiveGraph(node.getURI(), execCxt.getContext()) instanceof RecursiveGraph rg) {

            ExecutionContext cxt2 = new ExecutionContext(execCxt, rg);
            return QC.execute(opGraph.getSubOp(), input, cxt2);
        }

        return super.execute(opGraph, input);
    }

}
