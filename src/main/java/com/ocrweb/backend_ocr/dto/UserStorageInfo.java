package com.ocrweb.backend_ocr.dto;

public class UserStorageInfo {
    private long usedStorage;
    private long availableStorage;
    private long upgradedStorage;

    public UserStorageInfo(long usedStorage, long availableStorage, long upgradedStorage) {
        this.usedStorage = usedStorage;
        this.availableStorage = availableStorage;
        this.upgradedStorage = upgradedStorage;
    }

    // Getters and setters

    public long getUsedStorage() {
        return usedStorage;
    }

    public void setUsedStorage(long usedStorage) {
        this.usedStorage = usedStorage;
    }

    public long getUpgradedStorage() {
        return upgradedStorage;
    }

    public void setUpgradedStorage(long upgradedStorage) {
        this.upgradedStorage = upgradedStorage;
    }

    public long getAvailableStorage() {
        return availableStorage;
    }

    public void setAvailableStorage(long availableStorage) {
        this.availableStorage = availableStorage;
    }
}
