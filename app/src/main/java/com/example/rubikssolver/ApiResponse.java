package com.example.rubikssolver;

import java.util.List;

public class ApiResponse {

    private boolean success;
    private List<String> detections;
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<String> getDetections() {
        return detections;
    }

    public void setDetections(List<String> detections) {
        this.detections = detections;
    }
}
