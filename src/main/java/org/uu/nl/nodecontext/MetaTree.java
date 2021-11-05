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
        return QueryFactory.create(query.replace("?root", "<" + root.getURI() + ">"));
    }

    public String getQuery() {
        return this.query;
    }

    public static String createCountQuery(String query) {
        return query.replaceFirst("SELECT .* WHERE", "SELECT (COUNT(*) AS ?count) WHERE");
    }

    public static String addValues(String query, NodeIndex roots) {

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("VALUES(?root){\n");
        for(String root : roots.focusKeys) {
            queryBuilder.append("\t(<");
            queryBuilder.append(root);
            queryBuilder.append(">)\n");
        }
        queryBuilder.append('}');

        int insertAt = query.lastIndexOf('}');
        queryBuilder.insert(0, query.substring(0, insertAt));
        queryBuilder.append(query.substring(insertAt));
        return queryBuilder.toString();
    }
}
