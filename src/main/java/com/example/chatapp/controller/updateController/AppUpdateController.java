package com.example.chatapp.controller.updateController;

import com.example.chatapp.DTO.appUpdateResponce.AppUpdateResponse;
import com.example.chatapp.exception.ApiException;
import com.example.chatapp.service.appUpdate.ApkHashService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/app")
public class AppUpdateController {

    private final ApkHashService apkHashService;

    @GetMapping("/check-update")
    public AppUpdateResponse checkUpdate() {

        AppUpdateResponse res = new AppUpdateResponse();
        res.setLatestVersionCode(2);
        res.setLatestVersionName("1.0.2");
        res.setApkUrl("http://192.168.1.11:2000/apk/app-release.apk");
        res.setForceUpdate(false);
        res.setMessage("New update available with bug fixes");

        try (InputStream is = new ClassPathResource("static/apk/app-release.apk").getInputStream()) {
            res.setSha256(apkHashService.calculateSha256(is));
        } catch (Exception e) {
            throw new ApiException("APK not found", HttpStatus.NOT_FOUND);
        }

        return res;
    }
}
