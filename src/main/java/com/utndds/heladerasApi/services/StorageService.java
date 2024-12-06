package com.utndds.heladerasApi.services;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.utndds.heladerasApi.config.FirebaseConfig;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class StorageService {

    private final String BUCKET_NAME = "heladerasddsimages"; // Reemplaza con el nombre de tu bucket

    private final Storage storage;

    private final FirebaseConfig firebaseConfig;

    public StorageService(FirebaseConfig firebaseConfig) throws IOException {
        this.firebaseConfig = firebaseConfig;

        String jsonCredentials = String.format(
                "{\"type\":\"%s\",\"project_id\":\"%s\",\"private_key_id\":\"%s\",\"private_key\":\"%s\",\"client_email\":\"%s\",\"client_id\":\"%s\",\"auth_uri\":\"%s\",\"token_uri\":\"%s\",\"auth_provider_x509_cert_url\":\"%s\",\"client_x509_cert_url\":\"%s\",\"universe_domain\":\"%s\"}",
                "service_account", // Usa 'service_account' si este es el tipo, o usa el valor que necesites
                firebaseConfig.getProjectId(),
                firebaseConfig.getPrivateKeyId(),
                firebaseConfig.getPrivateKey(),
                firebaseConfig.getClientEmail(),
                firebaseConfig.getClientId(),
                firebaseConfig.getAuthUri(),
                firebaseConfig.getTokenUri(),
                firebaseConfig.getCertUrl(),
                firebaseConfig.getClientCertUrl(),
                firebaseConfig.getUniverseDomain());

        storage = StorageOptions.newBuilder()
                .setCredentials(ServiceAccountCredentials.fromStream(
                        new ByteArrayInputStream(jsonCredentials.getBytes(StandardCharsets.UTF_8))))
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
