package com.monbat.pages.importfiles;

import com.monbat.utils.TypeConstants;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.Bytes;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.time.Duration;

public class ImportFiles extends Panel {

    private final FileUploadField fileUploadField;
    private final FeedbackPanel feedbackPanel; // Feedback panel for messages
    private final Label progressLabel;
    private String uploadId;

    public ImportFiles(String id) {
        super(id);
        // Feedback panel for user messages
        feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true); // Allow it to be updated via Ajax
        add(feedbackPanel);

        // Create a form with file upload support
        Form<Void> form = new Form<>("form");
        form.setMultiPart(true);
        form.setMaxSize(Bytes.megabytes(10)); // Set max file size
        add(form);

        // Progress label
        progressLabel = new Label("progressLabel", Model.of("Processing: 0%"));
        progressLabel.setOutputMarkupId(true);
        form.add(progressLabel);

        // File upload field
        fileUploadField = new FileUploadField("fileUpload");
        form.add(fileUploadField);

        // Add multiple upload buttons
        addUploadButton(form, "submitBom", TypeConstants.BOM);
        addUploadButton(form, "submitMaterial", TypeConstants.MATERIAL);
        addUploadButton(form, "submitProdversion", TypeConstants.PRODUCTION_VERSION);
        addUploadButton(form, "submitReadiness", TypeConstants.READINESS);
        addUploadButton(form, "submitRouting", TypeConstants.ROUTING);
        addUploadButton(form, "submitWorkCenterCapacity", TypeConstants.WORK_CENTER_CAPACITY);
        addUploadButton(form, "submitQuantities", TypeConstants.QUANTITIES);
    }

    private String sendFileToSpringBootApi(File file, String type) {
        RestTemplate restTemplate = new RestTemplate();
        String apiUrl = "http://localhost:8080/api/files/upload";

        LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(file));
        body.add("type", type);

        return restTemplate.postForObject(apiUrl, new HttpEntity<>(body), String.class);
    }

    private int fetchProgressFromApi(String uploadId, String type) {
        RestTemplate restTemplate = new RestTemplate();
        String apiUrl = String.format("http://localhost:8080/api/files/progress/%s?type=%s", uploadId, type);
        return restTemplate.getForObject(apiUrl, Integer.class);
    }

    private void addUploadButton(Form<?> form, String buttonId, String type) {
        AjaxButton uploadButton = new AjaxButton(buttonId) {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                super.onSubmit(target);

                if (fileUploadField.getFileUpload() != null) {
                    File tempFile = new File(System.getProperty("java.io.tmpdir") + "/" +
                            fileUploadField.getFileUpload().getClientFileName());
                    try {
                        fileUploadField.getFileUpload().writeTo(tempFile);

                        // Send file to Spring Boot and get upload ID
                        uploadId = sendFileToSpringBootApi(tempFile, type);

                        // Start progress tracking
                        startProgressTracking(target, type);

                    } catch (Exception e) {
                        e.printStackTrace();
                        error("File upload failed: " + e.getMessage());
                        target.add(feedbackPanel);
                    }
                } else {
                    error("No file selected.");
                    target.add(feedbackPanel);
                }
            }

            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);

                // Optional: Add client-side onclick event logic
                tag.put("onclick", "console.log('Upload button clicked');");
            }
        };

        form.add(uploadButton);
    }

    private void startProgressTracking(AjaxRequestTarget target, String type) {
        AbstractAjaxTimerBehavior timer = new AbstractAjaxTimerBehavior(Duration.ofSeconds(1)) {
            @Override
            protected void onTimer(AjaxRequestTarget target) {
                try {
                    int progress = fetchProgressFromApi(uploadId, type);
                    progressLabel.setDefaultModelObject("Processing: " + progress + "%");
                    target.add(progressLabel);

                    if (progress >= 100) {
                        stop(target); // Stop polling when complete
                        info("File processing complete!");
                        target.add(feedbackPanel);
                    }
                } catch (Exception e) {
                    stop(target);
                    error("Error checking progress: " + e.getMessage());
                    target.add(feedbackPanel);
                }
            }
        };

        progressLabel.add(timer);
        target.add(progressLabel); // Ensure the progress label is updated
    }
}

