package com.utndds.heladerasApi.services;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;

@Service
public class StorageService {

    private final String BUCKET_NAME = "heladerasddsimages"; // Reemplaza con el nombre de tu bucket

    private final Storage storage;

    public StorageService() throws IOException {
        // Inicializa el cliente de Google Cloud Storage
        storage = StorageOptions.newBuilder()
                .setCredentials(ServiceAccountCredentials.fromStream(
                        new FileInputStream("/etc/secrets/leafy-respect-443713-n1-1d09a8beb458") // Ruta al archivo JSON
                ))
                .build()
                .getService();
    }

    // Método para subir un archivo al bucket
    public String uploadFile(MultipartFile file) throws IOException {
        String blobName = file.getOriginalFilename(); // Usa el nombre original del archivo
        BlobId blobId = BlobId.of(BUCKET_NAME, blobName); // Identificador único para el archivo
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build(); // Metadata del archivo

        // Subir el archivo
        storage.create(blobInfo, file.getBytes());

        // Retorna la URL pública del archivo
        return String.format("https://storage.googleapis.com/%s/%s", BUCKET_NAME, blobName);
    }

    // Método para descargar un archivo desde el bucket
    public byte[] downloadFile(String fileName) {
        Blob blob = storage.get(BlobId.of(BUCKET_NAME, fileName));
        return blob.getContent();
    }
}
