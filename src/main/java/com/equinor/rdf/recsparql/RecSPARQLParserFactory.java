package com.equinor.rdf.recsparql;

import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.lang.SPARQLParser;
import org.apache.jena.sparql.lang.SPARQLParserFactory;
import org.apache.jena.sparql.lang.SPARQLParserRegistry;

public class RecSPARQLParserFactory implements SPARQLParserFactory {
    private static SPARQLParserFactory arq = null, sparql11 = null;
    private static RecSPARQLParserFactory factory = new RecSPARQLParserFactory();
    
    static public RecSPARQLParserFactory getFactory() {
        return factory;
    }
    static public void register() {
        SPARQLParserRegistry.addFactory(RecSyntax.syntaxRecARQ, factory);

        Syntax.querySyntaxNames.put("recarq", RecSyntax.syntaxRecARQ);
        Syntax.querySyntaxNames.put("rec-arq", RecSyntax.syntaxRecARQ);

        SPARQLParserRegistry.addFactory(RecSyntax.syntaxRecSPARQL11, factory);
        
        Syntax.querySyntaxNames.put("recsparql", RecSyntax.syntaxRecSPARQL11);
        Syntax.querySyntaxNames.put("rec-sparql", RecSyntax.syntaxRecSPARQL11);
        Syntax.querySyntaxNames.put("recsparql11", RecSyntax.syntaxRecSPARQL11);
        Syntax.querySyntaxNames.put("rec-sparql11", RecSyntax.syntaxRecSPARQL11);
        Syntax.querySyntaxNames.put("recsparql_11", RecSyntax.syntaxRecSPARQL11);
        Syntax.querySyntaxNames.put("rec-sparql_11", RecSyntax.syntaxRecSPARQL11);
    }
    static public void extend(){
        arq = SPARQLParserRegistry.findFactory(Syntax.syntaxARQ);
        sparql11 = SPARQLParserRegistry.findFactory(Syntax.syntaxSPARQL_11);
        SPARQLParserRegistry.addFactory(Syntax.syntaxARQ, factory);
        SPARQLParserRegistry.addFactory(Syntax.syntaxSPARQL_11, factory);
    }

    static public void unregister() {
        SPARQLParserRegistry.removeFactory(RecSyntax.syntaxRecARQ);
        SPARQLParserRegistry.removeFactory(RecSyntax.syntaxRecSPARQL11);
    }
    static public void unextend() {
        if(arq != null){
            SPARQLParserRegistry.removeFactory(Syntax.syntaxARQ);
            SPARQLParserRegistry.addFactory(Syntax.syntaxARQ, arq);
            arq = null;
        } 
        if(sparql11 != null){
            SPARQLParserRegistry.removeFactory(Syntax.syntaxSPARQL_11);
            SPARQLParserRegistry.addFactory(Syntax.syntaxSPARQL_11, sparql11);
            sparql11 = null;
        } 
    }

    private RecSPARQLParserFactory(){}

    @Override
    public boolean accept(Syntax syntax) {
        if (syntax.equals(RecSyntax.syntaxRecARQ))
            return true;
        if (syntax.equals(RecSyntax.syntaxRecSPARQL11))
            return true;
        if (syntax.equals(Syntax.syntaxARQ))
            return true;
        if (syntax.equals(Syntax.syntaxSPARQL_11))
            return true;
        return false;
    }

    @Override
    public SPARQLParser create(Syntax syntax) {
        if (syntax.equals(RecSyntax.syntaxRecARQ) ||syntax.equals(Syntax.syntaxARQ))
            return new ParserRecARQ();
        if (syntax.equals(RecSyntax.syntaxRecSPARQL11) || syntax.equals(Syntax.syntaxSPARQL_11))
            return new ParserRecSPARQL11();
        return null;
    }
}
