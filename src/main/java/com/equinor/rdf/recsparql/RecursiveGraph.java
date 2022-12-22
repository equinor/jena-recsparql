package com.equinor.rdf.recsparql;

import java.util.Deque;
import java.util.LinkedList;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphBase;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.util.iterator.ExtendedIterator;

public class RecursiveGraph extends GraphBase {

    private final Deque<Graph> graphs = new LinkedList<Graph>();
    private final Query query;

    public RecursiveGraph(Query query, DatasetGraph dsg){
        this.query = query;
        graphs.push(emptyGraph);
    }

    @Override
    protected ExtendedIterator<Triple> graphBaseFind(Triple triplePattern) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public void performAdd(Triple t) {
        // TODO Auto-generated method stub
        
    }

}
