package it.pagopa.selfcare.dashboard.connector.azure_storage;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobProperties;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import it.pagopa.selfcare.dashboard.connector.exception.FileUploadException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

class AzureBlobClientTest {

    @Test
    void uploadInstitutionLogo_ok() throws URISyntaxException, InvalidKeyException, FileUploadException, IOException, NoSuchFieldException, IllegalAccessException, StorageException {
        // given
        AzureBlobClient blobClient = new AzureBlobClient("UseDevelopmentStorage=true;", "$web");
        CloudBlockBlob blockBlobMock = Mockito.mock(CloudBlockBlob.class);
        Mockito.when(blockBlobMock.getProperties())
                .thenReturn(new BlobProperties());
        Mockito.doNothing().
                when(blockBlobMock).upload(Mockito.any(), Mockito.anyByte());
        CloudBlobContainer blobContainerMock = Mockito.mock(CloudBlobContainer.class);
        Mockito.when(blobContainerMock.getBlockBlobReference("filename.png"))
                .thenReturn(blockBlobMock);
        CloudBlobClient blobClientMock = Mockito.mock(CloudBlobClient.class);
        Mockito.when(blobClientMock.getContainerReference("$web"))
                .thenReturn(blobContainerMock);
        mockCloudBlobClient(blobClient, blobClientMock);
        InputStream resource = new ClassPathResource("logo-pagopa-spa.png")
                .getInputStream();
        // when
        Executable executable = () -> blobClient.uploadInstitutionLogo(resource, "filename.png", "image/png");
        // then
        Assertions.assertDoesNotThrow(executable);
    }


    @Test
    void uploadInstitutionLogo_ko() throws URISyntaxException, InvalidKeyException, FileUploadException, IOException, NoSuchFieldException, IllegalAccessException, StorageException {
        // given
        AzureBlobClient blobClient = new AzureBlobClient("UseDevelopmentStorage=true;", "$web");
        CloudBlobClient blobClientMock = Mockito.mock(CloudBlobClient.class);
        Mockito.doThrow(StorageException.class)
                .when(blobClientMock)
                .getContainerReference("$web");
        mockCloudBlobClient(blobClient, blobClientMock);
        InputStream resource = new ClassPathResource("logo-pagopa-spa.png")
                .getInputStream();
        // when
        Executable executable = () -> blobClient.uploadInstitutionLogo(resource, "filename.png", "image/png");
        // then
        Assertions.assertThrows(FileUploadException.class, executable);
    }


    private void mockCloudBlobClient(AzureBlobClient blobClient, CloudBlobClient blobClientMock) throws NoSuchFieldException, IllegalAccessException {
        Field field = AzureBlobClient.class.getDeclaredField("blobClient");
        field.setAccessible(true);
        field.set(blobClient, blobClientMock);
    }

}