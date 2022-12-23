package com.equinor.rdf.recsparql;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.ARQInternalErrorException;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.OpEval;
import org.apache.jena.sparql.engine.Plan;
import org.apache.jena.sparql.engine.QueryEngineBase;
import org.apache.jena.sparql.engine.QueryEngineFactory;
import org.apache.jena.sparql.engine.QueryEngineRegistry;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.main.OpExecutor;
import org.apache.jena.sparql.engine.main.OpExecutorFactory;
import org.apache.jena.sparql.engine.main.QC;
import org.apache.jena.sparql.engine.main.QueryEngineMain;
import org.apache.jena.sparql.engine.main.QueryEngineMainQuad;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;
import org.apache.jena.tdb.TDB;
import org.apache.jena.tdb.store.DatasetGraphTDB;
import org.apache.jena.tdb2.TDB2;
import org.apache.jena.tdb2.solver.QueryEngineTDB;
import org.apache.jena.sparql.engine.iterator.QueryIterRoot;
import org.apache.jena.sparql.engine.iterator.QueryIteratorCheck;
import org.apache.jena.sparql.engine.iterator.QueryIteratorTiming;

/**
 * Example skeleton for a query engine.
 * To just extend ARQ by custom basic graph pattern matching (a very common
 * case)
 * see the arq.examples.bgpmatching package.
 * To take full control of query execution, use this example for catching the
 * execution setup and see opexec for a customized OpExecutor for query
 * execution.
 */

public interface RecQueryEngine extends OpEval, Closeable {
    static final RecSyntax recursiveGraphs = RecSyntax.create("execution/recursiveGraphs");
    static Op createRecOp(Query query){
        if(query instanceof RecQuery rq)
            return RecAlgebra.compile(query);
        else 
            return Algebra.compile(query);
    }
    static void initContext(Query query, DatasetGraph dataset, Context context){
        if(query instanceof RecQuery rq){
            HashMap<String, RecursiveGraph> gMap = new HashMap<String, RecursiveGraph>();
            for(var c : rq.getRecursiveQueries())
                gMap.put(c.iri(), new RecursiveGraph(c.query(), dataset));
            context.set(recursiveGraphs, gMap);
        }
    }
    static RecursiveGraph getRecursiveGraph(String iri, Context context){
        Map<String, RecursiveGraph> gMap = context.get(recursiveGraphs);
        return gMap.get(iri);
    }
    static void initAllRecursiveGraphs(Query query, Context context){

    }
    static void addRecursiveGraph(String iri, Query query, DatasetGraph originalDataset, Context context){
        Map<String, RecursiveGraph> gMap = context.get(recursiveGraphs);
        gMap.put(iri, new RecursiveGraph(query, originalDataset));
    }
    static void initRecursiveGraph(String iri, QueryIterator iter, Context context){
        var rGraph = getRecursiveGraph(iri, context);
        rGraph.init(iter);
    }
    // Do nothing template for a query engine.

    // private Object sysDatasetDescription = null;

    // public final Boolean isBackedByTDB;
    // public final Boolean isTDBUnionGraph;

    // public RecQueryEngine(Query query, DatasetGraph dataset, Binding initial, Context context) {
    //     super(query, dataset, initial, context);

    //     var inner = new QueryEngineTDB(){
            
    //         }
    //     };
    //     if (dataset instanceof org.apache.jena.tdb.store.DatasetGraphTDB
    //             || dataset instanceof org.apache.jena.tdb.transaction.DatasetGraphTransaction) {
    //         this.isBackedByTDB = true;
    //         this.isTDBUnionGraph = (context.isTrue(TDB.symUnionDefaultGraph));
    //     } else if (dataset instanceof org.apache.jena.tdb2.store.DatasetGraphTDB
    //             || dataset instanceof org.apache.jena.tdb2.store.DatasetGraphSwitchable) {
    //         this.isBackedByTDB = true;
    //         this.isTDBUnionGraph = (context.isTrue(TDB2.symUnionDefaultGraph1)
    //                 || context.isTrue(TDB2.symUnionDefaultGraph2));
    //     } else
    //         this.isBackedByTDB = this.isTDBUnionGraph = false;
    // }

    // public RecQueryEngine(Query query, DatasetGraph dataset) {
    //     // This will default to the global context with no initial settings
    //     this(query, dataset, null, null);
    // }

    // @Override
    // public QueryIterator eval(Op op, DatasetGraph dsg, Binding input, Context context) {
    //     ExecutionContext execCxt = new ExecutionContext(
    //             context,
    //             dsg.getDefaultGraph(),
    //             dsg,
    //             new OpExecutorFactory() {
    //                 private OpExecutorFactory innerFactory = QC.getFactory(context);

    //                 @Override
    //                 public OpExecutor create(ExecutionContext execCxt) {
    //                     return new RecOpExecutor(execCxt, innerFactory.create(execCxt));
    //                 }
    //             });
    //     ;
    //     QueryIterator qIter1 = (input.isEmpty()) ? QueryIterRoot.create(execCxt)
    //             : QueryIterRoot.create(input, execCxt);
    //     QueryIterator qIter = QC.execute(op, qIter1, execCxt);
    //     // Wrap with something to check for closed iterators.
    //     qIter = QueryIteratorCheck.check(qIter, execCxt);
    //     // Need call back.
    //     if (context.isTrue(ARQ.enableExecutionTimeLogging))
    //         qIter = QueryIteratorTiming.time(qIter);
    //     return qIter;
    // }

