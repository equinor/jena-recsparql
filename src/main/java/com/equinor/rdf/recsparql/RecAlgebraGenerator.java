package com.equinor.rdf.recsparql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.AlgebraGenerator;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.Op1;
import org.apache.jena.sparql.algebra.op.OpModifier;
import org.apache.jena.sparql.syntax.ElementNamedGraph;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.util.Context;

import com.equinor.rdf.recsparql.RecQuery.RecClause;

public class RecAlgebraGenerator extends AlgebraGenerator {
    private final Context context;
    private final int subQueryDepth;
    private final String recursiveGraphName;
    private HashSet<String> referencedNamedgraphs;

    protected RecAlgebraGenerator(Context context, int depth, HashSet<String> referencedNamedGraphs,
            String recursiveGraphName) {
        super(context, depth);
        this.context = context;
        this.subQueryDepth = depth;
        this.referencedNamedgraphs = referencedNamedGraphs;
        this.recursiveGraphName = recursiveGraphName;
    }

    static public RecAlgebraGenerator create(Context context, int depth, HashSet<String> referencedNamedGraphs,
            String recursiveGraphName) {
        context = context != null ? context : ARQ.getContext().copy();
        return new RecAlgebraGenerator(context, depth, referencedNamedGraphs, recursiveGraphName);
    }

    static public RecAlgebraGenerator create(Context context) {
        return create(context, 0, new HashSet<String>(), "root");
    }

    static public RecAlgebraGenerator create() {
        return create(null);
    }

    @Override
    public Op compile(Query query) {
        Op op = compile(query.getQueryPattern());
        op = compileRecursiveGraphs(query, op);
        op = compileModifiers(query, op);
        return op;
    }

    private Op compileRecursiveGraphs(Query query, Op op) {
        if (!(query instanceof RecQuery))
            return op;
        if (!((RecQuery) query).isRecursive)
            return op;
        var rq = (RecQuery) query;
        referencedNamedgraphs.addAll(Stream.concat(
                rq.getGraphURIs().stream(),
                rq.getNamedGraphURIs().stream())
                .toList());

        RecClause[] recursiveQueries = rq.getRecursiveQueries().toArray(new RecClause[0]);
        int depth = 0;
        for (var c : recursiveQueries) {
            if (!referencedNamedgraphs.contains(c.iri()))
                continue; // Will never be referenced. Optimize away.

            Query sq = c.query();
            referencedNamedgraphs.addAll(Stream.concat(
                    sq.getGraphURIs().stream(),
                    sq.getNamedGraphURIs().stream())
                    .toList());

            AlgebraGenerator gen = new RecAlgebraGenerator(context, depth++, referencedNamedgraphs, c.iri());
            Op recOp = gen.compile(sq);
            op = new OpWithRec(recOp, query, c.iri(), op);

        }
        // for(Map.Entry<String,)
        return op;
    }

    @Override
    protected Op compileElementGraph(ElementNamedGraph eltGraph) {
        Op op = super.compileElementGraph(eltGraph);
        Node node = eltGraph.getGraphNameNode();
        if (node.isURI()) {
            referencedNamedgraphs.add(node.getURI());
        }
        return op;

    }

    @Override
    protected Op compileElementSubquery(ElementSubQuery eltSubQuery) {
        AlgebraGenerator gen = new RecAlgebraGenerator(context, subQueryDepth + 1, referencedNamedgraphs,
                recursiveGraphName);
        return gen.compile(eltSubQuery.getQuery());
    }

}
