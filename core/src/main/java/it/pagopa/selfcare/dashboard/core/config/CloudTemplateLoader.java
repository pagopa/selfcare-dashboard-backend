package it.pagopa.selfcare.dashboard.core.config;

import freemarker.cache.URLTemplateLoader;

import java.net.MalformedURLException;
import java.net.URL;

class CloudTemplateLoader extends URLTemplateLoader {
    private URL root;

    public CloudTemplateLoader(URL root) {
        super();
        this.root = root;
    }

    @Override
    protected URL getURL(String template) {
        try {
            return new URL(root, "/" + template);
        } catch (MalformedURLException e) {

        }
        return null;
    }
}
