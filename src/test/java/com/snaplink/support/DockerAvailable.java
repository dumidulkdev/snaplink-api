package com.snaplink.support;

import org.testcontainers.DockerClientFactory;

public final class DockerAvailable {

    private DockerAvailable() {
    }

    public static boolean isDockerAvailable() {
        String flag = System.getProperty("testcontainers.docker.available", "true");
        if ("false".equalsIgnoreCase(flag)) {
            return false;
        }
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (Exception e) {
            return false;
        }
    }
}