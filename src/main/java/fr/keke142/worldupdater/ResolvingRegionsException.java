package fr.keke142.worldupdater;

public class ResolvingRegionsException extends Exception {
    public ResolvingRegionsException(Exception throwerException) {
        this.initCause(throwerException.getCause());
    }

    public ResolvingRegionsException() {
    }
}
