package com.equinor.rdf.recsparql;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Iterator;
import java.util.*;

import java.util.stream.StreamSupport;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.algebra.OpExtRegistry;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.engine.QueryEngineRegistry;
import org.apache.jena.sparql.lang.SPARQLParser;
import org.apache.jena.sparql.lang.SPARQLParserFactory;
import org.apache.jena.sparql.lang.SPARQLParserRegistry;
import org.apache.jena.sparql.sse.SSE;
import org.junit.BeforeClass;
import org.junit.Test;

import com.equinor.rdf.recsparql.ParserRecARQ;
import com.equinor.rdf.recsparql.RecQuery.RecClause;
import com.equinor.rdf.recsparql.RecSyntax;

/**
 * Test API use of models, including some union graph cases : see also
 * DatasetGraphTests
 */

public class RecSparqlTest {
    // These graphs must exist.
    protected static final String graph1 = "http://example/g1";
    protected static final String graph2 = "http://example/g2";
    protected static final String graph3 = "http://example/g3";

    private Dataset dataset;
    private Model calcUnion = ModelFactory.createDefaultModel();

    protected Dataset createDataset() {
        return DatasetFactory.create();
    }

    protected Dataset getDataset() {
        if (dataset == null) {
            dataset = createDataset();
            fillDataset(dataset);
        }
        return dataset;
    }

    protected void fillDataset(Dataset dataset) {
        // Load default model.
        // Load graph 1
        // Load graph 2.
        dataset.getDefaultModel().getGraph().add(SSE.parseTriple("(<x> <p> 'Default graph')"));

        Model m1 = dataset.getNamedModel(graph1);
        m1.getGraph().add(SSE.parseTriple("(<x> <p> 'Graph 1')"));
        m1.getGraph().add(SSE.parseTriple("(<x> <p> 'ZZZ')"));

        Model m2 = dataset.getNamedModel(graph2);
        m2.getGraph().add(SSE.parseTriple("(<x> <p> 'Graph 2')"));
        m2.getGraph().add(SSE.parseTriple("(<x> <p> 'ZZZ')"));
        calcUnion.add(m1);
        calcUnion.add(m2);
    }

    String prologue = "BASE <http://test.iri/>\n";

    String queryString = prologue + with_rec("<http://graph.iri>", constructValues())
            + select_from("<http://graph.iri>");

    @BeforeClass
    public static void init() {
        RecSPARQLParserFactory.register();
        RecSPARQLParserFactory.extend();
        // OpExtRegistry.register(new WithRecursiveOpExtBuilder());
        RecQueryEngine.register();
        // QueryExecutionFactory
    }

    @Test
    public void parse_valid_single_withrec_select_from() {
        var graph = "<http://graph.iri>";
        var querystring = prologue +
                with_rec(graph, constructValues()) +
                select_from(graph);
        var query = parse(querystring);
        assertTrue("query should be recQuery", query instanceof RecQuery);
        var recQuery = ((RecQuery) query);
        assertTrue(recQuery.isRecursive());
        var firstClause = recQuery.getRecursiveQueries().iterator().next();
        assertEquals(trim(graph), firstClause.iri());
    }

    @Test
    public void parse_valid_single_withrec_select_graph() {
        var graph = "<http://graph.iri>";
        var querystring = prologue +
                with_rec(graph, constructValues()) +
                select_graph(graph);
        var query = parse(querystring);
        assertTrue("query should be recQuery", query instanceof RecQuery);
        var recQuery = ((RecQuery) query);
        assertTrue(recQuery.isRecursive());
        var firstClause = recQuery.getRecursiveQueries().iterator().next();
        assertEquals(trim(graph), firstClause.iri());
    }

    @Test
    public void parse_valid_double_withrec_select_from() {
        var graph1 = "<http://graph1.iri>";
        var graph2 = "<http://graph2.iri>";
        var querystring = prologue +
                with_rec(graph1, constructValues()) +
                with_rec(graph2, construct_dependent_from(graph1)) +
                select_from(graph2);
        var query = parse(querystring);
        assertTrue("query should be recQuery", query instanceof RecQuery);
        var recQuery = ((RecQuery) query);
        assertTrue(recQuery.isRecursive());
        Iterator<RecClause> iterator = recQuery.getRecursiveQueries().iterator();
        // NOTICE: getRecursiveQueries returns clauses in applicative order, which is
        // the reverse of declaration order !!
        var secondClause = iterator.next();
        assertEquals(trim(graph2), secondClause.iri());
        var firstClause = iterator.next();
        assertEquals(trim(graph1), firstClause.iri());
    }

