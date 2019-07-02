package com.zebra.scannercontrol.app.activities;

import java.io.File;
import java.util.List;

/**
 * Created by pndv47 on 7/1/2016.
 */
class ScannerPlugIn {
    List<String> supportedModels;
    String revision;
    File path;

    public ScannerPlugIn(File path, List<String> supportedModels, String revision) {
        this.supportedModels = supportedModels;
        this.revision = revision;
        this.path = path;
    }

    List<String> getSupportedModels() {
        return supportedModels;
    }

    void setSupportedModels(List<String> supportedModels) {
        this.supportedModels = supportedModels;
    }

    String getRevision() {
        return revision;
    }

    void setRevision(String revision) {
        this.revision = revision;
    }

    File getPath() {
        return path;
    }

    void setPath(File path) {
        this.path = path;
    }
}
