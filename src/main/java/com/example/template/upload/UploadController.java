package com.example.template.upload;

import com.example.template.upload.dto.UploadMetadataRequest;
import com.example.template.upload.dto.UploadProcessingResponse;
import com.example.template.upload.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Tag(name = "Uploads", description = "Processes CSV/TXT files and persists successful rows")
@SecurityRequirement(name = "bearer-jwt")
@RestController
@RequestMapping("/api/v1/uploads/records")
public class UploadController {

    private final FileUploadService fileUploadService;

    public UploadController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    @Operation(summary = "Upload CSV or TXT file", description = "Accepts a text/csv file and metadata, stores valid rows, and returns failed rows with reasons.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "File processed"),
            @ApiResponse(responseCode = "400", description = "Invalid payload", content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<UploadProcessingResponse> upload(@RequestPart("file") FilePart file,
                                                 @Valid @RequestPart("metadata") UploadMetadataRequest metadata) {
        return fileUploadService.process(file, metadata);
    }
}
