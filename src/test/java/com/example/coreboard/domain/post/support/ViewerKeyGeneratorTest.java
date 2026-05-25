package com.example.coreboard.domain.post.support;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


class ViewerKeyGeneratorTest {
    ViewerKeyGenerator viewerKeyGenerator = new ViewerKeyGenerator();

    @Test
    @DisplayName("X_Forwarded_For가_있으면_첫번째_IP로_viewerKey를_생성한다")
    void generateByForwardedForFirstIp() {
        HttpServletRequest request = mock(HttpServletRequest.class);

        given(request.getHeader("X-Forwarded-For"))
                .willReturn("1.1.1.1, 2.2.2.2, 3.3.3.3");

        String result = viewerKeyGenerator.generate(request);

        assertEquals("ip:1.1.1.1", result);

        verify(request).getHeader("X-Forwarded-For");
        verify(request, never()).getRemoteAddr();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("X_Forwarded_For가_null_또는_공백이면_remoteAddr로_viewerKey를_생성한다")
    void generateByRemoteAddrWhenForwardedForInvalid(String forwardedFor) {
        HttpServletRequest request = mock(HttpServletRequest.class);

        given(request.getHeader("X-Forwarded-For"))
                .willReturn(forwardedFor);
        given(request.getRemoteAddr())
                .willReturn("10.0.0.1");

        String result = viewerKeyGenerator.generate(request);

        assertEquals("ip:10.0.0.1", result);

        verify(request).getHeader("X-Forwarded-For");
        verify(request).getRemoteAddr();
    }

    @Test
    @DisplayName("X_Forwarded_For의_첫번째_IP_앞뒤_공백을_제거한다")
    void generateTrimmedFirstForwardedForIp() {
        HttpServletRequest request = mock(HttpServletRequest.class);

        given(request.getHeader("X-Forwarded-For"))
                .willReturn("  1.1.1.1  , 2.2.2.2");

        String result = viewerKeyGenerator.generate(request);

        assertEquals("ip:1.1.1.1", result);

        verify(request).getHeader("X-Forwarded-For");
        verify(request, never()).getRemoteAddr();
    }
}