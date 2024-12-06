package com.utndds.heladerasApi.services;

import org.springframework.beans.factory.annotation.Value;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class StorageService {

    private final String BUCKET_NAME = "heladerasddsimages"; // Reemplaza con el nombre de tu bucket

    private final Storage storage;
    @Value("${firebase.type}")
    private String type;

    @Value("${firebase.project_id}")
    private String projectId;

    @Value("${firebase.private_key_id}")
    private String privateKeyId;

    @Value("${firebase.private_key}")
    private String privateKey;

    @Value("${firebase.client_email}")
    private String clientEmail;

    @Value("${firebase.client_id}")
    private String clientId;

    @Value("${firebase.auth_uri}")
    private String authUri;

    @Value("${firebase.token_uri}")
    private String tokenUri;

    @Value("${firebase.cert_url}")
    private String certUrl;

    @Value("${firebase.client_cert_url}")
    private String clientCertUrl;

    @Value("${firebase.universe_domain}")
    private String universeDomain;

    public StorageService() throws IOException {
        // Construir el JSON a partir de las variables de entorno inyectadas
        String jsonCredentials = String.format(
            "{\"type\":\"%s\",\"project_id\":\"%s\",\"private_key_id\":\"%s\",\"private_key\":\"%s\",\"client_email\":\"%s\",\"client_id\":\"%s\",\"auth_uri\":\"%s\",\"token_uri\":\"%s\",\"auth_provider_x509_cert_url\":\"%s\",\"client_x509_cert_url\":\"%s\",\"universe_domain\":\"%s\"}",
            type, projectId, privateKeyId, privateKey, clientEmail, clientId, authUri, tokenUri, certUrl, clientCertUrl, universeDomain
        );

        // Inicializar el cliente de Google Cloud Storage usando las credenciales en formato JSON
        storage = StorageOptions.newBuilder()
                .setCredentials(ServiceAccountCredentials.fromStream(
                        new ByteArrayInputStream(jsonCredentials.getBytes(StandardCharsets.UTF_8))
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
