package com.phithang.mysocialnetwork.service.Impl;

import com.phithang.mysocialnetwork.repository.MediaRepository;
import com.phithang.mysocialnetwork.service.IMediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MediaService implements IMediaService {
    @Autowired
    private MediaRepository mediaRepository;

}
