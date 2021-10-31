package org.uu.nl.embedding.metatree;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;

public class MetaTree  {

    private final String queryBase;

    public MetaTree(String queryBase) {
        this.queryBase = queryBase;
    }

    public Query getQuery(Node root) {
        return QueryFactory.create(String.format(queryBase, root.getURI()));
    }
}
