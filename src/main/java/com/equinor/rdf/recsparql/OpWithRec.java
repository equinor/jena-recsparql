package com.equinor.rdf.recsparql;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitor;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.algebra.op.OpExt;
import org.apache.jena.sparql.algebra.walker.ApplyTransformVisitor;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.util.NodeIsomorphismMap;

public class OpWithRec extends OpExt {
    public static final String Name = "withrec";
    public final Query query;
    public final Op recOp;
    public final Op mainOp;
    public final String iri;

    public OpWithRec(Op recOp, Query recQuery, String iri, Op mainOp) {
        super(Name);
        this.query = recQuery;
        this.recOp = recOp;
        this.mainOp = mainOp;
        this.iri = iri;
    }

    @Override
    public Op effectiveOp() {
        return mainOp;
    }

    @Override
    public QueryIterator eval(QueryIterator input, ExecutionContext execCxt) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Op apply(Transform transform, OpVisitor before, OpVisitor after) {
        var trv = new ApplyTransformVisitor(transform, null, true, before, after);
        recOp.visit(trv);
        var recOpVisited = trv.opResult();
        mainOp.visit(trv);
        var mainOpVisited = trv.opResult();
        return new OpWithRec(recOpVisited, query, iri, mainOpVisited);
    }
    @Override
    public Op apply(Transform transform) {
        return this.apply(transform, null, null);
    }
    @Override
    public void outputArgs(IndentedWriter out, SerializationContext sCxt) {
        out.print(iri);
        out.print(" ");
        recOp.output(out, sCxt);
        mainOp.output(out, sCxt);
    }


    @Override
    public int hashCode() {
        return recOp.hashCode() << 1 ^ mainOp.hashCode() ^ getName().hashCode();
    }

    @Override
    public boolean equalTo(Op other, NodeIsomorphismMap labelMap) {
        if(!(other instanceof OpWithRec)) return false;
        var otherRec = (OpWithRec)other;
        if(this.iri != otherRec.iri) return false;
        return this.recOp.equalTo(otherRec.recOp, labelMap)
        && this.mainOp.equalTo(otherRec.mainOp, labelMap);
    }

}