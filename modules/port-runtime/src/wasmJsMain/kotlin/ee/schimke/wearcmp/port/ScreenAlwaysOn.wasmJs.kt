package ee.schimke.wearcmp.port

/**
 * The Screen Wake Lock API — the web's own answer to `FLAG_KEEP_SCREEN_ON`, and the second place
 * (after `prefers-reduced-motion`) where the browser has a real implementation rather than a stub.
 *
 * Two things are handled here that the Android flag gets for free. The lock is asynchronous, so
 * acquiring it is fire-and-forget and the handle is parked where the release path can find it. And
 * it is per-document rather than per-window, so nesting is counted: two components asking to stay
 * awake must not have one of them release for both.
 *
 * A browser without the API, or one that refuses the request (a background tab always does), is
 * not an error — the promise rejection is swallowed and the screen behaves normally.
 */
private var holders = 0

public actual fun platformSetScreenAlwaysOn(enabled: Boolean) {
    if (enabled) {
        if (holders++ == 0) requestWakeLock()
    } else {
        if (--holders == 0) releaseWakeLock()
    }
}

private fun requestWakeLock() {
    js(
        """{
        if (navigator.wakeLock) {
            navigator.wakeLock.request('screen')
                .then(function (lock) { globalThis.__wearComposeWakeLock = lock; })
                .catch(function () {});
        }
    }"""
    )
}

private fun releaseWakeLock() {
    js(
        """{
        var lock = globalThis.__wearComposeWakeLock;
        if (lock) { globalThis.__wearComposeWakeLock = null; lock.release().catch(function () {}); }
    }"""
    )
}
