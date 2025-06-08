package com.application.repository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.application.entity.MediaFile;

@Repository
public interface MediaFileRepository extends MongoRepository<MediaFile, String> {
 // Spring Data MongoDB provides basic CRUD methods
}
