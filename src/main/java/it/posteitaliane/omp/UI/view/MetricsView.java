package it.posteitaliane.omp.UI.view;

import com.google.common.base.Function;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;
import it.posteitaliane.omp.actor.OMPSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.zip.ZipOutputStream;

public class MetricsView extends BaseView<VerticalLayout> {
    private static Function<String, String> TEMPORARY_FILE = new Function<String, String>() {
        @Override
        public String apply(String filename) {
            return MessageFormat.format("{0}/{1}", System.getProperty("java.io.tmpdir"), filename);
        }
    };

    public MetricsView() {
        super(new VerticalLayout());
        setSizeFull();
        MetricsHistoryUploader uploadReceiver = new MetricsHistoryUploader();
        Upload upload = new Upload("Upload Metrics history file.", uploadReceiver);
        upload.addSucceededListener(uploadReceiver);
        addComponent(upload);
    }

    @Override
    protected void onEnter(ViewChangeListener.ViewChangeEvent event) {
    }

    class MetricsHistoryUploader implements Upload.Receiver, Upload.SucceededListener {
        private final Logger log = LoggerFactory.getLogger(MetricsHistoryUploader.class);
        ZipOutputStream zippedOutStream = null;
        FileOutputStream outStream = null;
        String filename = null;

        @Override
        public OutputStream receiveUpload(String filename, String mimeType) {
            try {
                this.filename = filename;
                log.info("creating stream");
                return new FileOutputStream(TEMPORARY_FILE.apply(filename));
            } catch (IOException e) {
                new Notification("Could not open file <br/>", e.getMessage(), Notification.Type.ERROR_MESSAGE)
                        .show(Page.getCurrent());
                return null;
            }
        }

        @Override
        public void uploadSucceeded(Upload.SucceededEvent succeededEvent) {
            new Notification("Upload succeeded to " + System.getProperty("java.io.tmpdir"), Notification.Type.TRAY_NOTIFICATION)
                    .show(Page.getCurrent());

            OMPSystem.uploadedFile(TEMPORARY_FILE.apply(filename));
        }
    }

}
