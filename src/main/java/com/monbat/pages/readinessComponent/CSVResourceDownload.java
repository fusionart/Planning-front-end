package com.monbat.pages.readinessComponent;

import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.ContentDisposition;

import java.time.Instant;
import java.time.LocalTime;


public class CSVResourceDownload extends AbstractResource {
    private final byte[] data;
    private final String filename;

    public CSVResourceDownload(byte[] data, String filename) {
        this.data = data;
        this.filename = filename;
    }

    @Override
    protected ResourceResponse newResourceResponse(Attributes attributes) {
        ResourceResponse response = new ResourceResponse();
        response.setContentType("text/csv");
        response.setContentDisposition(ContentDisposition.ATTACHMENT);
        response.setFileName(filename);
        response.setWriteCallback(new WriteCallback() {
            @Override
            public void writeData(Attributes attributes) {
                attributes.getResponse().write(data);
            }
        });
        response.setLastModified(Instant.from(LocalTime.now()));
        return response;
    }
}