    // @Override
    // protected Op minimalModifyOp(Op op) {
    //     // TODO Auto-generated method stub
    //     return super.minimalModifyOp(op);
    // }

    // @Override
    // public Plan getPlan() {
    //     // TODO Auto-generated method stub
    //     return super.getPlan();
    // }

    // /**
    //  * cannot extend this behaviour from the outside, logic copied from TDB-/TDB2
    //  * QueryEngines
    //  */
    // @Override
    // protected DatasetGraph dynamicDataset(DatasetDescription dsDesc, DatasetGraph dataset, boolean unionDftGraph) {
    //     var union = isBackedByTDB
    //             || unionDftGraph;
    //     return super.dynamicDataset(dsDesc, dataset, union);
    // }

    // @Override
    // protected Op createOp(Query query) {
    //     Op op = RecAlgebra.compile(query);
    //     // if(op instanceof OpWithRec &&
    //     // context.isDefined(ARQConstants.sysDatasetDescription)){
    //     // this.sysDatasetDescription = context.get(ARQConstants.sysDatasetDescription);
    //     // context.remove(ARQConstants.sysDatasetDescription);
    //     // }
    //     return op;
    // }

    // @Override
    // protected Op modifyOp(Op op) {
    //     // Extension point: possible place to alter the algebra expression.
    //     // Alternative to eval().
    //     op = super.modifyOp(op);
    //     // op = Algebra.toQuadForm(op) ;
    //     return op;
    // }

    // // ---- Registration of the factory for this query engine class.

    // // Query engine factory.
    // // Call MyQueryEngine.register() to add to the global query engine registry.

    // static QueryEngineFactory factory = new RecQueryEngineFactory();

    // static public QueryEngineFactory getFactory() {
    //     return factory;
    // }

    // static public void register() {
    //     QueryEngineRegistry.addFactory(factory);
    // }

    // static public void unregister() {
    //     QueryEngineRegistry.removeFactory(factory);
    // }

    // // public class RecTransform extends TransformCopy {
    // // public Op transform(OpWithRec opWithRec) {
    // // return opBGP;
    // // }
    // // }
    public class Main extends QueryEngineMain implements RecQueryEngine{

        public Main(Op op, DatasetGraph dataset, Binding input, Context context) {
            super(op, dataset, input, context);
        }
        public Main(Query query, DatasetGraph dataset, Binding input, Context context) {
            super(query, dataset, input, context);
        }
        @Override
        protected Op createOp(Query query) {
            return RecQueryEngine.createRecOp(query);
        }

    }
    public class MainQuads extends QueryEngineMainQuad implements RecQueryEngine{

        public MainQuads(Op op, DatasetGraph dataset, Binding input, Context context) {
            super(op, dataset, input, context);
        }
        public MainQuads(Query query, DatasetGraph dataset, Binding input, Context context) {
            super(query, dataset, input, context);
        }
        @Override
        protected Op createOp(Query query) {
            return RecQueryEngine.createRecOp(query);
        }

    }
    public class TDB1 extends org.apache.jena.tdb.solver.QueryEngineTDB implements RecQueryEngine{

        protected TDB1(Op op, DatasetGraphTDB dataset, Binding input, Context context) {
            super(op, dataset, input, context);
        }
        protected TDB1(Query query, DatasetGraphTDB dataset, Binding input, Context context) {
            super(query, dataset, input, context);
        }
        @Override
        protected Op createOp(Query query) {
            return RecQueryEngine.createRecOp(query);
        }

    }
    public class TDB2 extends org.apache.jena.tdb2.solver.QueryEngineTDB implements RecQueryEngine{

        protected TDB2(Op op, org.apache.jena.tdb2.store.DatasetGraphTDB dataset, Binding input, Context context) {
            super(op, dataset, input, context);
        }
        protected TDB2(Query query, org.apache.jena.tdb2.store.DatasetGraphTDB dataset, Binding input, Context context) {
            super(query, dataset, input, context);
        }
        @Override
        protected Op createOp(Query query) {
            return RecQueryEngine.createRecOp(query);
        }

    }

    static class RecQueryEngineFactory implements QueryEngineFactory {
        // Accept any dataset for query execution
        public static Symbol recQueryProcessed = RecSyntax.create("execution/recQueryProcessed");

        @Override
        public boolean accept(Query query, DatasetGraph dataset, Context context) {
            if (context.get(recQueryProcessed, false))
                return false;
            if (!(query instanceof RecQuery))
                return false;
            if (!((RecQuery) query).isRecursive())
                return false;
            return true;
        }

        @Override
        public Plan create(Query query, DatasetGraph dataset, Binding initial, Context context) {
            RecQueryEngine.initContext(query, dataset, context);
            var recEngine = new RecQueryEngine(query, dataset, initial, context);
            Op op = recEngine.getOp();
            context.put(recQueryProcessed, true);
            QueryEngineFactory realFactory = QueryEngineRegistry.findFactory(op, dataset, context);
            return realFactory.create(op, recEngine.dataset, initial, context);

        }

        @Override
        public boolean accept(Op op, DatasetGraph dataset, Context context) { // Refuse to accept algebra expressions
                                                                              // directly.
            return false;
        }

        @Override
        public Plan create(Op op, DatasetGraph dataset, Binding inputBinding, Context context) { // Should notbe called
                                                                                                 // because accept/Op is
                                                                                                 // false
            throw new ARQInternalErrorException("RecQueryEngine: factory called directly with an algebra expression");
        }
    // }
}