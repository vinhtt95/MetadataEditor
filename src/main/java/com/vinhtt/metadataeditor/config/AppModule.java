package com.vinhtt.metadataeditor.config;

import com.google.inject.AbstractModule;
import com.vinhtt.metadataeditor.service.IFileService;
import com.vinhtt.metadataeditor.service.IMetadataService;
import com.vinhtt.metadataeditor.service.impl.LocalFileService;
import com.vinhtt.metadataeditor.service.impl.FfmpegMetadataService;

public class AppModule extends AbstractModule {
    @Override
    protected void configure() {
        // Map Interface -> Implementation
        bind(IFileService.class).to(LocalFileService.class);
        bind(IMetadataService.class).to(FfmpegMetadataService.class);
    }
}