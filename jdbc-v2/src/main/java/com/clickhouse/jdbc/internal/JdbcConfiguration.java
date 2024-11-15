package com.clickhouse.jdbc.internal;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.DriverPropertyInfo;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class JdbcConfiguration {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JdbcConfiguration.class);
    public static final String PREFIX_CLICKHOUSE = "jdbc:clickhouse:";
    public static final String PREFIX_CLICKHOUSE_SHORT = "jdbc:ch:";

    final String host;
    final int port;
    final String protocol;
    final String database;
    final String user;
    final String password;
    final Map<String, String> queryParams;

    public String getDatabase() {
        return database;
    }

    public String getHost() {
        return host;
    }

    public String getPassword() {
        return password;
    }

    public int getPort() {
        return port;
    }

    public String getProtocol() {
        return protocol;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public String getUser() {
        return user;
    }

    public JdbcConfiguration(String url, Properties info) {
        Map<String, String> urlProperties = parseUrl(url);
        this.host = urlProperties.get("host");
        this.port = Integer.parseInt(urlProperties.get("port"));
        this.protocol = urlProperties.get("protocol");
        this.database = urlProperties.get("database") == null ? "default" : urlProperties.get("database");
        this.queryParams = urlProperties.get("queryParams") == null ? new HashMap<>() : parseQueryParams(urlProperties.get("queryParams"));


        this.user = info.getProperty("user", "default");
        this.password = info.getProperty("password", "");
    }

    public static boolean acceptsURL(String url) {
        return url.startsWith(PREFIX_CLICKHOUSE) || url.startsWith(PREFIX_CLICKHOUSE_SHORT);
    }

    public DriverPropertyInfo[] getPropertyInfo() {
        List<DriverPropertyInfo> properties = new ArrayList<>();
        properties.add(new DriverPropertyInfo("host", host));
        properties.add(new DriverPropertyInfo("port", String.valueOf(port)));
        properties.add(new DriverPropertyInfo("protocol", protocol));
        properties.add(new DriverPropertyInfo("database", database));
        properties.add(new DriverPropertyInfo("user", user));
        properties.add(new DriverPropertyInfo("password", "*REDACTED*"));
        properties.add(new DriverPropertyInfo("queryParams", queryParams.toString()));
        return properties.toArray(new DriverPropertyInfo[0]);
    }

    private Map<String, String> parseUrl(String urlString) {
        log.debug("Parsing URL: {}", urlString);
        URL url;
        try {
            String urlStripped = stripUrlPrefix(urlString);
            int index = urlStripped.indexOf("//");
            if (index == 0) {//Add in the HTTP protocol if it is missing
                urlStripped = "http:" + urlStripped;
            }

            url = new URL(urlStripped);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("URL is malformed.");
        }

        Map<String, String> urlProperties = new HashMap<>();
        urlProperties.put("host", url.getHost());
        urlProperties.put("protocol", url.getProtocol());
        urlProperties.put("port", String.valueOf(url.getPort() == -1 ?
                url.getProtocol().equalsIgnoreCase("HTTP") ? 8123 : 8443
                : url.getPort()));

        try {
            urlProperties.put("database", url.getPath().substring(1));
        } catch (StringIndexOutOfBoundsException e) {
            urlProperties.put("database", "default");
        }

        urlProperties.put("queryParams", url.getQuery());

        return urlProperties;
    }
    private String stripUrlPrefix(String url) {
        if (url.startsWith(PREFIX_CLICKHOUSE)) {
            return url.substring(PREFIX_CLICKHOUSE.length());
        } else if (url.startsWith(PREFIX_CLICKHOUSE_SHORT)) {
            return url.substring(PREFIX_CLICKHOUSE_SHORT.length());
        } else {
            throw new IllegalArgumentException("URL is not supported.");
        }
    }
    private Map<String, String> parseQueryParams(String queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return new HashMap<>(0);
        }

        return Arrays.stream(queryParams.split("&"))
                .map(s -> {
                            String[] parts = s.split("=");
                            return new AbstractMap.SimpleImmutableEntry<>(parts[0], parts[1]);
                }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
