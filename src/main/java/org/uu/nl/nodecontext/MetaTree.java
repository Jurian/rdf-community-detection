package org.uu.nl.nodecontext;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.shared.PrefixMapping;

public class MetaTree  {

    private String query;

    public MetaTree(String query) {
        this.query = query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Query getQuery(Node root) {
        Query q = QueryFactory.create(String.format(query, root.getURI()));
        return q;
    }
}
