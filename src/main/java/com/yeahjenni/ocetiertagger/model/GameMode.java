package com.yeahjenni.ocetiertagger.model;

public record GameMode(String id, String title) {
    public String getId() { return id; }
    public String getTitle() { return title; }
}
