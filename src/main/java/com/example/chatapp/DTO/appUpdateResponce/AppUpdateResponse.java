package com.example.chatapp.DTO.appUpdateResponce;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AppUpdateResponse {
    private int latestVersionCode;
    private String latestVersionName;
    private String apkUrl;
    private String sha256;
    private boolean forceUpdate;
    private String message;
}