    @Test
    public void parse_scope_error_double_withrec_select_from() {
        var graph1 = "<http://graph1.iri>";
        var graph2 = "<http://graph2.iri>";
        var querystring = prologue +
                with_rec(graph2, construct_dependent_from(graph1)) +
                with_rec(graph1, constructValues()) +
                select_from(graph2);
        try {
            parse(querystring);
            fail(String.format("Query: %s\n tries to use a recursive graph out of scope. Exception expected.",
                    querystring));
        } catch (QueryException ex) {
            assertTrue("Exception ", ex.getMessage().contains(graph1));
        }
    }

    @Test
    public void parse_construct_error_double_withrec_select_from() {
        var graph1 = "<http://graph1.iri>";
        var graph2 = "<http://graph2.iri>";
        var querystring = prologue +
                with_rec(graph1, constructGraph()) +
                with_rec(graph2, construct_dependent_from(graph1)) +
                select_from(graph2);
        try {
            parse(querystring);
            fail(String.format("Query: %s\n uses illegal CONSTRUC GRAPH in recursive query. Exception expected.",
                    querystring));
        } catch (QueryException ex) {
            assertTrue("Exception ", ex.getMessage().contains("CONSTRUCT GRAPH"));
        }
    }

    @Test
    public void query_single_withrec_select_from() {
        var graph = "<http://graph.iri>";
        var queryString = prologue +
                with_rec(graph, constructWhere()) +
                select_from(graph);
        Dataset ds = getDataset();
        int x = query(queryString, ds.getDefaultModel());
        assertEquals(1, x);
    }
    @Test
    public void query_single_withrec_construct_graph() {
        var graph = "<http://graph.iri>";
        var queryString = prologue +
                with_rec(graph, constructWhere()) +
                construct_dependent_graph(graph);
        Dataset ds = getDataset();
        int x = query(queryString, ds.getDefaultModel());
        assertEquals(1, x);
    }

    @Test
    public void graph1() {
        Dataset ds = getDataset();
        int x = query(queryString, ds.getDefaultModel());
        assertEquals(1, x);
    }

    @Test
    public void graph2() {
        Dataset ds = getDataset();
        int x = query(queryString, ds.getNamedModel(graph1));
        assertEquals(2, x);
    }

    @Test
    public void graph3() {
        Dataset ds = getDataset();
        int x = query(queryString, ds.getNamedModel(graph3));
        assertEquals(0, x);
    }

    @Test
    public void graph4() {
        Dataset ds = getDataset();
        int x = query(queryString, ds.getNamedModel(Quad.unionGraph.getURI()));
        assertEquals(3, x);
        Model m = ds.getNamedModel(Quad.unionGraph.getURI());
        m.isIsomorphicWith(calcUnion);
    }

    @Test
    public void graph5() {
        Dataset ds = getDataset();
        int x = query(queryString, ds.getNamedModel(Quad.defaultGraphIRI.getURI()));
        assertEquals(1, x);
    }

    @Test
    public void graph6() {
        Dataset ds = getDataset();
        int x = query(queryString, ds.getNamedModel(Quad.defaultGraphNodeGenerated.getURI()));
        assertEquals(1, x);
    }

    @Test
    public void graph_count1() {
        Dataset ds = getDataset();
        long x = count(ds.getDefaultModel());
        assertEquals(1, x);
    }

    @Test
    public void graph_count2() {
        Dataset ds = getDataset();
        long x = count(ds.getNamedModel(graph1));
        assertEquals(2, x);
    }

    @Test
    public void graph_count3() {
        Dataset ds = getDataset();
        long x = count(ds.getNamedModel(graph3));
        assertEquals(0, x);
    }

    @Test
    public void graph_count4() {
        Dataset ds = getDataset();
        long x = count(ds.getNamedModel(Quad.unionGraph.getURI()));
        assertEquals(3, x);
    }

    @Test
    public void graph_count5() {
        Dataset ds = getDataset();
        long x = count(ds.getNamedModel(Quad.defaultGraphIRI.getURI()));
        assertEquals(1, x);
    }

