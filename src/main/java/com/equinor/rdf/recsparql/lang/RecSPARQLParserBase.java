package com.equinor.rdf.recsparql.lang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.lang.QueryParserBase;
import org.apache.jena.sparql.lang.SPARQLParserBase;
import org.apache.jena.sparql.modify.request.QuadAccSink;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementNamedGraph;
import org.apache.jena.sparql.syntax.ElementVisitor;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;

import com.equinor.rdf.recsparql.RecQuery;

public abstract class RecSPARQLParserBase
        extends SPARQLParserBase {
    private RecQuery recursiveQuery;

    protected abstract Syntax getSyntax();

    private HashSet<String> referencedGraphs = new HashSet<>();
    private HashMap<String, ArrayList<int[]>> iriLocations = new HashMap<>();

    public RecQuery getRecQuery() {
        if (!(query instanceof RecQuery))
            super.throwParseException(
                    "Parser initialized with un-extended Query type, cannot process recursive query.");
        return (RecQuery) query;
    }

    private static final String scopeError = """
            Scope error: Recursive graph <%s> referenced before declaration at location(s): %s
            """;

    protected void startRecursiveConstruct(String iri, int beginLine, int beginColumn) {
        if (referencedGraphs.contains(iri))
            super.throwParseException(
                    String.format(
                            scopeError,
                            iri,
                            String.join(", ", iriLocations
                                    .get(iri)
                                    .stream()
                                    .map((int[] loc) -> String.format("(line: %d, column: %d)", loc[0], loc[1]))
                                    .toList())),
                    beginLine, beginColumn);
        recursiveQuery = getRecQuery();
        Query recursiveConstruct = newSubQuery(getPrologue());
        recursiveQuery.setRecursive(getSyntax());
        recursiveQuery.addRecursiveQuery(iri, recursiveConstruct);
        query = recursiveConstruct;
    }

    public void endRecursiveConstruct(String iri, int beginColumn, int beginLine) {
        if (recursiveQuery == null)
            return;
        Stream.concat(
                query.getNamedGraphURIs().stream(),
                query.getGraphURIs().stream())
                .forEach(graph -> referencedGraphs.add(graph));
        query = recursiveQuery;
        recursiveQuery = null;
    }

    @Override
    protected String resolveIRI(String iriStr, int line, int column) {
        var iri = super.resolveIRI(iriStr, line, column);
        addIriLocation(iri, line, column);
        return iri;
    }

    @Override
    protected String resolvePName(String prefixedName, int line, int column) {
        var pname = super.resolvePName(prefixedName, line, column);
        addIriLocation(pname, line, column);
        return pname;
    }

    private void addIriLocation(String iri, int line, int column) {
        ArrayList<int[]> locations;
        if (iriLocations.containsKey(iri))
            locations = iriLocations.get(iri);
        else {
            locations = new ArrayList<>();
            iriLocations.put(iri, locations);
        }
        locations.add(new int[] { line, column });
    }

    @Override
    protected void endGroup(ElementGroup elg) {
        super.endGroup(elg);
        if (recursiveQuery == null)
            return;
        ElementWalker.walk(elg, new ElementVisitorBase() {
            @Override
            public void visit(ElementNamedGraph el) {
                var graphNode = el.getGraphNameNode();
                if (!graphNode.isURI())
                    return;
                referencedGraphs.add(graphNode.getURI());
            }
        });
    }

    @Override
    protected void setAccGraph(QuadAccSink acc, Node gn) {
        if (!super.getBNodesAreVariables() && recursiveQuery != null) {
            var locs = iriLocations.get(recursiveQuery.getRecursiveQueries().iterator().next().iri());
            var it = locs.iterator();
            int[] last = { 0, 0 };
            while (it.hasNext())
                last = it.next();
            super.throwParseException("Illegal CONSTRUCT GRAPH template in recursive query", last[0], last[1]);
        }
        super.setAccGraph(acc, gn);
    }
};