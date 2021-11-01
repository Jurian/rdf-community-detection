package org.uu.nl.nodecontext;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;

public class MetaTree  {

    private String query;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Query getQuery(Node root) {
        return QueryFactory.create(String.format(query, root.getURI()));
    }
}
