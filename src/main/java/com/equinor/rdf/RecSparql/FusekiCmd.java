package com.equinor.rdf.RecSparql;
import org.apache.jena.fuseki.main.cmds.FusekiMainCmd;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpExtRegistry;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.Plan;
import org.apache.jena.sparql.engine.QueryEngineFactory;
import org.apache.jena.sparql.engine.QueryEngineRegistry;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.util.Context;

public class FusekiCmd {

    public static void main(String[] args) {
        OpExtRegistry.register(new WithRecursiveOpExtBuilder());
        QueryEngineRegistry.addFactory(new QueryEngineFactory() {

            @Override
            public boolean accept(Query query, DatasetGraph dataset, Context context) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public Plan create(Query query, DatasetGraph dataset, Binding inputBinding, Context context) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public boolean accept(Op op, DatasetGraph dataset, Context context) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public Plan create(Op op, DatasetGraph dataset, Binding inputBinding, Context context) {
                // TODO Auto-generated method stub
                return null;
            }
            
        });
        // org.apache.jena.fuseki.cmd.FusekiCmd.main(args);
        FusekiMainCmd.main(args);

    }
}