    @Test
    public void graph_count6() {
        Dataset ds = getDataset();
        long x = count(ds.getNamedModel(Quad.defaultGraphNodeGenerated.getURI()));
        assertEquals(1, x);
    }

    @Test
    public void graph_count7() {
        Dataset ds = getDataset();
        Model m = ds.getNamedModel("http://example/no-such-graph");
        long x = m.size();
        assertEquals(0, x);
    }

    @Test
    public void graph_api1() {
        Dataset ds = getDataset();
        int x = api(ds.getDefaultModel());
        assertEquals(1, x);
    }

    @Test
    public void graph_api2() {
        Dataset ds = getDataset();
        int x = api(ds.getNamedModel(graph1));
        assertEquals(2, x);
    }

    @Test
    public void graph_api3() {
        Dataset ds = getDataset();
        int x = api(ds.getNamedModel(graph3));
        assertEquals(0, x);
    }

    @Test
    public void graph_api4() {
        Dataset ds = getDataset();
        int x = api(ds.getNamedModel(Quad.unionGraph.getURI()));
        assertEquals(3, x);
        Model m = ds.getNamedModel(Quad.unionGraph.getURI());
        m.isIsomorphicWith(calcUnion);
    }

    @Test
    public void graph_api5() {
        Dataset ds = getDataset();
        int x = api(ds.getNamedModel(Quad.defaultGraphIRI.getURI()));
        assertEquals(1, x);
    }

    @Test
    public void graph_api6() {
        Dataset ds = getDataset();
        int x = api(ds.getNamedModel(Quad.defaultGraphNodeGenerated.getURI()));
        assertEquals(1, x);
    }

    private int query(String str, Model model) {
        Query q = parse(str);
        try (QueryExecution qexec = QueryExecutionFactory.create(q, model)) {
            if(q.isSelectType()){
                ResultSet rs = qexec.execSelect();
                return ResultSetFormatter.consume(rs);
            }
            if(q.isConstructType()){
                var ds = qexec.execConstruct();
                return (int)StreamSupport.stream(Spliterators.spliteratorUnknownSize(ds.listStatements(), 0),false).count();
            }
        }
        return 0;
    }

    private Query parse(String str) {
        Query q = QueryFactory.create(str, Syntax.syntaxARQ);
        return q;
    }

    private int api(Model model) {
        Iterator<Triple> iter = model.getGraph().find(Node.ANY, Node.ANY, Node.ANY);
        int x = (int) Iter.count(iter);
        return x;
    }

    private long count(Model model) {
        return model.size();
    }

    private String trim(String graph) {
        return graph.substring(1, graph.length() - 1);
    }

    String with_rec(String graph, String construct) {
        return String.format("WITH RECURSIVE %s AS {\n%s}\n", graph, construct);
    }

    String constructValues() {
        return "CONSTRUCT {?s ?p ?o} WHERE {} VALUES (?s ?p ?o) {(<a> <p> 1) (<b> <p2> \"YO\")}\n";
    }

    String constructWhere() {
        return "CONSTRUCT {?s ?p ?o} WHERE {?s ?p ?o}\n";
    }

    String constructGraph() {
        return "CONSTRUCT {GRAPH ?g {?s ?p ?o}} WHERE {} VALUES (?s ?p ?o ?g) {(<a> <p> 1 <g1>) (<b> <p2> \"YO\" <g2>)}\n";
    }

    String select_from(String graph) {
        return String.format("SELECT * FROM %s WHERE {?s ?p ?o}\n", graph);
    }

    String select_graph(String graph) {
        return String.format("SELECT * WHERE {GRAPH ?g {?s ?p ?o}}\n", graph);
    }

    private String construct_dependent_from(String graph1) {
        return String.format(
                "CONSTRUCT {?s ?p ?o} FROM %s WHERE {{?s ?p ?o} union {VALUES (?s ?p ?o) {(<x> <p> 99)}}}",
                graph1);
    }
    private String construct_dependent_graph(String graph1) {
        return String.format(
                "CONSTRUCT { ?s ?p ?o .} WHERE { { GRAPH %s { ?s ?p ?o . } } union {GRAPH ?g { ?s ?p ?o .} } union { VALUES ( ?s ?p ?o ) { (<x> <p> 99) } } }",
                graph1);
    }
}
